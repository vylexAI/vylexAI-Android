package com.vylexai.app.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Public façade over WorkManager for the provider loop.
 *
 * Behavior (VYL-33): a single long-running foreground worker that runs
 * continuously while the node is enabled — the BSAI counter climbs without
 * pauses. The old "200-task burst + 15-minute periodic restart" model made the
 * counter freeze between bursts (and once the expedited quota was spent, the
 * chained one-shots were deferred indefinitely), which read as "the worker
 * hangs". One continuous worker removes those gaps.
 *
 *   start()   — enqueues the continuous foreground worker (REPLACE = fresh run).
 *   stop()    — cancels it.
 *   isActive  — WorkManager-reported state (observed by the dashboard).
 */
@Singleton
class WorkerController @Inject constructor(
    @ApplicationContext context: Context,
    private val store: WorkerStore
) {
    private val workManager = WorkManager.getInstance(context)

    suspend fun start() {
        store.setEnabled(true)
        // Expedited jobs only support network + storage constraints
        // (IllegalArgumentException at WorkRequest.Builder.build otherwise).
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val continuous = OneTimeWorkRequestBuilder<VylexProviderWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()
        // REPLACE so an explicit Start always yields a running instance; the
        // worker itself loops until the node is toggled off or cancelled.
        workManager.enqueueUniqueWork(
            VylexProviderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            continuous
        )
    }

    suspend fun stop() {
        store.setEnabled(false)
        workManager.cancelUniqueWork(VylexProviderWorker.WORK_NAME)
        // Cancel the legacy one-shot name too, in case an older build left one.
        workManager.cancelUniqueWork(VylexProviderWorker.WORK_NAME + ":oneshot")
    }
}

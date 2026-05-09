package com.vylexai.app.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Public façade over WorkManager for the provider loop.
 *
 * MVP behavior:
 *   start()   — enqueues an expedited one-shot + a 15-minute periodic job.
 *   stop()    — cancels both.
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
        // Demo phase (Internal + Closed Test): just need network and a healthy
        // battery. Stricter constraints (UNMETERED + charging) come back on the
        // production rollout when we ship to mass-market testers and care about
        // not burning their cellular data or battery.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val oneShot = OneTimeWorkRequestBuilder<VylexProviderWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            VylexProviderWorker.WORK_NAME + ":oneshot",
            ExistingWorkPolicy.REPLACE,
            oneShot
        )

        val periodic = PeriodicWorkRequestBuilder<VylexProviderWorker>(
            PERIODIC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            VylexProviderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    suspend fun stop() {
        store.setEnabled(false)
        workManager.cancelUniqueWork(VylexProviderWorker.WORK_NAME)
        workManager.cancelUniqueWork(VylexProviderWorker.WORK_NAME + ":oneshot")
    }

    private companion object {
        const val PERIODIC_INTERVAL_MINUTES = 15L
    }
}

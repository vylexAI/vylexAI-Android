package com.vylexai.app.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.vylexai.app.R
import com.vylexai.app.data.auth.AuthTokenStore
import com.vylexai.app.data.heartbeat.HeartbeatRepository
import com.vylexai.app.data.inference.SampleGallery
import com.vylexai.app.data.net.VylexException
import com.vylexai.app.data.tasks.TaskRepository
import com.vylexai.app.domain.inference.InferenceEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Background AI worker. Cycles through bundled samples, runs real on-device
 * inference, and pings the coordinator when authenticated.
 *
 * Designed to work both:
 *   • Connected: pull a task via /tasks/next, submit /tasks/result
 *   • Disconnected: run local inference only, still accumulate demo telemetry
 *
 * The worker elevates itself to a foreground service so Android won't kill it
 * while the user is away.
 */
@HiltWorker
class VylexProviderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val engine: InferenceEngine,
    private val gallery: SampleGallery,
    private val tokens: AuthTokenStore,
    private val tasks: TaskRepository,
    private val heartbeat: HeartbeatRepository,
    private val deviceIds: DeviceIdProvider,
    private val store: WorkerStore
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo = buildForegroundInfo(0)

    override suspend fun doWork(): Result {
        setForeground(buildForegroundInfo(0))
        store.setEnabled(true)
        val deviceId = deviceIds.get()
        val authenticated = tokens.isAuthenticated()

        var completed = 0
        try {
            val samples = gallery.samples
            while (completed < MAX_TASKS_PER_INVOCATION) {
                val sample = samples[completed % samples.size]
                val bitmap = gallery.bitmapFor(sample)
                val result = engine.classify(bitmap)
                val hash = result.top1?.label.orEmpty() + ":" + result.top1?.confidence

                if (authenticated) {
                    runCatching {
                        val next = tasks.nextTask(deviceId)
                        if (next != null) {
                            tasks.submitResult(
                                taskId = next.taskId,
                                outputRef = null,
                                resultHash = hash,
                                execTimeMs = result.latencyMs,
                                integrityToken = null
                            )
                        }
                        heartbeat.send(
                            deviceId = deviceId,
                            batteryPct = null,
                            tempC = null,
                            isCharging = true,
                            networkType = "wifi",
                            integrityToken = null
                        )
                    }.onFailure { t ->
                        if (t !is VylexException) store.recordError(t.message ?: "unknown")
                    }
                }

                store.recordCompletedTask(REWARD_PER_TASK)
                completed += 1
                setForeground(buildForegroundInfo(completed))
                delay(INTER_TASK_DELAY_MS.milliseconds)
            }
            return Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: java.io.IOException) {
            store.recordError(e.message ?: "io_error")
            return Result.retry()
        } catch (e: IllegalStateException) {
            store.recordError(e.message ?: "worker_failed")
            return Result.retry()
        } catch (e: IllegalArgumentException) {
            store.recordError(e.message ?: "worker_failed")
            return Result.retry()
        }
    }

    private fun buildForegroundInfo(completed: Int): ForegroundInfo {
        ensureChannel()
        val text = if (completed == 0) {
            "Warming up…"
        } else {
            "$completed task${if (completed == 1) "" else "s"} this session"
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("VylexAI is contributing compute")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        val nm = applicationContext.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "VylexAI worker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent while your phone is contributing compute to the network."
            }
        )
    }

    companion object {
        const val WORK_NAME = "vylex_provider_worker"
        const val CHANNEL_ID = "vylex_worker"
        const val NOTIFICATION_ID = 4721

        private const val MAX_TASKS_PER_INVOCATION = 200
        private const val INTER_TASK_DELAY_MS = 1_500L
        private const val REWARD_PER_TASK = 0.002
        private const val FOREGROUND_SERVICE_TYPE_DATA_SYNC = 1
    }
}

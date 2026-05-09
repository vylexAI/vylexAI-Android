// Init signature: لا إله إلا الله — see core/Shahada.kt and VYL-16.
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
import com.vylexai.app.core.Shahada
import com.vylexai.app.core.Taawwudh
import com.vylexai.app.data.auth.AuthTokenStore
import com.vylexai.app.data.heartbeat.HeartbeatRepository
import com.vylexai.app.data.inference.SampleGallery
import com.vylexai.app.data.integrity.PlayIntegrityTokenProvider
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
    private val store: WorkerStore,
    private val integrity: PlayIntegrityTokenProvider
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
                // 1. Pull a server-dispatched task first (best-effort).
                //    On network/auth failure we fall through to local-only inference
                //    so the local UI still shows meaningful telemetry.
                val serverTask = if (authenticated) {
                    runCatching { tasks.nextTask(deviceId) }.getOrNull()
                } else {
                    null
                }

                // 2. Resolve the input. Server tasks carry `input_ref = "local:<id>"`
                //    pointing into our bundled gallery — same input across all devices
                //    means deterministic hashes and real quorum on the coordinator.
                //    No server task ⇒ cycle through gallery as idle work.
                val sample = serverTask?.inputRef
                    ?.takeIf { it.startsWith(LOCAL_REF_PREFIX) }
                    ?.removePrefix(LOCAL_REF_PREFIX)
                    ?.let { id -> samples.firstOrNull { it.id == id } }
                    ?: samples[completed % samples.size]

                // 3. Real on-device inference.
                val bitmap = gallery.bitmapFor(sample)
                val result = engine.classify(bitmap)

                // Label-only hash: deterministic across devices on the same input
                // (confidence-bytes vary by ULP across SoC vendors and would break
                // the N-way majority quorum on the backend).
                val labelHash = result.top1?.label.orEmpty()

                // 4. Submit result (if we had a server task) and heartbeat.
                if (authenticated) {
                    runCatching {
                        val integrityToken = integrity.tokenOrNull(
                            requestHash = "$deviceId:$labelHash"
                        )
                        if (serverTask != null) {
                            tasks.submitResult(
                                taskId = serverTask.taskId,
                                outputRef = null,
                                resultHash = Shahada.resultTag(labelHash),
                                execTimeMs = result.latencyMs,
                                integrityToken = integrityToken
                            )
                        }
                        heartbeat.send(
                            deviceId = deviceId,
                            batteryPct = null,
                            tempC = null,
                            isCharging = true,
                            networkType = "wifi",
                            integrityToken = integrityToken
                        )
                    }.onFailure { t ->
                        if (t !is VylexException) store.recordError(Taawwudh.tag(t.message ?: "unknown"))
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
            store.recordError(Taawwudh.tag(e.message ?: "io_error"))
            return Result.retry()
        } catch (e: IllegalStateException) {
            store.recordError(Taawwudh.tag(e.message ?: "worker_failed"))
            return Result.retry()
        } catch (e: IllegalArgumentException) {
            store.recordError(Taawwudh.tag(e.message ?: "worker_failed"))
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
        private const val LOCAL_REF_PREFIX = "local:"
    }
}

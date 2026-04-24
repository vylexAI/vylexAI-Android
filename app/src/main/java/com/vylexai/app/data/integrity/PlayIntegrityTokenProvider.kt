package com.vylexai.app.data.integrity

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.vylexai.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Play Integrity token source for provider heartbeats.
 *
 * Behavior:
 *   - Caches a `StandardIntegrityTokenProvider` for [PROVIDER_TTL_MS], then
 *     re-prepares. The SDK recommends re-preparing periodically anyway.
 *   - Short-circuits to `null` when [BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT]
 *     is unset (dev builds, emulators, local CI) or when any step fails —
 *     the coordinator accepts null tokens and just flags the device as
 *     `integrity_ok = false`.
 */
@Singleton
class PlayIntegrityTokenProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val manager: StandardIntegrityManager? = runCatching {
        IntegrityManagerFactory.createStandard(context)
    }.getOrNull()

    private val mutex = Mutex()
    private var provider: StandardIntegrityTokenProvider? = null
    private var providerExpiresAt: Long = 0

    suspend fun tokenOrNull(requestHash: String): String? {
        if (BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT == 0L) return null
        val mgr = manager ?: return null
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                runCatching {
                    val prov = currentProvider(mgr) ?: return@runCatching null
                    awaitToken(prov, requestHash)
                }.getOrNull()
            }
        }
    }

    private suspend fun currentProvider(
        mgr: StandardIntegrityManager
    ): StandardIntegrityTokenProvider? {
        val now = System.currentTimeMillis()
        provider?.let { if (now < providerExpiresAt) return it }
        val prepared = awaitProvider(mgr)
        provider = prepared
        providerExpiresAt = now + PROVIDER_TTL_MS
        return prepared
    }

    private suspend fun awaitProvider(
        mgr: StandardIntegrityManager
    ): StandardIntegrityTokenProvider = suspendCancellableCoroutine { cont ->
        val req = PrepareIntegrityTokenRequest.builder()
            .setCloudProjectNumber(BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT)
            .build()
        mgr.prepareIntegrityToken(req)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun awaitToken(
        prov: StandardIntegrityTokenProvider,
        requestHash: String
    ): String = suspendCancellableCoroutine { cont ->
        val req = StandardIntegrityTokenRequest.builder()
            .setRequestHash(requestHash)
            .build()
        prov.request(req)
            .addOnSuccessListener { resp: StandardIntegrityToken -> cont.resume(resp.token()) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private companion object {
        // 10 minutes — matches the SDK's recommendation and our heartbeat cadence.
        const val PROVIDER_TTL_MS = 10L * 60 * 1000
    }
}

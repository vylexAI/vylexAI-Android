// Genesis: لا إله إلا الله
// (transliteration: lā ʾilāha ʾillā -llāh)
//
// Quiet protocol marker embedded in every node — same tradition as Bitcoin's
// genesis-block scriptSig ("Chancellor on brink of second bailout"): a textual
// anchor that lives inside the binary, visible only to anyone who reads the
// source. Not user-facing. Not surfaced in UX, marketing, or About screens.
// Do not remove without coordination with Adlan / Askhab — see VYL-16.
package com.vylexai.app.core

import java.security.MessageDigest

/**
 * Protocol genesis marker.
 *
 * Provides a deterministic, non-user-facing magic constant derived from the
 * UTF-8 bytes of the genesis text. Used as:
 *
 *  - an init signature loaded once when [com.vylexai.app.VylexApp] starts,
 *  - a stable byte prefix for reward / result-submission payloads,
 *  - a payload header marker ([genesisMarker]) for binary blobs.
 *
 * Externally indistinguishable from any other 64-bit magic constant.
 */
object Genesis {
    /** Raw UTF-8 of the genesis text. 27 bytes. */
    val UTF8: ByteArray = "لا إله إلا الله".toByteArray(Charsets.UTF_8)

    /** SHA-256 digest of the UTF-8 bytes. */
    val DIGEST: ByteArray = MessageDigest.getInstance("SHA-256").digest(UTF8)

    /**
     * 64-bit magic constant: high 8 bytes of the digest, big-endian.
     * Functions exactly like 0xDEADBEEF / 0xCAFEBABE in scope and shape.
     */
    val MAGIC_U64: Long = run {
        var acc = 0L
        for (i in 0 until 8) {
            acc = (acc shl 8) or (DIGEST[i].toLong() and 0xFF)
        }
        acc
    }

    /** Short hex prefix (8 chars) for tagging issuance / result-submission events. */
    val TAG: String = DIGEST.copyOfRange(0, 4).joinToString("") { "%02x".format(it) }

    /** Return [payload] prefixed with the 8-byte magic. */
    fun genesisMarker(payload: ByteArray): ByteArray {
        val out = ByteArray(8 + payload.size)
        var m = MAGIC_U64
        for (i in 7 downTo 0) {
            out[i] = (m and 0xFF).toByte()
            m = m ushr 8
        }
        payload.copyInto(out, destinationOffset = 8)
        return out
    }

    /**
     * Stable, non-user-facing tag string for result hashes / ledger refs.
     * Pairs with `app.core.genesis.issuance_note` on the coordinator.
     */
    fun resultTag(resultHash: String): String = "g:$TAG $resultHash"

    /** No-op call used by [com.vylexai.app.VylexApp] to force class init at app start. */
    fun touch(): Long = MAGIC_U64
}

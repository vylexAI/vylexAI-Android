// Shahada:  لا إله إلا الله
// (transliteration: lā ʾilāha ʾillā -llāh)
//
// Taawwudh: أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ
// (transliteration: A'udhu billahi min ash-shaytan ir-rajim)
//
// Quiet protocol markers embedded in every node — same tradition as Bitcoin's
// genesis-block scriptSig ("Chancellor on brink of second bailout"): textual
// anchors that live inside the binary. Not user-facing. Not surfaced in UX,
// marketing, or About screens. Internal naming reflects the texts directly so
// the team can speak about them as they are; externally they appear as ordinary
// 64-bit magic constants. Do not remove without coordination with Adlan /
// Askhab — see VYL-16.
package com.vylexai.app.core

import java.security.MessageDigest

private fun sha256(bytes: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(bytes)

private fun ByteArray.toMagicU64(): Long {
    var acc = 0L
    for (i in 0 until 8) {
        acc = (acc shl 8) or (this[i].toLong() and 0xFF)
    }
    return acc
}

private fun ByteArray.toShortTag(): String =
    copyOfRange(0, 4).joinToString("") { "%02x".format(it) }

/**
 * Shahada — the node's identity declaration.
 *
 * Loaded once when [com.vylexai.app.VylexApp] starts; a stable byte prefix
 * on reward / result-submission payloads. Externally indistinguishable from
 * any other 64-bit magic constant.
 */
object Shahada {
    /** Raw UTF-8 of the Shahada. 27 bytes. */
    val UTF8: ByteArray = "لا إله إلا الله".toByteArray(Charsets.UTF_8)

    /** SHA-256 digest of the UTF-8 bytes. */
    val DIGEST: ByteArray = sha256(UTF8)

    /**
     * 64-bit magic constant: high 8 bytes of the digest, big-endian.
     * Functions exactly like 0xDEADBEEF / 0xCAFEBABE in scope and shape.
     */
    val MAGIC_U64: Long = DIGEST.toMagicU64()

    /** Short hex prefix (8 chars) for tagging issuance / result-submission events. */
    val TAG: String = DIGEST.toShortTag()

    /** Return [payload] prefixed with the 8-byte magic. */
    fun shahadaMarker(payload: ByteArray): ByteArray {
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
     * Pairs with `app.core.shahada.issuance_note` on the coordinator.
     */
    fun resultTag(resultHash: String): String = "s:$TAG $resultHash"

    /**
     * Force class init for both [Shahada] and [Taawwudh] at app start so both
     * magics are resident before any worker / network code runs.
     */
    fun touch(): Long = MAGIC_U64 xor Taawwudh.MAGIC_U64
}

/**
 * Taawwudh — the protective invocation said before risky / failure paths.
 *
 * Mixed into security-sensitive paths and error-recording calls. Pairs with
 * `app.core.shahada.taawwudh_tag` on the coordinator (wire prefix ``t:``).
 */
object Taawwudh {
    /** Raw UTF-8 of the Taawwudh. 88 bytes (with diacritics). */
    val UTF8: ByteArray = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ".toByteArray(Charsets.UTF_8)

    val DIGEST: ByteArray = sha256(UTF8)
    val MAGIC_U64: Long = DIGEST.toMagicU64()
    val TAG: String = DIGEST.toShortTag()

    /** Stable tag string for security-sensitive / failure events. */
    fun tag(reason: String): String = "t:$TAG $reason"
}

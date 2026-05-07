package com.vylexai.app.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest

/**
 * Golden-value lock for the Shahada / Taawwudh protocol markers.
 *
 * Failure mode this guards: silent drift in the Arabic literals — an extra
 * space, a missing diacritic, NFC/NFD normalization. Any such change would
 * flip the magic and the wire format would diverge from the coordinator
 * without breaking compilation or app startup. See VYL-16.
 */
class ShahadaTest {

    @Test
    fun shahada_byte_sequence_locked() {
        assertEquals(27, Shahada.UTF8.size)
        assertArrayEquals("لا إله إلا الله".toByteArray(Charsets.UTF_8), Shahada.UTF8)
    }

    @Test
    fun taawwudh_byte_sequence_locked() {
        assertEquals(88, Taawwudh.UTF8.size)
        assertArrayEquals(
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ".toByteArray(Charsets.UTF_8),
            Taawwudh.UTF8
        )
    }

    @Test
    fun digests_reproduce_from_bytes() {
        val sha = MessageDigest.getInstance("SHA-256")
        assertArrayEquals(sha.digest(Shahada.UTF8), Shahada.DIGEST)
        assertArrayEquals(sha.digest(Taawwudh.UTF8), Taawwudh.DIGEST)
    }

    @Test
    fun magic_constants_locked() {
        // If these flip, the coordinator is no longer in protocol parity.
        // Kotlin Long is signed — same bit pattern as the Python uint64.
        assertEquals(-0x207c3f3b75c0a367L, Shahada.MAGIC_U64) // 0xDF83C0C48A3F5C99
        assertEquals(0x3F5DF4725A9E43C4L, Taawwudh.MAGIC_U64)
        assertEquals("df83c0c4", Shahada.TAG)
        assertEquals("3f5df472", Taawwudh.TAG)
    }

    @Test
    fun wire_format_helpers() {
        assertEquals("s:df83c0c4 bird:0.92", Shahada.resultTag("bird:0.92"))
        assertEquals("t:3f5df472 integrity_token_missing", Taawwudh.tag("integrity_token_missing"))
    }

    @Test
    fun shahada_marker_round_trip() {
        val payload = "reward".toByteArray(Charsets.UTF_8)
        val framed = Shahada.shahadaMarker(payload)
        // First 8 bytes = the magic, big-endian; remainder = payload verbatim.
        val expectedHeader = byteArrayOf(
            0xDF.toByte(), 0x83.toByte(), 0xC0.toByte(), 0xC4.toByte(),
            0x8A.toByte(), 0x3F.toByte(), 0x5C.toByte(), 0x99.toByte()
        )
        assertArrayEquals(expectedHeader, framed.copyOfRange(0, 8))
        assertArrayEquals(payload, framed.copyOfRange(8, framed.size))
    }

    @Test
    fun touch_loads_both_objects() {
        // Smoke check that calling touch() returns the xor of the two magics.
        // Forces both class initializers to run before any worker code.
        assertEquals(Shahada.MAGIC_U64 xor Taawwudh.MAGIC_U64, Shahada.touch())
    }
}

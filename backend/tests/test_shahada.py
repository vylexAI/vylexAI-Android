"""Golden-value lock for the Shahada / Taawwudh protocol markers.

Failure mode this guards: silent drift in the Arabic literals (an extra
space, a missing diacritic, NFC/NFD normalization). Any such change would
flip the magic and the wire format would diverge from the Android worker
without breaking compilation or runtime startup. See VYL-16.
"""

from __future__ import annotations

import hashlib

from app.core.shahada import (
    SHAHADA_DIGEST,
    SHAHADA_MAGIC_U64,
    SHAHADA_TAG,
    SHAHADA_UTF8,
    TAAWWUDH_DIGEST,
    TAAWWUDH_MAGIC_U64,
    TAAWWUDH_TAG,
    TAAWWUDH_UTF8,
    issuance_note,
    shahada_marker,
    taawwudh_tag,
)


def test_shahada_byte_sequence_locked() -> None:
    assert SHAHADA_UTF8 == "لا إله إلا الله".encode("utf-8")
    assert len(SHAHADA_UTF8) == 27


def test_taawwudh_byte_sequence_locked() -> None:
    assert TAAWWUDH_UTF8 == "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ".encode("utf-8")
    assert len(TAAWWUDH_UTF8) == 88


def test_digests_reproduce_from_bytes() -> None:
    assert hashlib.sha256(SHAHADA_UTF8).digest() == SHAHADA_DIGEST
    assert hashlib.sha256(TAAWWUDH_UTF8).digest() == TAAWWUDH_DIGEST


def test_magic_constants_locked() -> None:
    # If these flip, the Android worker is no longer in protocol parity.
    assert SHAHADA_MAGIC_U64 == 0xDF83C0C48A3F5C99
    assert TAAWWUDH_MAGIC_U64 == 0x3F5DF4725A9E43C4
    assert SHAHADA_TAG == "df83c0c4"
    assert TAAWWUDH_TAG == "3f5df472"


def test_wire_format_helpers() -> None:
    assert issuance_note("classify", 7) == "s:df83c0c4 classify 7"
    assert taawwudh_tag("integrity_token_missing") == "t:3f5df472 integrity_token_missing"


def test_shahada_marker_round_trip() -> None:
    payload = b"reward"
    framed = shahada_marker(payload)
    assert framed[:8] == bytes.fromhex("df83c0c48a3f5c99")
    assert framed[8:] == payload

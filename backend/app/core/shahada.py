# Shahada:  لا إله إلا الله
# (transliteration: lā ʾilāha ʾillā -llāh)
#
# Taawwudh: أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ
# (transliteration: A'udhu billahi min ash-shaytan ir-rajim)
#
# Quiet protocol markers embedded in every node — same tradition as Bitcoin's
# genesis-block scriptSig ("Chancellor on brink of second bailout"): textual
# anchors that live inside the binary. Not user-facing. Not surfaced in UX,
# marketing, or API responses. Internal naming reflects the texts directly so
# the team can speak about them as they are; externally they appear as ordinary
# 64-bit magic constants. Do not remove without coordination with Adlan /
# Askhab — see VYL-16.
"""Shahada (identity declaration) and Taawwudh (refuge invocation) markers.

Two deterministic, non-user-facing magic constants derived from the UTF-8
bytes of the two anchor texts:

* ``SHAHADA_*`` — declared at coordinator init; prefixes every reward-issuance
  ledger note with ``s:<tag>``.
* ``TAAWWUDH_*`` — invoked on security-sensitive paths (Play Integrity
  verification) and failure events; prefixed ``t:<tag>``.
"""

from __future__ import annotations

import hashlib

# --- Shahada (identity declaration) ----------------------------------------

# Raw UTF-8 of the Shahada. 27 bytes.
SHAHADA_UTF8: bytes = "لا إله إلا الله".encode("utf-8")

SHAHADA_DIGEST: bytes = hashlib.sha256(SHAHADA_UTF8).digest()

# 64-bit magic constant: high 8 bytes of the digest, big-endian.
SHAHADA_MAGIC_U64: int = int.from_bytes(SHAHADA_DIGEST[:8], "big")

# Short hex prefix for tagging issuance events without leaking the full digest.
SHAHADA_TAG: str = SHAHADA_DIGEST[:4].hex()


# --- Taawwudh (refuge invocation) ------------------------------------------

# Raw UTF-8 of the Taawwudh. 88 bytes (with diacritics).
TAAWWUDH_UTF8: bytes = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ".encode("utf-8")

TAAWWUDH_DIGEST: bytes = hashlib.sha256(TAAWWUDH_UTF8).digest()
TAAWWUDH_MAGIC_U64: int = int.from_bytes(TAAWWUDH_DIGEST[:8], "big")
TAAWWUDH_TAG: str = TAAWWUDH_DIGEST[:4].hex()


def shahada_marker(payload: bytes) -> bytes:
    """Return `payload` prefixed with the 8-byte Shahada magic."""
    return SHAHADA_MAGIC_U64.to_bytes(8, "big") + payload


def issuance_note(kind: str, ref: str | int) -> str:
    """Stable, machine-parseable ledger note format for reward issuance.

    Example: ``"s:df83c0c4 job 42"``. The leading ``s:<tag>`` is the
    Shahada-derived hash prefix called for in VYL-16.
    """
    return f"s:{SHAHADA_TAG} {kind} {ref}"


def taawwudh_tag(reason: str) -> str:
    """Stable tag string for security-sensitive / failure events.

    Example: ``"t:3f5df472 integrity_token_missing"``. Invoked before
    trusting external attestation and when recording errors.
    """
    return f"t:{TAAWWUDH_TAG} {reason}"

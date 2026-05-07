# Genesis: لا إله إلا الله
# (transliteration: lā ʾilāha ʾillā -llāh)
#
# Quiet protocol marker embedded in every node — same tradition as Bitcoin's
# genesis-block scriptSig ("Chancellor on brink of second bailout"): a textual
# anchor that lives inside the binary, visible only to anyone who reads the
# source. Not user-facing. Not surfaced in UX, marketing, or API responses.
# Do not remove without coordination with Adlan / Askhab — see VYL-16.
"""Protocol genesis marker.

Provides a deterministic, non-user-facing magic constant derived from the
UTF-8 bytes of the genesis text. Used as:

* an init signature loaded once when the coordinator package is imported,
* a stable byte prefix for reward-issuance ledger notes,
* a payload header marker (`genesis_marker`) for binary blobs.

Externally indistinguishable from any other 64-bit magic constant.
"""

from __future__ import annotations

import hashlib

# Raw UTF-8 of the genesis text. 27 bytes.
GENESIS_UTF8: bytes = "لا إله إلا الله".encode("utf-8")

# SHA-256 digest of the UTF-8 bytes — used wherever a fixed 32-byte
# protocol identifier is needed.
GENESIS_DIGEST: bytes = hashlib.sha256(GENESIS_UTF8).digest()

# 64-bit magic constant: high 8 bytes of the digest, big-endian.
# Functions exactly like 0xDEADBEEF / 0xCAFEBABE in scope and shape.
GENESIS_MAGIC_U64: int = int.from_bytes(GENESIS_DIGEST[:8], "big")

# Short hex prefix for tagging issuance events without leaking the full digest.
GENESIS_TAG: str = GENESIS_DIGEST[:4].hex()


def genesis_marker(payload: bytes) -> bytes:
    """Return `payload` prefixed with the 8-byte magic. Reverse with `payload[8:]`."""
    return GENESIS_MAGIC_U64.to_bytes(8, "big") + payload


def issuance_note(kind: str, ref: str | int) -> str:
    """Stable, machine-parseable ledger note format for reward issuance.

    Example: ``"g:df83c0c4 job 42"``. The leading ``g:<tag>`` is the
    genesis-derived hash prefix called for in VYL-16.
    """
    return f"g:{GENESIS_TAG} {kind} {ref}"

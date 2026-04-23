"""Worker heartbeat + Play Integrity device attestation (stubbed verification in MVP)."""

from __future__ import annotations

from datetime import UTC, datetime

from fastapi import APIRouter
from sqlalchemy import select

from app.deps import CurrentUser, DbSession
from app.models import Device
from app.schemas import HeartbeatIn, HeartbeatOut

router = APIRouter(prefix="/worker", tags=["worker"])


async def _verify_play_integrity(token: str | None) -> bool:
    """Real verification will call Play Integrity `decodeIntegrityToken`.

    For MVP we accept any non-empty token. When `PLAY_INTEGRITY_PROJECT_NUMBER`
    is set we'll wire the real verifier.
    """
    return bool(token)


@router.post("/heartbeat", response_model=HeartbeatOut)
async def heartbeat(body: HeartbeatIn, db: DbSession, user: CurrentUser) -> HeartbeatOut:
    device = await db.scalar(
        select(Device).where(Device.user_id == user.id, Device.device_id == body.device_id)
    )
    if device is None:
        # lazy auto-register to not block a provider's first beat
        device = Device(user_id=user.id, device_id=body.device_id)
        db.add(device)

    integrity_ok = await _verify_play_integrity(body.integrity_token)

    now = datetime.now(UTC)
    device.last_heartbeat_at = now
    if integrity_ok:
        device.integrity_verified_at = now
    await db.commit()

    return HeartbeatOut(
        accepted=True,
        integrity_ok=integrity_ok,
        next_poll_ms=60_000,
    )

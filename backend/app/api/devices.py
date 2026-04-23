from fastapi import APIRouter
from sqlalchemy import select

from app.deps import CurrentUser, DbSession
from app.models import Device
from app.schemas import DeviceProfileIn, DeviceProfileOut

router = APIRouter(prefix="/device", tags=["device"])


def _recommend(score: int) -> list[str]:
    """Map performance score → a curated list of task types the phone can handle."""
    if score >= 700:
        return ["image_classification", "object_detection", "ocr", "nlp_inference", "fine_tuning"]
    if score >= 400:
        return ["image_classification", "ocr", "nlp_inference"]
    return ["image_classification", "moderation"]


def _estimate_bsai(score: int) -> tuple[float, float]:
    base = max(1, score) / 1000
    return round(base * 10, 2), round(base * 50, 2)


@router.put("/profile", response_model=DeviceProfileOut)
async def upsert_profile(body: DeviceProfileIn, db: DbSession, user: CurrentUser) -> DeviceProfileOut:
    device = await db.scalar(
        select(Device).where(Device.user_id == user.id, Device.device_id == body.device_id)
    )
    if device is None:
        device = Device(user_id=user.id, device_id=body.device_id)
        db.add(device)
    device.model = body.model
    device.android_sdk = body.android_sdk
    device.profile = body.profile
    device.performance_score = body.performance_score
    await db.commit()
    await db.refresh(device)
    return DeviceProfileOut(
        id=device.id,
        device_id=device.device_id,
        model=device.model,
        android_sdk=device.android_sdk,
        performance_score=device.performance_score,
        profile=device.profile,
        recommended_tasks=_recommend(device.performance_score),
        estimated_monthly_bsai=_estimate_bsai(device.performance_score),
    )


@router.get("/profile", response_model=list[DeviceProfileOut])
async def list_profiles(db: DbSession, user: CurrentUser) -> list[DeviceProfileOut]:
    result = await db.scalars(select(Device).where(Device.user_id == user.id))
    return [
        DeviceProfileOut(
            id=d.id,
            device_id=d.device_id,
            model=d.model,
            android_sdk=d.android_sdk,
            performance_score=d.performance_score,
            profile=d.profile,
            recommended_tasks=_recommend(d.performance_score),
            estimated_monthly_bsai=_estimate_bsai(d.performance_score),
        )
        for d in result.all()
    ]

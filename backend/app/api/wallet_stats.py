from decimal import Decimal

from fastapi import APIRouter
from sqlalchemy import func, select

from app.deps import CurrentUser, DbSession
from app.models import LedgerEntry, Task, TaskState
from app.schemas import UserStatsOut, WalletOut

router = APIRouter(tags=["wallet+stats"])


@router.get("/wallet/balance", response_model=WalletOut)
async def wallet_balance(db: DbSession, user: CurrentUser) -> WalletOut:
    credited = await db.scalar(
        select(func.coalesce(func.sum(LedgerEntry.amount_bsai), 0)).where(
            LedgerEntry.user_id == user.id, LedgerEntry.amount_bsai > 0
        )
    )
    debited = await db.scalar(
        select(func.coalesce(func.sum(LedgerEntry.amount_bsai), 0)).where(
            LedgerEntry.user_id == user.id, LedgerEntry.amount_bsai < 0
        )
    )
    total = (credited or Decimal(0)) + (debited or Decimal(0))
    return WalletOut(
        balance_bsai=total,
        total_credited_bsai=credited or Decimal(0),
        total_debited_bsai=-(debited or Decimal(0)),
    )


@router.get("/user/stats", response_model=UserStatsOut)
async def user_stats(db: DbSession, user: CurrentUser) -> UserStatsOut:
    from app.models import Device

    device_ids = (
        await db.scalars(select(Device.id).where(Device.user_id == user.id))
    ).all()
    completed = 0
    if device_ids:
        completed = await db.scalar(
            select(func.count(Task.id)).where(
                Task.device_id.in_(device_ids), Task.state == TaskState.ACCEPTED
            )
        ) or 0

    credited = await db.scalar(
        select(func.coalesce(func.sum(LedgerEntry.amount_bsai), 0)).where(
            LedgerEntry.user_id == user.id, LedgerEntry.amount_bsai > 0
        )
    ) or Decimal(0)
    debited = await db.scalar(
        select(func.coalesce(func.sum(LedgerEntry.amount_bsai), 0)).where(
            LedgerEntry.user_id == user.id, LedgerEntry.amount_bsai < 0
        )
    ) or Decimal(0)

    # MVP contribution score: 0..100, log-curve on tasks completed.
    import math

    score = 0 if completed == 0 else min(100, int(round(20 * math.log2(1 + completed))))

    return UserStatsOut(
        tasks_completed=completed,
        bsai_earned=credited,
        bsai_spent=-debited,
        contribution_score=score,
    )

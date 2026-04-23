from decimal import Decimal

from fastapi import APIRouter, HTTPException
from sqlalchemy import select

from app.deps import CurrentUser, DbSession
from app.models import Job, LedgerEntry, Task
from app.schemas import JobCreateIn, JobOut

router = APIRouter(prefix="/jobs", tags=["jobs"])

# Simple MVP pricing. Real pricing = dynamic on-chain.
BASE_UNIT_COST_BSAI = Decimal("0.002")


async def _user_balance(db, user_id) -> Decimal:
    from sqlalchemy import func

    total = await db.scalar(
        select(func.coalesce(func.sum(LedgerEntry.amount_bsai), 0)).where(
            LedgerEntry.user_id == user_id
        )
    )
    return Decimal(total or 0)


@router.post("", response_model=JobOut, status_code=201)
async def create_job(body: JobCreateIn, db: DbSession, user: CurrentUser) -> JobOut:
    n_units = len(body.input_refs) * body.replication
    cost = Decimal(n_units) * BASE_UNIT_COST_BSAI

    balance = await _user_balance(db, user.id)
    if balance < cost:
        raise HTTPException(status_code=402, detail="insufficient_bsai")

    job = Job(
        client_user_id=user.id,
        task_type=body.task_type,
        model_ref=body.model_ref,
        params=body.params,
        replication=body.replication,
        reward_bsai=cost,
    )
    db.add(job)
    await db.flush()

    for input_ref in body.input_refs:
        for _ in range(body.replication):
            db.add(
                Task(
                    job_id=job.id,
                    input_ref=input_ref,
                    reward_bsai=BASE_UNIT_COST_BSAI,
                )
            )

    db.add(
        LedgerEntry(
            user_id=user.id,
            amount_bsai=-cost,
            kind="payment",
            ref_job_id=job.id,
            note=f"job {body.task_type}",
        )
    )
    await db.commit()
    await db.refresh(job)
    return JobOut(
        id=job.id,
        task_type=job.task_type,
        model_ref=job.model_ref,
        state=job.state,
        replication=job.replication,
        reward_bsai=job.reward_bsai,
        created_at=job.created_at,
        completed_at=job.completed_at,
    )


@router.get("/status", response_model=list[JobOut])
async def list_jobs(db: DbSession, user: CurrentUser) -> list[JobOut]:
    result = await db.scalars(
        select(Job).where(Job.client_user_id == user.id).order_by(Job.created_at.desc())
    )
    return [
        JobOut(
            id=j.id,
            task_type=j.task_type,
            model_ref=j.model_ref,
            state=j.state,
            replication=j.replication,
            reward_bsai=j.reward_bsai,
            created_at=j.created_at,
            completed_at=j.completed_at,
        )
        for j in result.all()
    ]

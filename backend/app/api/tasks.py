"""Task distribution + result collection with N-way redundancy.

Flow:
  provider GET /tasks/next  → atomically claim the highest-priority pending Task for this device
  provider POST /tasks/result → submit result hash; if the job has enough submissions, run quorum.

Quorum rule: majority of result_hashes wins. Matching providers get paid (ledger credit),
losing providers are rejected (no payment, no slash in MVP).
"""

from __future__ import annotations

import uuid
from collections import Counter
from datetime import UTC, datetime

from fastapi import APIRouter, HTTPException, Query
from sqlalchemy import select, update

from app.deps import CurrentUser, DbSession
from app.models import Device, Job, JobState, LedgerEntry, Task, TaskState
from app.schemas import TaskOut, TaskResultIn

router = APIRouter(prefix="/tasks", tags=["tasks"])


@router.get("/next", response_model=TaskOut | None)
async def next_task(
    db: DbSession,
    user: CurrentUser,
    device_id: str = Query(min_length=8),
) -> TaskOut | None:
    device = await db.scalar(
        select(Device).where(Device.user_id == user.id, Device.device_id == device_id)
    )
    if device is None:
        raise HTTPException(status_code=404, detail="device_not_registered")

    # Atomically claim the oldest pending task via SKIP LOCKED-style UPDATE ... RETURNING.
    pending_id = await db.scalar(
        select(Task.id).where(Task.state == TaskState.PENDING).order_by(Task.created_at).limit(1)
    )
    if pending_id is None:
        return None

    await db.execute(
        update(Task)
        .where(Task.id == pending_id, Task.state == TaskState.PENDING)
        .values(state=TaskState.ASSIGNED, device_id=device.id)
    )
    task = await db.get(Task, pending_id)
    if task is None:  # raced against another node
        return None
    job = await db.get(Job, task.job_id)
    if job is None:
        raise HTTPException(status_code=500, detail="orphan_task")
    await db.commit()

    return TaskOut(
        task_id=task.id,
        job_id=task.job_id,
        task_type=job.task_type,
        model_ref=job.model_ref,
        input_ref=task.input_ref,
        deadline=task.deadline,
        reward_bsai=task.reward_bsai,
    )


async def _maybe_finalize_job(db, job_id: uuid.UUID) -> None:
    """Once all tasks for a job are submitted, pick the majority hash and settle."""
    tasks = (
        await db.scalars(select(Task).where(Task.job_id == job_id))
    ).all()
    if not tasks or any(t.state in (TaskState.PENDING, TaskState.ASSIGNED) for t in tasks):
        return  # still waiting

    hashes = [t.result_hash for t in tasks if t.result_hash]
    if not hashes:
        return
    winner, _ = Counter(hashes).most_common(1)[0]

    for task in tasks:
        if task.result_hash == winner:
            task.state = TaskState.ACCEPTED
            # credit the provider
            device = await db.get(Device, task.device_id) if task.device_id else None
            if device is not None:
                db.add(
                    LedgerEntry(
                        user_id=device.user_id,
                        amount_bsai=task.reward_bsai,
                        kind="reward",
                        ref_task_id=task.id,
                        ref_job_id=task.job_id,
                        note="task accepted",
                    )
                )
        elif task.state == TaskState.SUBMITTED:
            task.state = TaskState.REJECTED

    job = await db.get(Job, job_id)
    if job is not None:
        job.state = JobState.DONE
        job.completed_at = datetime.now(UTC)


@router.post("/result")
async def submit_result(body: TaskResultIn, db: DbSession, user: CurrentUser) -> dict[str, str]:
    task = await db.get(Task, body.task_id)
    if task is None:
        raise HTTPException(status_code=404, detail="task_not_found")
    if task.state != TaskState.ASSIGNED:
        raise HTTPException(status_code=409, detail="task_not_assigned")

    # ownership check: the device on the task must belong to this user
    device = await db.get(Device, task.device_id) if task.device_id else None
    if device is None or device.user_id != user.id:
        raise HTTPException(status_code=403, detail="not_your_task")

    task.output_ref = body.output_ref
    task.result_hash = body.result_hash
    task.exec_time_ms = body.exec_time_ms
    task.state = TaskState.SUBMITTED
    task.submitted_at = datetime.now(UTC)

    await _maybe_finalize_job(db, task.job_id)
    await db.commit()
    return {"status": "accepted"}

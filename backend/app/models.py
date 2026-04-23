"""SQLAlchemy models for the VylexAI coordinator.

Entity map:
  User        — custodial account (email/password). Holds BSAI balance.
  Device      — a phone bound to a user; carries the performance profile.
  Job         — a task submitted by a client; pays BSAI to the network.
  Task        — a unit of work for a provider node (fan-out of a Job with N-way redundancy).
  LedgerEntry — every BSAI movement (credit/debit) — single source of truth for balances.
"""

from __future__ import annotations

import uuid
from datetime import UTC, datetime
from enum import StrEnum

from sqlalchemy import (
    DateTime,
    ForeignKey,
    Integer,
    Numeric,
    String,
    Text,
    UniqueConstraint,
    func,
)
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


def _uuid() -> uuid.UUID:
    return uuid.uuid4()


def _now() -> datetime:
    return datetime.now(UTC)


class JobState(StrEnum):
    PENDING = "pending"
    RUNNING = "running"
    DONE = "done"
    FAILED = "failed"


class TaskState(StrEnum):
    PENDING = "pending"        # created, not yet assigned
    ASSIGNED = "assigned"      # claimed by a provider
    SUBMITTED = "submitted"    # result received, awaiting quorum
    ACCEPTED = "accepted"      # part of the majority — provider gets paid
    REJECTED = "rejected"      # minority / failed verification


class User(Base):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=_uuid)
    email: Mapped[str] = mapped_column(String(320), unique=True, index=True)
    password_hash: Mapped[str] = mapped_column(String(255))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, server_default=func.now())

    devices: Mapped[list[Device]] = relationship(back_populates="user", cascade="all,delete")


class Device(Base):
    __tablename__ = "devices"
    __table_args__ = (UniqueConstraint("user_id", "device_id", name="uq_user_device"),)

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=_uuid)
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    device_id: Mapped[str] = mapped_column(String(128))  # stable client-side fingerprint
    model: Mapped[str | None] = mapped_column(String(128))
    android_sdk: Mapped[int | None] = mapped_column(Integer)
    profile: Mapped[dict] = mapped_column(JSONB, default=dict)
    performance_score: Mapped[int] = mapped_column(Integer, default=0)
    last_heartbeat_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    integrity_verified_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, server_default=func.now())

    user: Mapped[User] = relationship(back_populates="devices")


class Job(Base):
    __tablename__ = "jobs"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=_uuid)
    client_user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("users.id"), index=True)
    task_type: Mapped[str] = mapped_column(String(64))
    model_ref: Mapped[str] = mapped_column(String(255))
    params: Mapped[dict] = mapped_column(JSONB, default=dict)
    replication: Mapped[int] = mapped_column(Integer, default=3)
    reward_bsai: Mapped[float] = mapped_column(Numeric(18, 8))
    state: Mapped[str] = mapped_column(String(16), default=JobState.PENDING)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, server_default=func.now())
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)


class Task(Base):
    __tablename__ = "tasks"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=_uuid)
    job_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("jobs.id", ondelete="CASCADE"), index=True)
    device_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("devices.id"), nullable=True, index=True
    )
    input_ref: Mapped[str] = mapped_column(Text)           # URL or inline payload
    output_ref: Mapped[str | None] = mapped_column(Text, nullable=True)
    result_hash: Mapped[str | None] = mapped_column(String(128), nullable=True)
    exec_time_ms: Mapped[int | None] = mapped_column(Integer, nullable=True)
    reward_bsai: Mapped[float] = mapped_column(Numeric(18, 8))
    state: Mapped[str] = mapped_column(String(16), default=TaskState.PENDING, index=True)
    deadline: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, server_default=func.now())
    submitted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)


class LedgerEntry(Base):
    """Every BSAI movement. Balance is computed by summing a user's entries."""

    __tablename__ = "ledger_entries"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=_uuid)
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("users.id"), index=True)
    amount_bsai: Mapped[float] = mapped_column(Numeric(18, 8))   # positive = credit, negative = debit
    kind: Mapped[str] = mapped_column(String(32))                 # reward | payment | adjustment
    ref_task_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("tasks.id"), nullable=True
    )
    ref_job_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("jobs.id"), nullable=True)
    note: Mapped[str | None] = mapped_column(String(255), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now, server_default=func.now())

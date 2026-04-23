"""Pydantic request/response schemas.

Intentionally minimal — Adlan said «не надо перегружать MVP, делай минимально жизнеспособно».
"""

from __future__ import annotations

import uuid
from datetime import datetime
from decimal import Decimal
from typing import Any

from pydantic import BaseModel, EmailStr, Field


class RegisterIn(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=256)


class LoginIn(BaseModel):
    email: EmailStr
    password: str


class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int


class DeviceProfileIn(BaseModel):
    device_id: str = Field(min_length=8, max_length=128)
    model: str | None = None
    android_sdk: int | None = None
    profile: dict[str, Any] = Field(default_factory=dict)
    performance_score: int = Field(ge=0, le=1000, default=0)


class DeviceProfileOut(BaseModel):
    id: uuid.UUID
    device_id: str
    model: str | None
    android_sdk: int | None
    performance_score: int
    profile: dict[str, Any]
    recommended_tasks: list[str]
    estimated_monthly_bsai: tuple[float, float]


class TaskOut(BaseModel):
    """Shape of a task delivered to a provider node."""

    task_id: uuid.UUID
    job_id: uuid.UUID
    task_type: str
    model_ref: str
    input_ref: str
    deadline: datetime | None
    reward_bsai: Decimal


class TaskResultIn(BaseModel):
    task_id: uuid.UUID
    output_ref: str | None = None
    inline_output: dict[str, Any] | None = None
    result_hash: str
    exec_time_ms: int = Field(ge=0)
    integrity_token: str | None = None


class JobCreateIn(BaseModel):
    task_type: str
    model_ref: str
    input_refs: list[str]
    params: dict[str, Any] = Field(default_factory=dict)
    replication: int = Field(ge=1, le=7, default=3)


class JobOut(BaseModel):
    id: uuid.UUID
    task_type: str
    model_ref: str
    state: str
    replication: int
    reward_bsai: Decimal
    created_at: datetime
    completed_at: datetime | None


class UserStatsOut(BaseModel):
    tasks_completed: int
    bsai_earned: Decimal
    bsai_spent: Decimal
    contribution_score: int  # 0..100


class WalletOut(BaseModel):
    balance_bsai: Decimal
    total_credited_bsai: Decimal
    total_debited_bsai: Decimal


class HeartbeatIn(BaseModel):
    device_id: str
    battery_pct: int | None = Field(default=None, ge=0, le=100)
    temp_c: float | None = None
    is_charging: bool | None = None
    network_type: str | None = None
    integrity_token: str | None = None


class HeartbeatOut(BaseModel):
    accepted: bool
    integrity_ok: bool
    next_poll_ms: int

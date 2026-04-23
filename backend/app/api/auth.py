from fastapi import APIRouter, HTTPException, status
from sqlalchemy import select

from app.core.config import settings
from app.core.security import create_access_token, hash_password, verify_password
from app.deps import DbSession
from app.models import User
from app.schemas import LoginIn, RegisterIn, TokenOut

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=TokenOut, status_code=status.HTTP_201_CREATED)
async def register(body: RegisterIn, db: DbSession) -> TokenOut:
    existing = await db.scalar(select(User).where(User.email == body.email))
    if existing:
        raise HTTPException(status_code=409, detail="email_taken")
    user = User(email=body.email, password_hash=hash_password(body.password))
    db.add(user)
    await db.commit()
    await db.refresh(user)
    token = create_access_token(str(user.id))
    return TokenOut(access_token=token, expires_in=settings.jwt_expires_minutes * 60)


@router.post("/login", response_model=TokenOut)
async def login(body: LoginIn, db: DbSession) -> TokenOut:
    user = await db.scalar(select(User).where(User.email == body.email))
    if not user or not verify_password(body.password, user.password_hash):
        raise HTTPException(status_code=401, detail="invalid_credentials")
    token = create_access_token(str(user.id))
    return TokenOut(access_token=token, expires_in=settings.jwt_expires_minutes * 60)

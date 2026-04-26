from decimal import Decimal

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str = "postgresql+asyncpg://vylex:vylex@localhost:5432/vylex"
    jwt_secret: str = "change-me-in-prod"
    jwt_expires_minutes: int = 60 * 24
    jwt_algorithm: str = "HS256"
    play_integrity_project_number: str | None = None
    play_integrity_package: str = "com.vylexai.app"
    env: str = "dev"

    # BSAI tokenomics — see docs/bsai-economics.md
    bsai_decimals: int = 8
    bsai_hard_cap: Decimal = Decimal("21000000")
    bsai_genesis_reserve: Decimal = Decimal("1100000")
    bsai_genesis_wallet_count: int = 100
    bsai_base_unit_cost: Decimal = Decimal("0.002")

    # Transactional email (Resend). Empty key → email path is a no-op
    # in dev / Technical Preview; /auth/register still returns the JWT.
    resend_api_key: str | None = None
    resend_from_email: str = "hello@vylexai.com"

    # Backups: Backblaze B2 (set on the host once Adlan provides keys).
    b2_key_id: str | None = None
    b2_application_key: str | None = None
    b2_bucket: str = "vylexai-backups"
    b2_region: str = "eu-central-003"

    # Crash / error tracking (Sentry). Empty DSN → no events sent.
    sentry_dsn: str | None = None
    sentry_traces_sample_rate: float = 0.0


settings = Settings()

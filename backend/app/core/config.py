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


settings = Settings()

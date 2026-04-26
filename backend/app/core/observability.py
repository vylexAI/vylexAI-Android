"""Sentry initialization.

No-op when `settings.sentry_dsn` is unset. Called once from
`app/main.py` *before* the FastAPI app is constructed so the SDK can
auto-instrument the ASGI middleware.
"""

from __future__ import annotations

import logging

from app.core.config import settings

log = logging.getLogger(__name__)


def init_sentry() -> None:
    if not settings.sentry_dsn:
        log.debug("sentry: no DSN, skipping init")
        return
    try:
        import sentry_sdk
        from sentry_sdk.integrations.asgi import SentryAsgiMiddleware  # noqa: F401
        from sentry_sdk.integrations.fastapi import FastApiIntegration
        from sentry_sdk.integrations.sqlalchemy import SqlalchemyIntegration
        from sentry_sdk.integrations.starlette import StarletteIntegration

        sentry_sdk.init(
            dsn=settings.sentry_dsn,
            environment=settings.env,
            traces_sample_rate=settings.sentry_traces_sample_rate,
            send_default_pii=False,
            integrations=[
                StarletteIntegration(transaction_style="endpoint"),
                FastApiIntegration(transaction_style="endpoint"),
                SqlalchemyIntegration(),
            ],
        )
        log.info("sentry: initialized for env=%s", settings.env)
    except ImportError:
        log.warning("sentry: sentry-sdk not installed; skipping (add to pyproject.toml)")
    except Exception as exc:  # noqa: BLE001
        log.warning("sentry: init failed: %s", exc)

"""Transactional email via Resend.

No-op when `settings.resend_api_key` is unset (dev / Technical Preview).
The send is fire-and-forget — it never blocks or raises out of the
caller's request path. A failed send is logged but does not affect the
HTTP response, because email delivery is not on the critical path of
account creation.
"""

from __future__ import annotations

import asyncio
import logging
from typing import Any

import httpx

from app.core.config import settings

log = logging.getLogger(__name__)

_RESEND_URL = "https://api.resend.com/emails"


async def _post(payload: dict[str, Any]) -> None:
    if not settings.resend_api_key:
        log.debug("resend: no key, skipping send to %s", payload.get("to"))
        return
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            r = await client.post(
                _RESEND_URL,
                headers={
                    "Authorization": f"Bearer {settings.resend_api_key}",
                    "Content-Type": "application/json",
                },
                json=payload,
            )
            if r.status_code >= 400:
                log.warning("resend: %s — %s", r.status_code, r.text[:200])
            else:
                log.info("resend: queued message_id=%s", r.json().get("id"))
    except Exception as exc:  # noqa: BLE001
        log.warning("resend: send failed: %s", exc)


def send_email_background(*, to: str, subject: str, html: str) -> None:
    """Fire-and-forget email send. Never blocks the caller."""
    payload = {
        "from": f"VylexAI <{settings.resend_from_email}>",
        "to": [to],
        "subject": subject,
        "html": html,
    }
    asyncio.create_task(_post(payload))


WELCOME_HTML = """\
<!doctype html>
<html lang="en">
<body style="margin:0;padding:0;background:#0A0F18;font-family:-apple-system,Helvetica,Arial,sans-serif;color:#F3F6FB;">
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#0A0F18;">
    <tr><td align="center" style="padding:40px 20px;">
      <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background:#111826;border:1px solid #283249;border-radius:16px;">
        <tr><td style="padding:36px 36px 24px;">
          <div style="font-size:11px;letter-spacing:0.22em;text-transform:uppercase;color:#FFD166;margin-bottom:14px;">VylexAI</div>
          <h1 style="margin:0 0 12px;font-size:24px;line-height:1.2;color:#F3F6FB;">Welcome to the network.</h1>
          <p style="margin:0 0 16px;color:#B8C3D9;line-height:1.55;">
            Your VylexAI account is live. Open the Android app, switch on Provider Mode while charging on Wi-Fi, and your phone joins a global decentralized AI compute network.
          </p>
          <p style="margin:0 0 16px;color:#B8C3D9;line-height:1.55;">
            Every verified AI task and storage contribution earns you BSAI — a fixed-supply network unit (21,000,000 hard cap). During Technical Preview the BSAI balance is simulated; redeemability rolls out once our regulatory framework is in place.
          </p>
          <p style="margin:24px 0 0;">
            <a href="https://vylexai.com" style="display:inline-block;background:#1AC8FF;color:#0A0F18;text-decoration:none;padding:12px 22px;border-radius:999px;font-weight:600;">Open vylexai.com</a>
          </p>
        </td></tr>
        <tr><td style="padding:20px 36px 28px;border-top:1px solid #283249;color:#7B8AA5;font-size:12px;line-height:1.6;">
          You're receiving this because you registered for VylexAI. If this wasn't you, just ignore — no account is active without confirmation.<br/>
          Dudaev Systems UG · Cottbus, Germany · <a href="https://vylexai.com/en/impressum" style="color:#7B8AA5;">Impressum</a>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body></html>
"""


def send_welcome(to: str) -> None:
    send_email_background(
        to=to,
        subject="Welcome to VylexAI",
        html=WELCOME_HTML,
    )

# vylexai-coordinator

> **VylexAI coordinator** — routes AI tasks across the distributed smartphone network, verifies results via N-way redundancy + Play Integrity, and settles custodial **BSAI** rewards.

Backend for the [VylexAI Android app](https://github.com/vylexAI/vylexAI-Android).

## Stack

- **FastAPI** (Python 3.12)
- **PostgreSQL 16** (via SQLAlchemy 2 async + asyncpg)
- **Alembic** for migrations
- **python-jose** for JWT
- **Google Play Integrity API** for device attestation

## Endpoints (MVP)

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/auth/register` | Create account |
| `POST` | `/auth/login` | JWT login |
| `GET` | `/device/profile` | Device perf score + task recs |
| `GET` | `/tasks/next` | Provider pulls next assigned task |
| `POST` | `/tasks/result` | Provider submits result; N-way redundancy verification |
| `POST` | `/jobs/create` | Client submits a task (cost deducted in BSAI) |
| `GET` | `/jobs/status` | Client polls job progress |
| `GET` | `/user/stats` | Earnings, tasks completed, uptime |
| `GET` | `/wallet/balance` | Custodial BSAI balance |
| `POST` | `/worker/heartbeat` | Keepalive + Play Integrity attestation |

## Dev

```bash
docker compose up -d postgres
uv sync
uv run alembic upgrade head
uv run fastapi dev app/main.py
```

Open <http://localhost:8000/docs>.

## License

Apache-2.0

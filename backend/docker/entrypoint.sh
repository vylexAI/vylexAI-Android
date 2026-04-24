#!/usr/bin/env sh
# Run pending migrations, then hand off to the CMD (uvicorn).
set -eu

echo "[entrypoint] running alembic migrations…"
alembic upgrade head

echo "[entrypoint] starting: $*"
exec "$@"

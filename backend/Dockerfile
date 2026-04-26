# syntax=docker/dockerfile:1.7
# VylexAI coordinator — production image.
#   • Multi-stage so the runtime layer has no build tools and a smaller
#     attack surface.
#   • Non-root runtime user.
#   • HEALTHCHECK hits /health.
#   • Entrypoint runs pending Alembic migrations then uvicorn.

FROM python:3.12-slim AS builder

ENV UV_LINK_MODE=copy \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PYTHONDONTWRITEBYTECODE=1

RUN pip install --no-cache-dir uv==0.10.0

WORKDIR /build
# `uv pip install .` resolves the local package, so the package directories
# declared in pyproject.toml (app, alembic) and the README must exist in the
# build context — copying just pyproject.toml fails with "package directory
# 'app' does not exist".
COPY pyproject.toml README.md ./
COPY app ./app
COPY alembic ./alembic
RUN uv pip install --system --no-cache .


FROM python:3.12-slim AS runtime

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PORT=8000

RUN groupadd --system --gid 1001 vylex \
 && useradd --system --uid 1001 --gid 1001 --home-dir /app --no-create-home vylex

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

COPY app ./app
COPY alembic.ini ./alembic.ini
COPY alembic ./alembic
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod 755 /entrypoint.sh \
 && chown -R vylex:vylex /app

USER vylex

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request, sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:8000/health', timeout=3).status == 200 else 1)"

ENTRYPOINT ["/entrypoint.sh"]
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers", "--forwarded-allow-ips=*"]

#!/usr/bin/env bash
# Daily Postgres backup → Backblaze B2.
# Runs from a systemd timer on the Hetzner host. Reads B2 + Postgres
# credentials from /etc/vylex/coordinator.env (the same env file Compose
# uses for the running stack).
#
# Strategy:
#   1. pg_dumpall (custom format) from inside the postgres container.
#   2. gzip and timestamp the dump.
#   3. Upload to B2 at vylexai-backups/coordinator/YYYY/MM/DD/.
#   4. Delete dumps older than RETENTION_DAYS from the host.
#   5. Lifecycle policy on the B2 bucket handles long-term cleanup.
#
# The B2 CLI is invoked via Docker so the host stays clean.
#
# Exit codes:
#   0 success
#   1 missing config / pre-flight failure
#   2 dump or upload failed
set -euo pipefail

ENV_FILE=${ENV_FILE:-/etc/vylex/coordinator.env}
WORK=${WORK:-/var/lib/vylex/backups}
RETENTION_DAYS=${RETENTION_DAYS:-7}

if [[ ! -r "$ENV_FILE" ]]; then
  echo "[backup] no env file at $ENV_FILE" >&2
  exit 1
fi
set -a; . "$ENV_FILE"; set +a

if [[ -z "${B2_KEY_ID:-}" || -z "${B2_APPLICATION_KEY:-}" ]]; then
  echo "[backup] B2 credentials not configured — skipping (env: $ENV_FILE)" >&2
  exit 0
fi
B2_BUCKET=${B2_BUCKET:-vylexai-backups}

mkdir -p "$WORK"
ts=$(date -u +%Y%m%dT%H%M%SZ)
day=$(date -u +%Y/%m/%d)
out="$WORK/coordinator-$ts.sql.gz"

echo "[backup] dumping postgres → $out"
docker exec -i vylex-postgres-1 pg_dumpall -U vylex \
  | gzip -9 > "$out"

if [[ ! -s "$out" ]]; then
  echo "[backup] empty dump — failing" >&2
  rm -f "$out"
  exit 2
fi

bytes=$(stat -c %s "$out" 2>/dev/null || stat -f %z "$out")
echo "[backup] dump ok, $bytes bytes"

remote="coordinator/$day/$(basename "$out")"

echo "[backup] uploading to b2://$B2_BUCKET/$remote"
docker run --rm \
  -v "$WORK":/backups:ro \
  -e B2_APPLICATION_KEY_ID="$B2_KEY_ID" \
  -e B2_APPLICATION_KEY="$B2_APPLICATION_KEY" \
  backblazeit/b2:latest \
  upload-file --quiet "$B2_BUCKET" "/backups/$(basename "$out")" "$remote"

echo "[backup] uploaded"

echo "[backup] pruning local dumps older than $RETENTION_DAYS days"
find "$WORK" -maxdepth 1 -type f -name "coordinator-*.sql.gz" \
  -mtime +"$RETENTION_DAYS" -print -delete

echo "[backup] done"

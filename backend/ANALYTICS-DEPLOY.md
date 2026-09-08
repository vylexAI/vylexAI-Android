# Deploy — VylexAI Analytics (VYL-80)

Stands up the analytics service from
[`Daniyal26/vylexai-analytics`](https://github.com/Daniyal26/vylexai-analytics)
as the `analytics` container in this compose stack. Caddy routes
`https://api.vylexai.com/api/v1/*` to it; everything else stays on the
coordinator. Until this runbook is done, `/api/v1/metrics/*` returns 404.

Architecture: the service reads the coordinator's ledger DB **read-only** and
keeps its **own read-write** analytics DB (`vylex_analytics`) for daily
aggregates. Bearer-token gate on `/api/v1/*` (`VYL_API_TOKEN`).

---

## One-time prerequisites (human, on prod — do BEFORE the first deploy)

### 1. Build the analytics image
Merge the deploy workflow in `Daniyal26/vylexai-analytics` (PR opens one), then
tag a release there so it builds & pushes `ghcr.io/daniyal26/vylexai-analytics`.
**Make that GHCR package public** (Package → Settings → Change visibility) so the
Hetzner VM can pull it — the coordinator's deploy token can't read another
namespace's private packages. (Alternative: log the VM into GHCR with a PAT.)

### 2. Postgres users + analytics DB
On the VM, inside the postgres container:
```bash
docker compose -f docker-compose.prod.yml exec postgres psql -U vylex -d vylex
```
```sql
-- read-only role on the ledger DB
CREATE ROLE vylex_ro LOGIN PASSWORD :'ro_pw';
GRANT CONNECT ON DATABASE vylex TO vylex_ro;
GRANT USAGE ON SCHEMA public TO vylex_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO vylex_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO vylex_ro;

-- read-write analytics DB (owns its own schema/tables)
CREATE ROLE vylex_analytics LOGIN PASSWORD :'rw_pw';
CREATE DATABASE vylex_analytics OWNER vylex_analytics;
```
(Pass `-v ro_pw="…" -v rw_pw="…"` or type the literals; never commit them.)

### 3. Secrets in `/etc/vylex/coordinator.env`
```
ANALYTICS_DB_RO_PASSWORD=<the ro_pw above>
ANALYTICS_DB_RW_PASSWORD=<the rw_pw above>
ANALYTICS_API_TOKEN=<openssl rand -hex 32>   # dashboard sends this as Bearer
ANALYTICS_TAG=latest                          # or a pinned image tag
```

---

## Deploy

1. Merge this PR (adds the `analytics` service + Caddy `/api/v1/*` route).
2. Ship it through the normal pipeline — push a coordinator tag:
   ```bash
   git tag coordinator-v<next>
   git push origin coordinator-v<next>
   ```
   `deploy.yml` rsyncs `backend/` + `docker compose up -d` → pulls the analytics
   image, starts it, reloads Caddy.
3. **Run analytics migrations** (creates the analytics schema/tables):
   ```bash
   docker compose -f docker-compose.prod.yml exec analytics alembic upgrade head
   ```

## Verify
```bash
curl -fsS -H "Authorization: Bearer $ANALYTICS_API_TOKEN" \
  https://api.vylexai.com/api/v1/metrics/health
```
Coordinator unaffected: `curl -fsS https://api.vylexai.com/health` still ok.

## Dashboard (separate)
The Next.js dashboard (`frontend/` in the analytics repo) is **not** covered
here — deploy it to Vercel or its own container + Caddy route (e.g.
`dashboard.vylexai.com`), pointed at the metrics API with the bearer token.
Note there is already a founder dashboard on `vylexai.com` (the `website`
stack) — decide whether this replaces or complements it before shipping.

## Rollback
Remove the `analytics` service + the `@analytics` Caddy route (revert this PR)
and re-tag, or on the box set `ANALYTICS_TAG` to a previous image and
`docker compose up -d`. The coordinator (`app`) is untouched by either.

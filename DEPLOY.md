# Deploy — VylexAI Coordinator

This doc takes the coordinator from "git repo" to "live at
`https://api.vylexai.com`" in ~20 minutes once the prerequisites land.

The app code ships a production `Dockerfile`, `docker-compose.prod.yml`,
a `Caddyfile` for TLS, and a `.github/workflows/deploy.yml` workflow
that builds the image to GHCR and SSH-deploys on tag push.

---

## One-time prerequisites

### 1. Hetzner VM

- **Size:** CX22 (€4.51/mo, 2 vCPU, 4 GB RAM, 40 GB SSD) is plenty for
  the preview and well into the hundreds of concurrent nodes.
- **Location:** Nuremberg (Germany) — keeps the data in the EU, matches
  Dudaev Systems UG's jurisdiction, helps with the GDPR story.
- **Image:** Ubuntu 24.04 LTS.
- **SSH key:** the public key of `VYLEX_DEPLOY_SSH_KEY` (see §4).
- **Firewall:** allow `22/tcp` (SSH), `80/tcp`, `443/tcp`, `443/udp`.

On the VM, once logged in:

```bash
# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo systemctl enable --now docker

# App directory
sudo mkdir -p /opt/vylex/coordinator /etc/vylex
sudo chown -R "$USER":"$USER" /opt/vylex

# Secrets (edit the placeholders)
sudo tee /etc/vylex/coordinator.env > /dev/null <<'ENV'
POSTGRES_PASSWORD=<generate a strong 32-char password>
JWT_SECRET=<generate with: openssl rand -hex 48>
PLAY_INTEGRITY_PROJECT_NUMBER=<Google Cloud project number, or leave blank>
ENV
sudo chmod 600 /etc/vylex/coordinator.env
```

### 2. DNS

In the zone for `vylexai.com`, add:

| Type | Name | Value               | TTL |
|------|------|---------------------|-----|
| A    | api  | (Hetzner VM IPv4)   | 300 |
| AAAA | api  | (Hetzner VM IPv6)   | 300 |

Propagation: 1–5 min usually; wait until `dig api.vylexai.com` resolves
before triggering the first deploy (Caddy's Let's Encrypt challenge
fails if DNS isn't live).

### 3. GitHub repo settings

**Secrets** (Settings → Secrets and variables → Actions → New secret):

| Name | Value |
|------|-------|
| `DEPLOY_HOST` | Hetzner VM IPv4 or `api.vylexai.com` |
| `DEPLOY_USER` | The SSH user that owns `/opt/vylex` (e.g. `vylex`) |
| `DEPLOY_SSH_KEY` | Private key corresponding to the pubkey on the VM |

**Variables** (same page, Variables tab):

| Name | Value |
|------|-------|
| `DEPLOY_ENABLED` | `true` |

`DEPLOY_ENABLED=true` gates the deploy job; until it's set, tag pushes
build the image but skip the SSH step (safe default).

### 4. Pull secret on the VM

The VM needs to pull from GHCR. Either make the image public (simpler;
the image itself contains no secrets) or have the VM log in with a
Personal Access Token with `read:packages` scope:

```bash
echo "$GHCR_READ_PAT" | docker login ghcr.io -u <user> --password-stdin
```

---

## Releasing

### First deploy

1. Verify prerequisites above.
2. Push a tag from `main`:
   ```bash
   git tag coordinator-v0.1.0
   git push origin coordinator-v0.1.0
   ```
3. GitHub Actions runs `deploy.yml`:
   - Builds `ghcr.io/vylexAI/vylexai-coordinator:0.1.0` and `:latest`
   - Rsyncs `backend/` to `/opt/vylex/coordinator/` on the VM
   - SSHs in, pulls the image, runs `docker compose up -d`
   - Curls `https://api.vylexai.com/health` up to 5 times before failing
4. Caddy automatically provisions a Let's Encrypt certificate on first
   request (takes ~10 seconds). You may see one 502 before it completes.

### Subsequent deploys

- **Tag push:** same flow as first deploy — `coordinator-v0.2.0`, etc.
- **Workflow dispatch:** for out-of-band pushes. Actions tab → Deploy
  Coordinator → Run workflow → pick an image tag.

### Manual rollback

```bash
ssh vylex@api.vylexai.com
cd /opt/vylex/coordinator
IMAGE_TAG=0.1.0 docker compose -f docker-compose.prod.yml \
  --env-file /etc/vylex/coordinator.env up -d
```

### Alembic migrations

The container's `entrypoint.sh` runs `alembic upgrade head` before
launching uvicorn. New migrations ship automatically with each release.
No manual step needed.

---

## Backups

The Postgres volume (`pgdata`) lives on the VM. Back it up daily:

```bash
# /etc/cron.d/vylex-pg-backup
0 3 * * *  vylex  docker exec coordinator-postgres-1 pg_dump -U vylex vylex | gzip > /opt/vylex/backups/$(date +\%F).sql.gz
```

And push to offsite storage (Backblaze B2 + `rclone` is cheap and
battle-tested).

---

## Monitoring (after launch)

- **UptimeRobot free tier** on `https://api.vylexai.com/health` — 5-min
  HTTP check, email/Telegram alert on miss.
- **Hetzner Console** shows VM CPU / disk / network without any agent.
- Consider adding **Grafana Loki + Promtail** later if we start caring
  about per-request observability. Not needed for the preview.

---

## Checklist before first live deploy

- [ ] Hetzner CX22 provisioned, SSH key added
- [ ] `/opt/vylex/coordinator/` writable by the deploy user
- [ ] `/etc/vylex/coordinator.env` populated with real secrets
- [ ] DNS `A` (and optionally `AAAA`) record for `api.vylexai.com`
  points at the VM
- [ ] GitHub secrets `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY` set
- [ ] GitHub variable `DEPLOY_ENABLED=true`
- [ ] GHCR pull works from the VM (public image or logged-in PAT)
- [ ] `git tag coordinator-v0.1.0 && git push origin coordinator-v0.1.0`
- [ ] `curl -fsS https://api.vylexai.com/health` returns `{"status":"ok",…}`

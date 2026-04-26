#!/usr/bin/env bash
# One-shot installer for the daily pg-backup systemd timer on the Hetzner
# host. Run as root after deploying the source tree to /opt/vylex.
set -euo pipefail

SRC=${SRC:-/opt/vylex/scripts/pg-backup.sh}
DEST=/usr/local/bin/vylex-pg-backup
UNIT_DIR=/etc/systemd/system

if [[ ! -x "$SRC" ]]; then
  chmod +x "$SRC"
fi
install -m 755 "$SRC" "$DEST"

cat > "$UNIT_DIR/vylex-pg-backup.service" <<'UNIT'
[Unit]
Description=VylexAI · Postgres backup to Backblaze B2
Wants=network-online.target docker.service
After=network-online.target docker.service

[Service]
Type=oneshot
ExecStart=/usr/local/bin/vylex-pg-backup
StandardOutput=journal
StandardError=journal
UNIT

cat > "$UNIT_DIR/vylex-pg-backup.timer" <<'UNIT'
[Unit]
Description=VylexAI · Run pg-backup daily at 03:30 UTC

[Timer]
OnCalendar=*-*-* 03:30:00
Persistent=true
Unit=vylex-pg-backup.service

[Install]
WantedBy=timers.target
UNIT

systemctl daemon-reload
systemctl enable --now vylex-pg-backup.timer

echo "[install] enabled. next run:"
systemctl list-timers vylex-pg-backup.timer --no-pager | head -5

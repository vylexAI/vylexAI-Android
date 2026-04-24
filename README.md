<p align="center">
  <img src="brand/vylexai-logo.svg" alt="VylexAI" width="96" />
</p>

<h1 align="center">VylexAI</h1>

<p align="center">
  <strong>Decentralized AI, powered by every smartphone.</strong><br/>
  A new layer of internet infrastructure that turns any device — phone, laptop, GPU node —<br/>
  into part of a global, verifiable AI compute network.
</p>

<p align="center">
  <a href="https://github.com/vylexAI/vylexAI-Android/actions/workflows/ci.yml"><img alt="Android CI"   src="https://github.com/vylexAI/vylexAI-Android/actions/workflows/ci.yml/badge.svg"/></a>
  <a href="https://github.com/vylexAI/vylexAI-Android/actions/workflows/backend.yml"><img alt="Backend CI" src="https://github.com/vylexAI/vylexAI-Android/actions/workflows/backend.yml/badge.svg"/></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License"   src="https://img.shields.io/badge/License-Apache_2.0-blue.svg"/></a>
  <img alt="Made in Germany" src="https://img.shields.io/badge/Made_in-Germany-000000?labelColor=DD0000&color=FFCE00"/>
</p>

<p align="center">
  <a href="https://vylexai-preview.vercel.app"><strong>Live site preview →</strong></a>
  &nbsp;·&nbsp;
  <a href="#download-the-beta"><strong>Download the beta</strong></a>
  &nbsp;·&nbsp;
  <a href="mailto:hello@vylexai.com"><strong>Talk to the team</strong></a>
</p>

---

## Why VylexAI

Today's AI lives inside a handful of data centers owned by a handful of companies. Compute is
expensive, scaling is constrained by physical infrastructure, and 3+ billion smartphones sit
idle every night. VylexAI unifies those idle devices into a single verifiable compute layer —
cheaper, faster-scaling, and not owned by any single corporation.

We are not improving the existing AI stack. We are **replacing the infrastructure under it.**

---

## Two modes, one network

<table>
<tr>
<td width="50%" valign="top">

### ⚡ Provider

Your phone joins the network as a compute node. While **charging on Wi-Fi with a safe
battery temperature**, the app runs lightweight AI tasks in the background and earns
**BSAI** — the network's unit of account — for every verified contribution.

Each device reports a *Performance Score* based on CPU / NPU / RAM / thermals, and
Play Integrity ensures the network can distinguish real phones from emulators.

</td>
<td width="50%" valign="top">

### ✨ Client

Submit inference, OCR, classification, or lightweight fine-tuning tasks. The coordinator
dispatches each job across participating devices with **N-way redundancy** — results are
only accepted when a majority of nodes agree.

Pay in BSAI, at a fraction of centralized cloud cost. Your task inputs and outputs stay
inside the network — no proxy in the middle.

</td>
</tr>
</table>

---

## How it works

```
       ┌─────────────┐     POST /jobs              ┌──────────────────┐
  You  │ Client app  │ ──────────────────────────▶ │                  │
       └─────────────┘                             │   Coordinator    │
                                                   │                  │
       ┌─────────────┐                             │  FastAPI +       │
       │             │ ◀── GET /tasks/next ─────── │  Postgres 16 +   │
       │  Phone node │                             │  BSAI ledger +   │
       │ (Provider)  │ ─── POST /tasks/result ───▶ │  N-way quorum    │
       └─────────────┘                             └──────────────────┘
       on-device                                   single source of truth
       MobileNet inference                         (Hetzner, Germany, EU-only)
```

Every heartbeat carries a **Play Integrity** token. Every task result carries a hash that
must match the majority of nodes to credit the provider.

---

## Status · April 2026

**Technical preview ships now.** Production BSAI economy follows separately — balances
shown during the preview reflect contribution, not monetary value.

| Stage | Status |
|---|---|
| **Stage 1 — UI prototype** · 11 Compose screens, dark-first design system | ✅ |
| **Stage 2 — functional MVP** · Device scanner, network layer, auth, MobileNet on-device demo, WorkManager provider loop, Play Integrity, live dashboards, FastAPI coordinator | ✅ |
| **Stage 3 — release track** · Signed release AAB · Privacy + Terms · Play Store listing · Deploy pipeline · Brand SVG | ✅ |
| **Stage 3 — operator handoff** · Play Console internal testing · Hetzner VM + `api.vylexai.com` DNS · Production keystore | 🟡 in progress |

## Download the beta

The Android AAB is ready to publish. Once the Play Console internal testing track is live,
investors and early testers will receive a private opt-in link here. Watch this repo —
we release via tags.

## Roadmap

- **Q2 2026** — Play Store internal testing · coordinator live at `api.vylexai.com`
- **Q3 2026** — Public beta · Bonsai / 1-bit runtime as an optional plug-in
- **Q4 2026** — BSAI production economy · withdraw flow · iOS client
- **2027** — `vylexai-core` desktop client sharing the same coordinator API
- **Later** — VylexAI OS for robotics (Phase 5 per the pitch deck)

---

## Core team — built in Germany

Engineering out of **Cottbus** and **Frankfurt**, inside the framework of the
EU AI Act. Supported by **Startup Lausitz** (consulting) and the
**Brandenburgische Technische Universität Cottbus-Senftenberg**.

- **Adlan Dudaev** — Founder · Chief Technology Architect
- **Ananya Sai Tippani** — AI & Machine Learning Engineer
- **Hammad Hassan Bajwa** — Software Engineer (AI / Backend)
- **Riyabrata Mondal** — Lead Full-Stack & Platform Engineer
- **Vishva Hirenkumar Jani** — AI Infrastructure & Backend Engineer

Company: **Dudaev Systems UG**, Cottbus, Germany.

---

## For developers

### Stack

- **Android** — Kotlin 2.0.21 · Jetpack Compose (Material 3) · MVVM · Hilt · Navigation · Room · Retrofit + OkHttp · WorkManager
- **On-device AI** — TensorFlow Lite (primary) + ONNX Runtime Mobile (wired, ready for larger models)
- **Backend** — FastAPI · SQLAlchemy 2 async · asyncpg · Alembic · JWT · bcrypt · Postgres 16
- **Security** — Play Integrity attestation · Android Keystore for JWT · custodial wallet for preview

### Requirements

- JDK 17
- Android Studio Narwhal (or newer), AGP 8.x
- `minSdk` 26 · `targetSdk` 35
- Python 3.12 (backend)

### Quick start

```bash
# Android
./gradlew assembleDebug                 # debug APK
./gradlew :app:bundleRelease            # signed release AAB (requires keystore — see KEYSTORE.md)
./gradlew test detekt ktlintCheck lintDebug

# Backend
cd backend
docker compose up -d postgres
uv sync
uv run alembic upgrade head
uv run fastapi dev app/main.py          # → http://localhost:8000/docs
```

### Repository layout

- [`app/`](app/) — Android client (Kotlin + Jetpack Compose)
- [`backend/`](backend/) — FastAPI coordinator (Python 3.12 + Postgres 16)
- [`website/`](website/) — Next.js site currently hosted at [vylexai-preview.vercel.app](https://vylexai-preview.vercel.app)
- [`docs/`](docs/) — Privacy policy, Terms of Service, Play Store listing copy, Data Safety answers
- [`brand/`](brand/) — Vector logo + source assets
- [`KEYSTORE.md`](KEYSTORE.md) — Production signing key workflow
- [`DEPLOY.md`](DEPLOY.md) — Coordinator deploy runbook (Hetzner + Caddy + GitHub Actions)

---

## Contact

- **Partnerships / investors:** `hello@vylexai.com`
- **Website:** https://vylexai.com
- **Preview:** https://vylexai-preview.vercel.app
- **Brand:** [`brand/vylexai-logo.svg`](brand/vylexai-logo.svg)

## License

Apache 2.0 — see [`LICENSE`](LICENSE).
Copyright © 2026 **Dudaev Systems UG**, Cottbus, Germany.

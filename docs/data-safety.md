# Play Console · Data Safety form — VylexAI App

Answers to each Google Play Data Safety question. Paste these into Play
Console → Policy → App Content → Data Safety.

---

## 1. Do you collect or share any of the required user data types?

**Yes.**

## 2. Is all of the user data collected by your app encrypted in transit?

**Yes.**
- Every call to the coordinator is HTTPS (TLS 1.2+).
- Debug builds additionally allow cleartext to `10.0.2.2` and `localhost` for emulator development — this is never present in release builds.

## 3. Do you provide a way for users to request that their data be deleted?

**Yes.** Via Settings → Account → Delete, or by emailing `hello@vylexai.com`. Full workflow in `docs/privacy-policy.md` §4.

---

## 4. Data types — what we collect

### Personal info

| Data | Collected | Shared | Optional? | Purpose | Notes |
|---|---|---|---|---|---|
| Email address | **Yes** | No | Required | Account management, security notifications | Stored in EU, hashed passwords only |
| Name | No | – | – | – | – |
| User IDs | **Yes** (server-side UUID) | No | Required | Routing AI tasks | Install-scoped, not cross-app |
| Phone number | No | – | – | – | – |
| Physical address | No | – | – | – | – |

### Financial info

| Data | Collected | Shared | Optional? | Purpose |
|---|---|---|---|---|
| User payment info | No | – | – | – |
| Purchase history | No | – | – | – |
| Credit score | No | – | – | – |
| Other financial info (BSAI ledger) | **Yes** | No | Required | Custodial reward ledger. Technical preview — not a monetary instrument |

### Health and fitness, Messages, Photos or videos, Audio, Files and docs, Calendar, Contacts

**None collected.**

### App activity

| Data | Collected | Shared | Optional? | Purpose |
|---|---|---|---|---|
| App interactions | **Yes** | No | Required | Worker telemetry: tasks completed, session start/end |
| In-app search history | No | – | – | – |
| Installed apps | No | – | – | – |
| Other user-generated content | **Yes** (task inputs *you* submit as a Client) | No | User action | Dispatching your AI job |
| Other actions | No | – | – | – |

### Web browsing

**None collected.**

### App info and performance

| Data | Collected | Shared | Optional? | Purpose |
|---|---|---|---|---|
| Crash logs | **Yes** (server logs) | No | Required | Diagnose Worker failures |
| Diagnostics | **Yes** (device profile + thermal + network telemetry while Worker runs) | No | Required | Task routing, battery/thermal safety |
| Other app performance data | No | – | – | – |

### Device or other IDs

| Data | Collected | Shared | Optional? | Purpose |
|---|---|---|---|---|
| Device or other IDs | **Yes** (random UUID, install-scoped) | No | Required | Identify your node on the network |

### Location

**None collected** (no ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission requested).

---

## 5. Security practices

- All network traffic is encrypted in transit (TLS).
- Data at rest in the coordinator's Postgres is encrypted via the hosting provider's volume encryption.
- JWT tokens on device are held in `EncryptedSharedPreferences` backed by the Android Keystore.
- We follow the Mobile Application Security Verification Standard (MASVS) L1 requirements at minimum.
- Users can request account + data deletion at any time (see Privacy Policy §4).
- We undergo a lightweight internal security review before every public release.

---

## 6. Data sharing with third parties

- **No sharing with third parties** apart from Google's Play Integrity API (attestation token verification). Tokens are ephemeral, per-request, and contain no user identifiers beyond a request-hash you control.
- We do not work with ad networks, analytics SDKs, or identity resellers.

---

## 7. Family policy

- Not directed at children under 16.
- No content likely to be attractive to children.
- No COPPA or GDPR-K obligations triggered.

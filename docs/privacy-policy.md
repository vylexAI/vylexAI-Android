# Privacy Policy — VylexAI App

**Last updated:** 2026-04-24
**Controller:** Dudaev Systems UG, Cottbus, Germany · `hello@vylexai.com`

Dudaev Systems UG ("VylexAI", "we", "us") runs the VylexAI App and the VylexAI network of decentralized AI compute. This policy explains what data the Android app collects, why we collect it, how long we keep it, and what rights you have under the **EU General Data Protection Regulation (GDPR)**.

If you don't want us to process your data, do not install or sign in to the app.

---

## 1. What the app collects

### 1.1 Data you give us directly

| Data | Why | Legal basis |
|---|---|---|
| Email address | Account identity, sign-in, transactional messages | Contract (Art. 6(1)(b) GDPR) |
| Password (hashed) | Authenticating you to the VylexAI coordinator | Contract (Art. 6(1)(b)) |

Passwords are never stored in plaintext. We store a bcrypt hash. The plaintext password never leaves your device in recoverable form.

### 1.2 Data generated automatically

| Data | Why | Legal basis |
|---|---|---|
| Stable device identifier (random UUID, install-scoped) | Route AI tasks to your phone; prevent replay | Contract |
| Device profile: phone model, Android version, CPU cores + frequency, RAM, free storage, network transport, NNAPI / Vulkan availability | Match task difficulty to your phone's capabilities; display an accurate Performance Score | Contract |
| Runtime telemetry while the Worker is active: battery %, temperature, charging state, network type | Stop compute when constraints aren't met; keep your battery safe | Contract + legitimate interest (Art. 6(1)(f)) |
| Task results (hash + execution time) | Network quorum verification and reward settlement | Contract |
| Play Integrity attestation token | Detect tampering, emulators, cloned APKs — ensures honest participation | Legitimate interest |
| BSAI ledger entries (credits + debits) | Custodial wallet balance | Contract |

### 1.3 Data we do **not** collect

- Your contacts, SMS, call logs, precise location, photos, microphone, or camera.
- Advertising identifiers. VylexAI App does not run ads and does not share data with ad networks.
- Files or images you didn't explicitly submit as part of a task.

---

## 2. Where the data goes

- Your account + device profile + telemetry + ledger go to our coordinator hosted in the **European Union** (Germany — Hetzner Online GmbH). No transfers outside the EU.
- Play Integrity tokens are verified through Google's Play Integrity API. Google's own handling is covered by Google's privacy policy.
- We do **not** sell or rent personal data. Ever.

---

## 3. How long we keep it

| Data | Retention |
|---|---|
| Account + email | While your account exists; 30 days after deletion request |
| Device profile | Last value kept while device is registered; deleted on device unlink |
| Worker telemetry | 90 days rolling |
| Task results | 90 days, or as long as the parent job exists |
| Ledger entries | As long as your account exists (audit + balance integrity) |
| Server logs | 14 days |

---

## 4. Your rights (GDPR)

You can exercise any of the following by emailing `hello@vylexai.com` with the subject `GDPR request`:

- **Access** — a copy of the data we hold about you (Art. 15)
- **Rectification** — correct wrong data (Art. 16)
- **Erasure** — delete your account and associated data (Art. 17)
- **Restriction** — temporarily freeze processing (Art. 18)
- **Portability** — receive your data in a machine-readable format (Art. 20)
- **Object** — stop processing where the basis is legitimate interest (Art. 21)
- **Complain** to a supervisory authority (Art. 77) — e.g. the Brandenburg Data Protection Authority

We respond within 30 days (Art. 12(3)).

---

## 5. Cookies and local storage

- The Android app does not use cookies.
- On-device storage: your JWT and device identifier are encrypted with a key held in Android Keystore (`EncryptedSharedPreferences`). They never leave your device except over HTTPS to our coordinator.

---

## 6. Children

VylexAI App is not intended for users under 16. We do not knowingly collect data from minors.

---

## 7. Changes to this policy

We will post revisions here and update the "Last updated" date. Material changes (new categories of data, new recipients) will be communicated in-app before they take effect.

---

## 8. Contact

Dudaev Systems UG
Cottbus, Germany
`hello@vylexai.com`

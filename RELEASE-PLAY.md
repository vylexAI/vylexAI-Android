# Release to Google Play — automated (no web console upload)

Uploads a signed AAB to a Play track via the **Google Play Android Publisher API**
using [gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher).
Replaces the manual "drag the .aab into Play Console" step.

> The **first upload of a brand-new app** and all **account/identity/verification**
> steps (incl. Google's Android developer verification) are still web-console only.
> This automates *subsequent* release uploads.

## One-time setup (account owner, in the web console)

1. **Play Console** → **Setup → API access** → **Link a Google Cloud project**.
2. **Create service account** (deep-links to Google Cloud Console) →
   **Keys → Add key → JSON** → downloads the key file.
3. Back in **Play Console → API access** → find the service account →
   **Grant access** → at minimum *Release to testing tracks* for `com.vylexai.app`.
4. The **Google Play Android Developer API** is enabled on the linked project
   automatically (else: GCP Console → APIs & Services → enable it).

Keep the JSON safe — it is a credential. **Never commit it** (this repo is public;
`play-service-account.json` is gitignored).

## Local publish

```bash
# put the key at repo root (gitignored), or export PLAY_SERVICE_ACCOUNT_JSON=/abs/path.json
cp ~/Downloads/play-*.json android/play-service-account.json

cd android
./gradlew :app:publishReleaseBundle                 # → internal track (default)
./gradlew :app:publishReleaseBundle --track beta     # or another track
```

Signing uses the same release keystore as `bundleRelease`
(`keystore.properties` or `VYLEX_KEYSTORE_*` env — see `KEYSTORE.md`).
Bump `versionCode`/`versionName` in `app/build.gradle.kts` before each upload.

## CI publish

Workflow: **Actions → "Release to Play" → Run workflow** (manual only; pick a track).
Add these repo **secrets** first (Settings → Secrets and variables → Actions):

| Secret | Value |
|---|---|
| `PLAY_SERVICE_ACCOUNT_JSON` | full contents of the service-account JSON |
| `VYLEX_KEYSTORE_BASE64` | `base64 -i keystores/vylexai-release.jks` |
| `VYLEX_KEYSTORE_PASSWORD` | keystore store password |
| `VYLEX_KEYSTORE_KEY_ALIAS` | signing key alias |
| `VYLEX_KEYSTORE_KEY_PASSWORD` | signing key password |
| `VYLEX_PLAY_INTEGRITY_PROJECT` | (optional) GCP project number |

Config lives in the `play { }` block of `app/build.gradle.kts` (default track:
`internal`, `defaultToAppBundles = true`).

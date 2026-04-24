# Release signing — VylexAI Android

The production AAB that ships to Google Play must be signed with a
dedicated keystore that lives **outside this repository**.

## Creating the keystore (once per organization)

> ⚠️ Do this on a machine you control. Never commit the `.jks` or the
> passwords to git.

```bash
keytool -genkeypair \
  -v \
  -keystore vylexai-release.jks \
  -alias vylexai \
  -keyalg RSA -keysize 4096 \
  -validity 9125 \
  -storetype PKCS12 \
  -dname "CN=Dudaev Systems UG, O=Dudaev Systems UG, C=DE"
```

`keytool` prompts for a store password and a key password — keep them
identical to reduce confusion, and record both in a secure password
manager (1Password / Bitwarden).

Back up the `.jks` to at least two separate locations (encrypted drive +
cloud vault). Losing it means losing the ability to update the app on
Google Play; there is no recovery path.

## Telling Gradle where the keystore lives

### Option A — `keystore.properties` (local dev + dry-run CI)

Create `keystore.properties` at the repo root. Already gitignored.

```properties
storeFile=/absolute/path/to/vylexai-release.jks
storePassword=<store password>
keyAlias=vylexai
keyPassword=<key password>
```

### Option B — Environment variables (CI / release pipelines)

```
VYLEX_KEYSTORE_PATH=/secure/path/vylexai-release.jks
VYLEX_KEYSTORE_PASSWORD=...
VYLEX_KEYSTORE_KEY_ALIAS=vylexai
VYLEX_KEYSTORE_KEY_PASSWORD=...
```

Set these as repository secrets in GitHub Actions and pass them through
in the workflow step. Never echo them.

### Option C — Nothing configured

Release builds fall back to the **debug** signing key and emit a
warning. Useful for local smoke builds; **will be rejected by Google
Play Console** — upload only releases signed with the real key.

## Building the release AAB

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

Play Console expects this bundle. Internal Testing track accepts it
within minutes; Production review takes 1–3 days.

## Upload-key vs App-signing-key

Google Play signing handles the **app-signing key** server-side. What
you upload (and what this project signs) is the **upload key**. If the
upload key is ever compromised, Play can rotate it; the app-signing key
stays intact. So: treat this keystore carefully, but it's not a
single-point-of-failure for the app on Play.

## Checklist before first upload

- [ ] Keystore generated with a 4096-bit RSA key, ≥25-year validity
- [ ] Store + key passwords recorded in a shared password manager
- [ ] Keystore `.jks` backed up in two separate secure locations
- [ ] CI repo secrets set (or `keystore.properties` wired locally)
- [ ] `./gradlew :app:bundleRelease` emits a file that `jarsigner -verify`
      reports as signed

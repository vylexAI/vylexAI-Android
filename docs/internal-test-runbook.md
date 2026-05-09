# Internal Test Track — Chrome-Claude Runbook

**Goal:** push the existing signed AAB to the Internal Testing track on Google Play Console, add Adlan + Riyabrata as testers, get them invitation emails so they can install via Play Store before Monday's Sebastian-Termin.

**Time estimate:** 25 minutes.

**Why Internal Test (not Closed Test):** Internal Test has no 12-tester rule, no Data Safety/Store Listing requirements, no review wait. It's the fast path for "get the production AAB onto a real phone via real Play Store invitation" before the Closed Testing arc starts.

---

## Prerequisites — gather before starting

1. **Adlan's personal Gmail** — Play Console testers need a real Google account. `adlan@vylexai.com` is Porkbun forwarding, NOT a Google account. Use Adlan's personal Gmail (something like `adlan.dudaev@gmail.com` — confirm with him). **Without this, the invitation email has nowhere to land.**
2. **AAB file path:** `/Users/niveauimmo./Developer/inngestnyth/vylexai/android/app/build/outputs/bundle/release/app-release.aab` (42 MB, signed, V0.1.0, com.vylexai.app, built 24 April)
3. **Logged into Play Console** as either:
   - Adlan (Account Owner, Konto-ID `7721323965621130284`)
   - or Smith Agency Admin (`ashab@cnap.dev`, invited 29 April)

---

## Sequential steps

### 1. Open the app

- Navigate to https://play.google.com/console
- Click "VylexAI" (com.vylexai.app, status: Entwurf, 1 App in list)

### 2. Open Internal Test track

- Left sidebar: **Testen und veröffentlichen** → expand
- Click **Tests** → **Interner Test**
- You'll land on the Internal Test dashboard (Releases tab default)

### 3. Create a new release

- Click button **Neuen Release erstellen** (top right of Versionen tab)
- Section "App-Integrität" — leave default (Play App Signing)
- Section "App-Bundles":
  - Click **Hochladen**
  - File picker opens — navigate to and select:
    ```
    /Users/niveauimmo./Developer/inngestnyth/vylexai/android/app/build/outputs/bundle/release/app-release.aab
    ```
  - Wait for upload (~30 seconds for 42 MB) + Google's automatic AAB verification (~1 min)
  - Should show: "VylexAI 0.1.0 (1)" with green checkmark

### 4. Release name + notes

- **Release-Name:** Google auto-fills with "1 (0.1.0)" — leave as is
- **Release-Hinweise** (release notes for testers):
  - Click "Sprache hinzufügen" if needed → ensure German + English are added
  - Paste the EN release notes (asset 1 below)
  - Paste the DE release notes (asset 2 below)

### 5. Save and review

- Click **Speichern** (bottom right)
- Click **Release prüfen** (also bottom right after save)
- Review summary screen
- Click **Rollout zum internen Test starten**
- Confirmation dialog: click **Rollout** to confirm

### 6. Add testers

- Top tabs (still inside Interner Test): click **Tester**
- Section "Tester":
  - Click **Tester-Liste erstellen** (or "E-Mail-Liste erstellen" depending on Google's current UI label)
  - List name: `Core Team` (or `Internal Core` — your choice)
  - Paste the tester emails (asset 3 below) — one per line or comma-separated
  - Click **Speichern**
- Toggle the list ON if not already enabled
- Click **Speichern** at the bottom

### 7. Get the opt-in link

- Still in **Tester** tab, scroll to "Wie Tester an deiner App teilnehmen können" section
- Find **Link zum Beitreten kopieren** (or "Opt-in URL")
- Copy this link — looks like: `https://play.google.com/apps/internaltest?id=com.vylexai.app&pli=1`

### 8. Verify rollout completed

- Go back to **Versionen** tab
- Should show a release with status "Verfügbar bei Internem Test" (green) — usually takes 5-15 min after rollout to flip to live

### 9. Done

- Send the opt-in link + tester instructions (asset 4) to Adlan and Riyabrata via WhatsApp
- Internal testers will receive a Google-sent invitation email within 5-30 min
- They click the email link → Play Store opens → install button works for them only

---

## Asset 1 — Release notes EN (paste in step 4)

```
VylexAI 0.1.0 — first internal build
- Provider/Client mode selection
- Real-device hardware scan (CPU, RAM, NNAPI, Vulkan)
- Worker UI: status pill, classification label, metric tiles, latency chart
- BSAI rewards counter (simulated)
- Connects to coordinator at api.vylexai.com
For internal pre-Termin demo.
```

## Asset 2 — Release notes DE (paste in step 4)

```
VylexAI 0.1.0 — erster interner Build
- Provider-/Client-Modus-Auswahl
- Echt-Hardware-Scan (CPU, RAM, NNAPI, Vulkan)
- Worker-UI: Status-Pill, Klassifikations-Label, Metrik-Kacheln, Latenz-Chart
- BSAI-Rewards-Zähler (simuliert)
- Verbindet sich mit Coordinator unter api.vylexai.com
Für internen Termin-Demo.
```

## Asset 3 — Tester emails (paste in step 6)

```
<adlan-personal-gmail-here@gmail.com>
riyabrata@gmail.com
```

(Replace the first line with Adlan's actual Gmail before pasting. Two emails total, one per line. ashab@cnap.dev does NOT need to be added — already has Admin role which includes test access.)

## Asset 4 — Instructions for Adlan + Riyabrata to install

Once the runbook is complete, send this via WhatsApp to Adlan (and Riyabrata gets the same):

```
VylexAI app — теперь готов к установке через Play Store

1. Открой свой Gmail на телефоне (тот же что я добавил в Play Console)
2. Найди письмо от Google "You're invited to test VylexAI" (придёт в течение 5-30 мин)
3. На телефоне нажми кнопку "Become a tester" в письме
4. Откроется Play Store с приложением VylexAI — нажми "Install"
5. Готово — приложение установится как обычное Play Store приложение

Если письмо не придёт через 30 минут — открой эту ссылку напрямую на телефоне:
<paste opt-in link from step 7>

После установки, открой приложение, выбери Provider mode, посмотри dashboard. Это та версия которую покажешь Себастьяну в понедельник.
```

---

## Sanity checks after completion

Before declaring done, verify:

- [ ] Release shows "Verfügbar bei Internem Test" (green) in Versionen tab
- [ ] Tester list shows 2 emails added
- [ ] Opt-in link copied and noted somewhere
- [ ] Adlan + Riyabrata received Google's invitation email (check by asking them — usually arrives in 5-30 min)
- [ ] At least one of them has installed the app and can launch it

If any step fails (upload error, AAB rejected, etc.) — stop and report the exact error message back. Do NOT bypass or retry blindly.

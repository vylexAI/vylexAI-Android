# vylexAI-Android

[![CI](https://github.com/vylexAI/vylexAI-Android/actions/workflows/ci.yml/badge.svg)](https://github.com/vylexAI/vylexAI-Android/actions/workflows/ci.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Made in Germany](https://img.shields.io/badge/Made_in-Germany-000000?labelColor=DD0000&color=FFCE00)](#)

> **VylexAI App — mobile-first MVP for Android devices.**
> Official Android client for the VylexAI distributed / federated AI compute network.

Dual-mode app: **Provider** (phone contributes compute to the network) · **Client** (submits AI tasks to the network).

> Positioning: **massive device network, not hardware**. Optimized for millions of smartphones as network nodes — not for few high-end rigs.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM**, **Hilt**, **Navigation-Compose**
- **Room** (local cache), **Retrofit** + **OkHttp** (network), **DataStore** (prefs)
- **WorkManager** + **ForegroundService** for the background compute worker
- **ONNX Runtime Mobile** (primary) + **TensorFlow Lite** (secondary) for on-device inference
- **JWT** auth, custodial BSAI wallet (MVP)

## Requirements

- JDK 17
- Android Studio Narwhal (or newer) / AGP 8.x
- `minSdk` 26 · `targetSdk` 35

## Getting started

```bash
./gradlew assembleDebug
./gradlew test detekt lint
```

## Status

**Stage 1** — Compose UI prototype of both modes on mock data ✅
**Stage 2** — Working APK with device scanner, real on-device inference demo, FastAPI coordinator integration (in progress)
**Stage 3** — Closed beta + Play Store internal testing track

## License

Apache-2.0

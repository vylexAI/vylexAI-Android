# vylexAI-Android

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

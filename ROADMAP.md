# Roadmap

Direction, not a commitment. Sequencing may change. The authoritative current
state is `ARCHITECTURE_STATE.md`.

## Near term
- **At-rest card-number encryption.** The Keystore engine is ready; switching it
  on for the live card columns needs a Room v3 migration adding a deterministic
  HMAC `cardNumberHash` so duplicate-detection and search keep working.
- **Wire the migration coordinator** into startup and add a WorkManager-backed
  `NotificationScheduler` (periodic backup / unused-card reminders).
- **Clipboard auto-clear** worker (setting already persists).
- **Detekt/ktlint to blocking** once a clean baseline is established.

## Medium term
- **Cloud sync provider.** Implement a real `CloudSyncProvider` +
  `AuthenticationProvider` (e.g. Drive app-data) behind the existing interfaces;
  enable encrypted, incremental, multi-device sync via `SyncCrypto`.
- **Wear OS module.** Stand up a `:wear` Gradle module consuming `core/wear`
  contracts: round/square UI, ambient mode, complication, offline cache.
- **Macrobenchmark module** for startup / scroll / render with CI baselines.
- **Manual drag-reorder** gesture UI (engine exists).

## Longer term
- **AI features** behind `core/ai` interfaces: card recognition, OCR, receipt
  parsing, offer recommendations, smart/voice search.
- **Glance widgets** and richer Material You theming.
- **Google / Apple Wallet import** (wizard placeholders exist).
- **Points & offers** notifications.

## Done
Phases 1–6A: foundation, data, UI, capture, smart wallet, security, flagship
architecture, and the enterprise development workflow.

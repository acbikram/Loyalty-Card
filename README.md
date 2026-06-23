# Universal Loyalty Wallet

An offline-first Android app for storing, organising, and instantly displaying
loyalty / rewards cards. Built for the Gulf retail market (Lulu, Nesto,
Carrefour, Prime, …) with first-class Arabic / RTL support.

> **Status — Phase 6A (Enterprise development workflow).** The app is feature-
> complete across capture, organisation, security, and flagship surfaces, and now
> ships a full testing, static-analysis, CI/CD, and documentation layer. See
> `ARCHITECTURE_STATE.md` for the authoritative, always-current component map.

---

## Highlights

- **Offline-first.** Every card lives on-device in Room; no account required.
- **Capture.** CameraX + ML Kit scanning, image import, and manual entry, with
  duplicate detection and a barcode/QR renderer (ZXing).
- **Organise.** Favourites, pinning, archiving, categories, smart ordering,
  multi-field search (sub-100 ms for realistic wallets).
- **Secure.** App Lock (biometric / PIN / device credential), Android Keystore
  encryption, password-encrypted backups, screenshot protection.
- **Flagship.** Home-screen widgets, a notification engine, an import wizard
  (JSON / CSV / image / encrypted) with undo, plus architecture for cloud sync,
  Wear OS, and future AI.
- **Gulf-ready.** Bilingual English/Arabic, RTL-aware, store catalogue tuned for
  regional retailers.

## Tech stack

Kotlin 2.0.21 · Jetpack Compose (Material 3) · Clean Architecture + MVVM · Hilt ·
Room · DataStore · Navigation Compose · Coroutines/Flow · kotlinx.serialization ·
ZXing · CameraX + ML Kit · Timber. minSdk 26, target/compile SDK 35. AGP 8.7.3.

## Module / package layout

```
com.universalwallet.loyalty
├── core/        theme, components, ui, cards, barcode, plugin, organize,
│                search, wallet, share, export, security, backup, sync, ai,
│                notifications, migration, wear, settings, developer, result
├── data/        Room database, entities, DAOs, mappers, repositories
├── domain/      models + repository contracts (framework-agnostic)
├── feature/     per-screen Compose UI + ViewModels
├── di/          Hilt modules
└── widget/      home-screen widgets
```

## Getting started

```bash
git clone https://github.com/acbikram/UniversalLoyaltyWallet.git
cd UniversalLoyaltyWallet
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew detekt ktlintCheck     # static analysis (advisory)
```

First open in Android Studio triggers a Gradle sync that regenerates the wrapper
and runs Hilt/Room codegen. See **[DEVELOPMENT.md](DEVELOPMENT.md)** for setup,
emulator requirements, debugging, and the release checklist.

## Documentation

| Doc | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Layers, data flow, diagrams |
| [ARCHITECTURE_STATE.md](ARCHITECTURE_STATE.md) | Live component inventory |
| [CONTRIBUTING.md](CONTRIBUTING.md) | How to contribute |
| [CODE_STYLE.md](CODE_STYLE.md) | Conventions, detekt/ktlint |
| [DEVELOPMENT.md](DEVELOPMENT.md) | Setup, build, debug, release |
| [SECURITY.md](SECURITY.md) | Security model + reporting |
| [PLUGIN_DEVELOPMENT_GUIDE.md](PLUGIN_DEVELOPMENT_GUIDE.md) | Add a store plugin |
| [BACKUP_FORMAT.md](BACKUP_FORMAT.md) / [JSON_SCHEMA.md](JSON_SCHEMA.md) | Backup format + schema |
| [CHANGELOG.md](CHANGELOG.md) / [ROADMAP.md](ROADMAP.md) | History + plans |

## License

See repository license. Store names and logos are trademarks of their owners and
are used only to identify cards the user already holds.

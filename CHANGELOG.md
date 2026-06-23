# Changelog

All notable changes to Universal Loyalty Wallet. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); this project will adopt
semantic versioning at its first tagged release.

## [Unreleased]

### Phase 6B — Final integration, release, privacy & monetization
- Centralized **feature-flag system** (`core/featureflags`: catalogue + manager)
  spanning Free / Premium / Experimental / Developer, with runtime overrides and
  tier gating (card/widget limits).
- **Monetization architecture** (`core/billing`): premium model, entitlement +
  billing-gateway interfaces, and a free-by-default `LocalEntitlementProvider`
  (no billing logic; ready for Google Play Billing). Bound via `di/BillingModule`.
- **Localization framework**: `values-ar/strings.xml`, `res/xml/locales_config.xml`
  (Android 13+ per-app language), `core/locale` (`LocaleManager`, `LocalizedFormat`,
  `AppLanguage`); RTL already enabled.
- Release config confirmed (file-guarded signing, R8 + resource shrinking,
  versioning) + `keystore.properties.template`.
- Documentation: PRIVACY, DATA_SAFETY, PLAY_READINESS, RELEASE, DEPLOYMENT,
  USER_GUIDE, ADMIN_GUIDE, FAQ; production audit + final ARCHITECTURE_STATE.

### Phase 6A — Testing, CI/CD, quality & documentation
- Added unit tests for ViewModels (fake-repository harness), export/import
  round-trip, card formatting, notification preferences, and JVM performance
  budgets; plus Compose UI tests for shared components.
- Integrated **detekt** and **ktlint** with config (`config/detekt/detekt.yml`,
  `.editorconfig`) and Gradle wiring.
- Added GitHub Actions: `ci.yml` (build, unit tests, detekt, ktlint, artifacts;
  optional emulator UI tests) and `release.yml` (release APK/AAB on tags).
- Authored the full documentation set (architecture, contributing, code style,
  security, plugin guide, backup format, JSON schema, development guide,
  changelog, roadmap).

### Phase 5B — Flagship layer
- Home-screen widgets (Quick Scan, Favourite Card) via RemoteViews.
- Notification engine with channels, per-type settings, and permission awareness.
- Import wizard (JSON / CSV / image / encrypted) with preview and undo.
- Cloud sync **architecture** (interfaces + local-only impls, pure
  ConflictResolver), AI-ready **interfaces**, Wear OS **contracts**.
- Migration engine (registry + validator + coordinator); settings expansion;
  premium-UX helpers (adaptive layout, glass surface); extended developer tools.

### Phase 5A — Security
- App Lock (biometric / PIN / device credential), session management, Android
  Keystore encryption, password-encrypted backup + validated restore preview,
  screenshot protection, hidden Developer Mode.

### Phase 4A/4B — Capture & Smart Wallet
- CameraX + ML Kit scanning, image import, duplicate detection, ZXing rendering.
- Smart ordering, favourites/pinning/archiving, categories, search ranking,
  JSON export/import, Room v2 migration.

### Phase 1–3 — Foundation, data, UI
- Build system, theme/design system, navigation, Hilt graph; Room data layer;
  Compose screens and components.

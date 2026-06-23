# ARCHITECTURE_STATE.md

_Universal Loyalty Wallet — living architecture snapshot. Updated through Part 5A._

## Overview

Offline-first Android loyalty-card wallet. Clean Architecture + MVVM, Jetpack
Compose (Material 3), Hilt, Room, DataStore, Navigation Compose, Kotlin
coroutines/Flow. Single-activity (`MainActivity` is now a `FragmentActivity` to
host BiometricPrompt). Package root `com.universalwallet.loyalty`.

## Layers

- **domain** — framework-agnostic models (`LoyaltyCard`, `StoreDefinition`,
  `BarcodeType`, `CardCategory`) and repository contracts.
- **data** — Room (`AppDatabase` v2), entities, DAOs, mappers, repository impls,
  JSON store catalogue.
- **core** — reusable building blocks (theme, components, ui, cards, barcode,
  plugin, organize, search, wallet, share, export, **security**, **backup**,
  **developer**).
- **feature** — per-screen Compose UI + ViewModels.
- **di** — Hilt modules.

## Part 5A — Security components (new)

### `core/security`
- `SecurityModels` — `AuthMethod`, `SecurityConfig`, `AuthResult`, `SecurityError`.
- `SecuritySettings` — DataStore persistence for all security prefs (PIN stored as salted hash only).
- `PinHasher` — pure PBKDF2 salted hashing + constant-time verify.
- `PasswordCrypto` — pure PBKDF2 + AES-GCM for password-encrypted backups.
- `Encryptor` (interface) + `KeystoreEncryptionManager` — Android Keystore AES-256/GCM, versioned payloads, `rotateKey()` for future key rotation.
- `BiometricAuthenticator` — wraps AndroidX BiometricPrompt; availability + authenticate.
- `SessionPolicy` (pure) + `SessionManager` — lock state, idle/background/grace logic.
- `PinManager` — set/verify/clear PIN.
- `SecurityManager` — facade: `config`, `isLocked`, lifecycle hooks, debug-log gate.
- `AppLog` — gated logger (off by default; never logs sensitive data).

### `core/backup`
- `BackupModels` — `ValidationResult`, `ConflictInfo`, `RestorePreview`.
- `ImportValidator` (pure) — validates a parsed export.
- `BackupManager` — JSON backup with optional password encryption; `CloudBackupTarget` interface (architecture only, no live cloud).
- `RestoreManager` — read → decrypt → validate → preview → apply, with conflict detection.

### `core/developer`
- `DeveloperModeManager` — DB inspector, store/plugin validators, demo-card generator, debug-logging toggle, memory snapshot.

### Managers (the requested set)
SecurityManager, EncryptionManager (`KeystoreEncryptionManager`), BiometricManager (`BiometricAuthenticator`), SessionManager, BackupManager, RestoreManager, ExportManager (Part 4B, reused), ImportValidator, DeveloperModeManager. Plus PinManager and SecuritySettings.

### Settings (new DataStore keys)
`APP_LOCK_ENABLED`, `AUTH_METHOD`, `AUTO_LOCK_TIMEOUT_MS`, `LOCK_ON_BACKGROUND`,
`REQUIRE_AUTH_ON_LAUNCH`, `SCREENSHOT_PROTECTION`, `CLIPBOARD_PROTECTION`,
`PIN_HASH`, `PIN_SALT`, `DEVELOPER_MODE_ENABLED`, `DEBUG_LOGGING`.

### Feature screens (new)
- `feature/lock` — `LockScreen` + `LockViewModel`.
- `feature/security` — `SecuritySettingsScreen` + `SecuritySettingsViewModel`.
- `feature/developer` — `DeveloperModeScreen` + `DeveloperModeViewModel`.

### Navigation
- `SecurityCenter` route → `SecuritySettingsScreen`.
- `Developer` route → `DeveloperModeScreen`.
- Settings → "Security & App Lock" → SecurityCenter.
- `MainActivity` overlays `LockScreen` while `SecurityManager.isLocked`.

### DI
- `SecurityModule` binds `Encryptor` → `KeystoreEncryptionManager`.
- All other managers are constructor-injectable (resolve existing providers:
  `DataStore<Preferences>`, `@IoDispatcher`, `@ApplicationScope`, repositories,
  `ExportManager`, `ImportManager`, `ZxingBarcodeEncoder`).

## Data model / schema

Room v2. `LoyaltyCard` fields: identity + barcode + organization
(`isPinned/isArchived/isHidden/usageCount/sortIndex`, Part 4B). `MIGRATION_1_2`
adds the 4B columns. **No schema change in 5A.**

## Part 5B — Flagship layer (widgets, sync, notifications, migration, AI, Wear)

All additive; no architecture redesign. No new Gradle dependencies (framework
APIs + existing libs only).

### `core/sync` — Cloud sync architecture (interfaces + local-only impls)
Models (`RemoteCard` with `updatedAt`/`deleted` tombstone, `RemoteStore`,
`DeviceInfo`, `SyncStatus`, `MergeStrategy`, `SyncOperation`, `SyncResult`,
`SyncError`). Contracts (`CloudSyncProvider`, `AuthenticationProvider`,
`DeviceIdentity`, `SyncQueue`, `SyncCrypto`). Pure `ConflictResolver`
(last-write-wins + tombstones, `merge()` → winners + conflicts). Default impls:
`LocalOnlyCloudSyncProvider` (never authenticated), `NoopAuthenticationProvider`,
`InMemorySyncQueue` (Mutex), `DeviceIdentityImpl` (ANDROID_ID). `SyncManager`
orchestrates pull → merge → push and short-circuits to NOT_CONFIGURED until a
real provider is bound. `di/SyncModule` binds all four. **No backend ships.**

### `core/ai` — AI-ready architecture (interfaces only, no impls)
`CardRecognizer`, `OcrEngine`, `ReceiptRecognizer`, `OfferRecommender`,
`CardSuggester`, `DuplicateDetectorAi`, `SmartSearchEngine`,
`NaturalLanguageSearch`, `VoiceSearchEngine`, `VoiceCommandInterpreter`, plus
`AiCapabilities` and platform-agnostic models. No DI bindings (nothing injects
them yet, by design).

### `core/notifications` — Notification engine (concrete)
`NotificationChannelDef` (4 channels), `NotificationType` (8 types incl. future
offer/points-expiry), `NotificationContent`, `NotificationScheduler` (interface;
WorkManager-backed in future). `NotificationSettings` (DataStore, per-type +
master). `NotificationEngine` creates channels, honours settings + OS permission,
deep-links via immutable PendingIntent. Channels created in
`UniversalWalletApp.onCreate`.

### `core/migration` — Migration engine
`DataMigrationStep` + `SettingsMigration`/`PluginMigration`/`TemplateMigration`
marker interfaces, `MigrationValidator` (pure chain-continuity check),
`MigrationRegistry` (Room migrations surfaced for validation; schema v2, data v1;
empty data-migration list), `MigrationCoordinator` (runs pending data migrations
using `APP_DATA_VERSION`; ready to wire into startup).

### `widget` — Home-screen widgets (RemoteViews, no Glance)
`QuickScanWidget` (stateless launcher), `FavoriteCardWidget` (reads favourite/
most-used via Hilt `WidgetEntryPoint` + `goAsync`), `WidgetConfigActivity`,
`WidgetUpdater`. Resources: 2 layouts, 2 drawables (rounded), 2 provider-info
XML, strings. Manifest: 2 receivers + config activity.

### `core/wear` — Wear OS architecture (contracts only, no `:wear` module)
`WearCardSummary`, `WearScreenShape`, `WearAmbientState`, `WearSettings`,
`WearSyncInterface`, `WearCacheContract`, `WearComplicationProvider`, mappers.
Intended to be consumed by a future `:wear` Gradle module (round/square, ambient).

### Import wizard
`core/importer/CsvImporter` (pure, tolerant). `feature/importwizard`
(ViewModel + screen): JSON/encrypted (reuses 5A `RestoreManager`), CSV, image
(reuses 4A decoder), Google/Apple Wallet placeholders, preview, duplicate policy,
and **undo** (before/after id diff). Route + Settings entry wired.

### Settings expansion
`core/settings/FeatureSettings` (DataStore: widgets/wear/cloud/reduced-motion/
experimental/accent). `feature/experimental` screen (Surfaces / Accessibility /
Experimental). `feature/notifications` settings screen. New DataStore keys for
all of the above + `APP_DATA_VERSION`. Settings screen gained Import wizard,
Notifications, and Features & accessibility entries.

### Premium UX
`core/ui/LayoutAdaptation` (content max-width + adaptive padding by width class,
complementing existing `Responsive`), `core/ui/GlassSurface` (frosted surface +
API-31-gated `glassBlur`).

### Developer tools (extended)
`DeveloperModeManager` gained `sendTestNotification()`, `simulateSync()`
(drives `ConflictResolver`), and `validateArchitecture()` (drives
`MigrationValidator`). Surfaced in the Developer screen.

### Navigation (new routes)
`ImportWizard`, `NotificationSettings`, `Experimental` (plus 5A `SecurityCenter`,
`Developer`).

### Wired vs architecture-only (5B)
- **Wired & functional:** notification engine + channels, notification settings,
  widgets, import wizard, experimental/feature settings, dev-tool extensions,
  premium-UX helpers.
- **Architecture/interfaces only:** cloud sync (local-only provider → no network),
  AI (no impls), Wear (contracts, no module), `MigrationCoordinator` (ready, not
  yet called from startup), `NotificationScheduler` (interface; no WorkManager).



- **Field-level at-rest encryption of card numbers**: `KeystoreEncryptionManager`
  is ready, but turning on card-number encryption requires deterministic
  equality (e.g. an HMAC `cardNumberHash` column) so duplicate-detection and
  search keep working — a v3 migration. Notes/customerName field encryption can
  land first. Currently **encryption is fully wired for backups**, not yet for
  the live card columns.
- **Live cloud backup**: only the `CloudBackupTarget` interface exists.
- **Clipboard auto-clear**: setting persists; the timed-clear worker is a stub to implement.
- **Manual drag-reorder gesture UI** (Part 4B engine exists).
- **Key rotation execution**: `rotateKey()` exists; background re-encryption job not yet scheduled.
- **Reduced-motion**: animations kept short; explicit system-setting check pending.
- **Widget / app-lock-per-card**: not started.

## Part 6B — Final integration, release, privacy & monetization

Final phase. Additive; no architecture redesign, no new Gradle dependencies.

### Monetization (`core/billing`)
`PremiumFeature` (5 features), `Entitlement` (+`FREE`/`fullPremium`),
`EntitlementSource`, `FreeTierLimits`. Interfaces `EntitlementProvider` and
`BillingGateway` (+`PremiumProduct`/`PurchaseResult`) define the future Google
Play Billing seam — **no billing logic ships**. `LocalEntitlementProvider`
(DataStore-backed, free by default) is bound in `di/BillingModule`, closing the
graph.

### Feature flags (`core/featureflags`)
`FeatureCatalog` enumerates every flag across FREE/PREMIUM/EXPERIMENTAL/DEVELOPER;
`FeatureFlagManager` (@Singleton) combines persisted settings + entitlement +
in-memory runtime overrides into `isEnabled(feature): Flow<Boolean>` and tier
gating (`canAddCard`/`canAddWidget`, limits). Feature code asks the manager rather
than checking tiers directly.

### Localization (`core/locale` + resources)
`AppLanguage` (system/en/ar), `LocaleManager` (persists BCP-47 tag, `wrap()` for
`attachBaseContext`), `LocalizedFormat` (locale-aware date/number/percent + RTL
check). `res/values-ar/strings.xml` (complete mirror of the externalized strings),
`res/xml/locales_config.xml` + manifest `android:localeConfig` (Android 13+ system
language picker, no code needed). `supportsRtl=true` already set. New prefs keys:
`PREMIUM_UNLOCKED`, `APP_LANGUAGE`.

### Release configuration
Already established and confirmed: file-guarded `release` signing config
(`keystore.properties`, git-ignored) + `keystore.properties.template`; R8
`isMinifyEnabled` + `isShrinkResources`; `proguard-rules.pro` keeps; debug
`.debug` suffix; `versionCode 1` / `versionName 1.0.0`.

### Documentation (final set)
PRIVACY, DATA_SAFETY, PLAY_READINESS, RELEASE, DEPLOYMENT, USER_GUIDE,
ADMIN_GUIDE, FAQ — plus the existing architecture/contributing/security/plugin/
backup docs. 20 docs total.

---

## Final architecture summary

Single-activity Jetpack Compose app, **Clean Architecture + MVVM**, Hilt-wired,
Room single-source-of-truth, offline-first. Layers: `feature` (UI+VM) → `domain`
(models+contracts) ← `data` (Room/impls); `core` holds reusable engines/managers;
`di` wires at the edges; `widget` for home-screen surfaces. ~222 main Kotlin
files, 34 unit-test files, 3 instrumented.

### Completed features
Capture (scan/image/manual + duplicate detection + ZXing render), organisation
(favourites/pin/archive/hide/categories/smart order/search), security (App Lock,
Keystore, sessions, password-encrypted backup + restore preview, screenshot
protection, Developer Mode), flagship (widgets, notification engine, import wizard
with undo), settings + premium UX, testing + CI/CD + static analysis, and the 6B
release/privacy/feature-flag/monetization/localization layer.

### Architecture-only (interfaces / inert by design)
Cloud sync (local-only provider, no INTERNET permission), AI (interfaces only),
Wear OS (contracts, no `:wear` module), billing (interfaces; free local provider).

### Known limitations
- At-rest card-number column encryption not yet enabled (needs Room v3 + a
  deterministic hash column for search/dedupe).
- Most Compose UI strings are inline; the localization framework is in place but
  full string externalization is incremental (only 14 strings externalized today).
- detekt/ktlint run advisory (not blocking) pending a clean baseline.
- `MigrationCoordinator`/`NotificationScheduler` present but not yet invoked from
  startup/WorkManager.
- Builds verified structurally only here; a Gradle sync is required on first open.

### Suggested future roadmap
- **v2.0:** official retailer integrations (where APIs allow), opt-in cloud sync,
  Wear OS sync, OCR-assisted import, smart coupon management.
- **v3.0:** household sharing, cross-platform/desktop companion, on-device
  AI-assisted organisation.

## Part 6A — Testing, CI/CD, static analysis & documentation

Additive; no source architecture change. The only build edits are the addition of
**detekt** and **ktlint** plugins (version catalog + root/app apply + config
files) — these require a Gradle sync to take effect.

### Tests (JVM unit — `app/src/test`)
New: `MainDispatcherRule`, `FakeLoyaltyCardRepository`, `SearchViewModelTest`
(StateFlow via Turbine), `CardFormattingTest`, `NotificationPrefsTest`,
`ExportRoundTripTest`, `PerformanceBudgetTest` — joining the existing suites for
**34 unit-test files**.

### Tests (instrumented / Compose UI — `app/src/androidTest`)
`ComponentsUiTest` (createComposeRule; rendering, clicks, state changes, labels)
alongside the existing `AppThemeTest`. No Hilt test runner needed.

### Static analysis
`config/detekt/detekt.yml` (overlays defaults, Compose-aware), `.editorconfig`
(ktlint). Applied via Gradle plugins; advisory in CI initially.

### CI/CD (`.github/workflows`)
`ci.yml` (build, unit tests, detekt+ktlint advisory, artifacts, on-demand
emulator UI tests) and `release.yml` (release APK/AAB on `v*` tags); complements
the pre-existing `build.yml`.

### Documentation
README (refreshed), CONTRIBUTING, ARCHITECTURE, CODE_STYLE, DEVELOPMENT, SECURITY,
CHANGELOG, ROADMAP, PLUGIN_DEVELOPMENT_GUIDE, BACKUP_FORMAT, JSON_SCHEMA.

### Quality metrics (current)
- Unit-test files: 34; instrumented: 2. High coverage on pure engine/manager/
  mapper layers (≥80% target applies to testable non-UI code; device-bound code
  excluded, verified structurally).
- detekt + ktlint integrated (advisory → blocking once baseline is clean).

### Remaining (6A follow-ups)
- detekt/ktlint baseline → make blocking.
- Macrobenchmark module for device startup/scroll/render with CI baselines.
- Hilt-instrumented Compose tests for full stateful screens (custom test runner).

## Testing

Pure JVM unit tests cover: barcode validation/symbology, smart-wallet scoring,
sorting, filtering, recent bucketing, search ranking, PIN hashing, password
crypto round-trip, session policy, import validation, **sync conflict resolution
(last-write-wins, tombstones, manual conflicts, merge), migration-chain
validation, CSV import parsing, and the SyncManager local-only path** (27 test
files). Android-dependent code (Keystore, BiometricPrompt, CameraX/ML Kit, DAOs,
widgets/RemoteViews, notification posting) is verified structurally and must be
exercised on a device.

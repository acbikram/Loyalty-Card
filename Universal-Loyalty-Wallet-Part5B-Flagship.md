# Universal Loyalty Wallet — Part 5B

## Flagship: Widgets · Wear OS · Cloud Sync · Notifications · AI-Ready · Import Wizard · Migration · Premium UX

This is the design companion to the Part 5B code. It builds on Parts 1–5A
**without redesigning anything**. The guiding principle for this phase: the
prompt itself asks for *architecture/interfaces* for cloud, AI, and Wear, and
*concrete* implementations for widgets, notifications, the import wizard, and
settings. That split is exactly what shipped — and crucially, **no new Gradle
dependencies were added**, so the build surface is unchanged and everything
relies on framework APIs (RemoteViews, NotificationCompat) and libraries already
in the project.

---

## 1. Home-screen widgets

Two widgets ship, built on classic `RemoteViews` + `AppWidgetProvider` (not
Glance — that would add a dependency). `QuickScanWidget` is stateless: a tap
launches the app, so it renders instantly and never touches data.
`FavoriteCardWidget` shows the favourite (or most-used) card; because Hilt can't
inject a broadcast receiver, it reaches the repository through a Hilt
`WidgetEntryPoint` inside `goAsync()`, querying off the main thread and masking
the card number. A `WidgetConfigActivity` satisfies the configuration contract
(and is where a future per-widget card picker lands), and `WidgetUpdater`
refreshes placed widgets after data changes. Resources provide rounded
backgrounds; full Material You dynamic-colour theming is noted as an enhancement
(API-31 system colours).

---

## 2. Wear OS architecture

Provided as **contracts and shared models**, not a separate Gradle module — a
live `:wear` module would add Wear-Compose dependencies and a second build target
that can't be verified here. `core/wear` defines `WearCardSummary` (a
watch-friendly projection), `WearSyncInterface` (the phone↔watch Data Layer
abstraction), `WearCacheContract` (offline cache), `WearComplicationProvider`,
and round/square/ambient enums, plus mappers. A future `:wear` module implements
the watch UI (round/square aware, ambient mode, battery-optimised) against these.

---

## 3. Cloud sync architecture (no backend)

The full pipeline exists behind interfaces. `RemoteCard` carries `updatedAt` and
a `deleted` tombstone so edits and deletions both propagate. `ConflictResolver`
is pure and unit-tested: last-write-wins by timestamp, with tombstones treated as
ordinary timestamped writes, plus prefer-local/prefer-remote/manual strategies
and a set-level `merge()`. `SyncManager` orchestrates pull → merge → push and
drains the offline `SyncQueue`. The default `LocalOnlyCloudSyncProvider` is never
authenticated, so `SyncManager` short-circuits to `NOT_CONFIGURED` — the app is a
local-only wallet today, with multi-device, encrypted, incremental sync ready to
switch on once a real `CloudSyncProvider` + `AuthenticationProvider` are bound.
`SyncCrypto` is the hook to reuse the Part 5A Keystore/password crypto for
end-to-end encryption.

---

## 4. Notification engine

Concrete and wired. Four channels (reminders, security, updates, offers) are
created on startup. `NotificationType` enumerates eight notifications, including
future offer/points-expiry. `NotificationEngine.notify()` respects both the
per-type `NotificationSettings` (DataStore) **and** the OS-level permission, then
posts a deep-linking notification via an immutable `PendingIntent`. It never
includes card numbers in the text. Scheduling (periodic backup/unused-card
reminders) is expressed as a `NotificationScheduler` interface — the immediate
path works now; a WorkManager-backed scheduler is future work (WorkManager was
deliberately not added).

---

## 5. AI-ready architecture

Interfaces only, **no implementations**, exactly as specified: card recognition,
OCR, receipt parsing, offer recommendation, smart/natural-language search, card
suggestions, AI duplicate detection, voice search, and voice commands, plus an
`AiCapabilities` availability probe. Inputs are platform-agnostic (a URI string
or audio path) so the contracts carry no Android dependency and a future
on-device or cloud model can plug in without feature-code changes. No DI bindings
exist because nothing should resolve them yet.

---

## 6. Import wizard

A real multi-step feature: pick a source → preview → import → result with
**undo**. JSON and encrypted backups reuse the Part 5A `RestoreManager` (with its
validation + restore-preview); CSV uses the pure, tolerant `CsvImporter` (optional
header, quoted fields, sensible defaults); image import reuses the Part 4A
barcode decoder. Google/Apple Wallet appear as clearly-disabled future sources.
Undo is robust and source-agnostic: the wizard snapshots card ids before and
after the import and removes exactly the new ones, so a wrong import is one tap to
reverse.

---

## 7. Migration engine

`MigrationRegistry` is the single source of truth: it surfaces the Room schema
migrations (currently `MIGRATION_1_2`, schema v2) for visibility/validation and
holds an ordered list of app-*data* migrations (settings/plugin/template) — empty
today, with the machinery ready. `MigrationValidator` is pure and tested: it
proves a chain is gap-free and advances one version at a time.
`MigrationCoordinator` runs pending data migrations against a stored
`APP_DATA_VERSION` and is ready to be called from startup.

---

## 8. Settings expansion & premium UX

`FeatureSettings` (DataStore) backs widget/Wear/cloud/reduced-motion/experimental
toggles and an accent-colour slot; the new **Features & accessibility** screen
surfaces them, a **Notifications** screen owns notification prefs, and both are
linked from Settings alongside the **Import wizard** entry. Premium UX adds
adaptive helpers (content max-width + padding by width class, complementing the
existing responsive columns for tablet/foldable/landscape) and a `GlassSurface`
with an API-31-gated `glassBlur` so the frosted effect degrades gracefully on
older devices.

---

## 9. Developer tools (extended)

Developer Mode gains a **notification tester** (posts through the real engine), a
**sync simulator** (drives `ConflictResolver` with two timestamped versions to
demonstrate last-write-wins and conflict counts, no network), and an
**architecture validator** (drives `MigrationValidator` over the registry). These
sit alongside the 5A database inspector, store/plugin validators, demo-card
generator, debug-logging toggle, and performance monitor.

---

## 10. Self-review (per the brief)

- **Compile verification (structural):** package declarations on all 200+ files;
  targeted import checks across every new manager/screen/widget; `@OptIn`
  annotations on all Material-3 screens; widget resources/manifest entries
  cross-checked; navigation passes all seven Settings callbacks.
- **Dependency verification:** no new dependencies; everything resolves against
  framework APIs (RemoteViews, NotificationCompat, AppWidget) and existing libs
  (DataStore, Hilt, Compose, kotlinx-serialization). WorkManager/Glance/Wear were
  deliberately avoided.
- **Architecture verification:** Clean Architecture preserved; cloud/AI/Wear are
  abstractions; `di/SyncModule` closes the sync graph; AI has no bindings by
  design.
- **Accessibility:** content descriptions on widgets/icons, a reduced-motion
  setting, large touch targets, system-settings guidance on the notifications
  screen.
- **Performance:** widgets render off the main thread via `goAsync`; no polling;
  notification engine does no work when disabled; sync short-circuits when not
  configured.
- **Security:** notifications never contain card numbers; sync encryption is an
  interface ready to reuse 5A crypto; no secrets logged.
- `ARCHITECTURE_STATE.md` updated.

---

## Final checklist

| Item | Status |
|---|---|
| Widgets complete | ✓ (RemoteViews; Quick Scan + Favourite + config) |
| Wear OS architecture complete | ✓ (contracts/models; no separate module) |
| Cloud Sync interfaces complete | ✓ (+ local-only impls, pure ConflictResolver) |
| Notification engine complete | ✓ (channels, settings, permission-aware) |
| AI-ready architecture complete | ✓ (interfaces only) |
| Migration engine complete | ✓ (registry, validator, coordinator) |
| Import wizard complete | ✓ (JSON/CSV/image/encrypted, preview, undo) |
| Settings expansion complete | ✓ (features/accessibility/notifications) |
| Premium UX complete | ✓ (adaptive helpers + glass) |
| Developer tools complete | ✓ (notif tester, sync sim, arch validator) |
| Tests | ✓ (ConflictResolver, MigrationValidator, CsvImporter, SyncManager) |

---

## Honest verification note

Verified by **structure and static analysis only** — no Gradle/Android build was
run (no Android SDK, network disabled), so a Gradle sync is required on first open
(it regenerates the wrapper and runs Hilt/Room/KSP codegen). What this means
concretely for 5B:

- **Architecture-only, by request:** cloud sync (local-only provider → no network
  calls happen; the pipeline is real but inert until a backend is bound), the AI
  layer (interfaces, no implementations), and Wear OS (contracts, no `:wear`
  Gradle module). `MigrationCoordinator` and `NotificationScheduler` are in place
  but not yet invoked from startup/WorkManager.
- **Device-dependent, test on hardware:** widgets (RemoteViews rendering,
  `AppWidgetProvider` broadcasts, the Hilt entry point, config activity),
  notification posting (channels + the runtime POST_NOTIFICATIONS permission on
  Android 13+), and the import wizard's `contentResolver` file reads.
- **Functional and unit-tested:** the pure logic — conflict resolution, migration
  validation, CSV parsing — plus the wired settings/notification/import
  ViewModels.

No new dependencies were introduced, so this phase does not change the project's
build/risk surface; the main first-open step remains the standard Gradle sync.

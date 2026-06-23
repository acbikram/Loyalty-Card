# Architecture

Universal Loyalty Wallet follows **Clean Architecture + MVVM** with a single-
activity, Jetpack Compose UI. This document is the narrative companion to the
machine-current `ARCHITECTURE_STATE.md`.

## Layers

```
┌─────────────────────────────────────────────────────────────┐
│  feature/ (Compose screens + ViewModels)   widget/           │
│     observes StateFlow, sends events, no Android in logic     │
├─────────────────────────────────────────────────────────────┤
│  domain/ (models + repository interfaces)                     │
│     framework-agnostic; the dependency inversion boundary     │
├─────────────────────────────────────────────────────────────┤
│  data/ (Room, DAOs, entities, mappers, repository impls)      │
│  core/ (engines + managers: security, sync, export, …)        │
├─────────────────────────────────────────────────────────────┤
│  di/ (Hilt modules wire everything at the edges)              │
└─────────────────────────────────────────────────────────────┘
```

**Rule of dependencies:** `feature` → `domain` ← `data`. Features depend on
domain interfaces, never on Room. `core` holds reusable, mostly-pure engines and
the manager classes that orchestrate Android APIs.

## Data flow (read)

```
Room (single source of truth)
  └─ DAO  →  RepositoryImpl  →  Flow<List<LoyaltyCard>>
                                   └─ ViewModel (combine/transform)
                                        └─ StateFlow<UiState>
                                             └─ Compose screen (collectAsStateWithLifecycle)
```

## Data flow (write)

```
Screen event → ViewModel → Repository (suspend, returns DataResult)
   → DAO upsert → Room emits → read flow updates → UI recomposes
```

`DataResult<T>` wraps expected failures (`Success`/`Failure(AppError)`); exceptions
are reserved for the truly exceptional.

## Key building blocks

- **Theme/design system** (`core/theme`, `core/components`): Material 3 tokens,
  `Spacing`, `WalletIcons`, reusable components (buttons, list items, dialogs,
  empty/error states, card tiles).
- **Barcode** (`core/barcode`): symbology mapping, validation, ZXing encoder,
  ML Kit analyzer, image decoder.
- **Plugin system** (`core/plugin`): `StorePluginRegistry` resolves a
  `StorePluginContract` per store — the extension point for new retailers.
- **Organisation** (`core/organize`, `core/search`, `core/wallet`): pure engines
  for sorting, filtering, recency, search ranking, and smart ordering.
- **Security** (`core/security`, `core/backup`): App Lock, Keystore encryption,
  sessions, backup/restore. See `SECURITY.md`.
- **Flagship** (`core/sync`, `core/ai`, `core/notifications`, `core/migration`,
  `core/wear`, `widget`): cloud-sync and AI architecture (interfaces), a concrete
  notification engine, the migration engine, Wear contracts, and home-screen
  widgets. See `Universal-Loyalty-Wallet-Part5B-Flagship.md`.

## Navigation

A sealed `WalletDestination` enumerates routes; `WalletNavHost` maps each to a
screen. The single `MainActivity` (a `FragmentActivity`) hosts the nav graph,
applies the theme, gates the lock screen, and feeds session lifecycle.

## Concurrency

- Coroutines + Flow throughout; dispatchers injected via `@IoDispatcher` /
  `@DefaultDispatcher`, an application scope via `@ApplicationScope`.
- ViewModels expose cold→hot `StateFlow` with `WhileSubscribed`; one-shot effects
  use a channel observed by `ObserveAsEvents`.

## Testing strategy

Pure engines/managers are unit-tested on the JVM (no Android). ViewModels are
tested with fake repositories + `kotlinx-coroutines-test` + Turbine. Presentational
Compose components have UI tests via `createComposeRule`. Device-dependent code
(Keystore, BiometricPrompt, CameraX/ML Kit, Room DAOs, widgets, notifications) is
verified structurally and exercised on a device/emulator. See `CONTRIBUTING.md`.

## Non-goals / boundaries
- No server dependency for core features (offline-first).
- Cloud sync, AI, and Wear are **architecture/interfaces** today; see the roadmap.

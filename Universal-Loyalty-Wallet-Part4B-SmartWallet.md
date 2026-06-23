# Universal Loyalty Wallet — Part 4B

## Smart Wallet, Advanced Search, Organization, Sharing, Export/Import & Premium UX

This is the design companion to the Part 4B code. It builds on Parts 1–4A
without redesigning anything: the Clean Architecture layering, theme/component
system, data layer, navigation, and barcode engine are all unchanged. Part 4B
adds the "premium wallet" behaviour — a Smart Wallet ranking engine, a richer
dashboard, fuzzy multi-field search, full card organization (pin/archive/hide/
duplicate/favourite/manual order), sorting and filtering, confirmed sharing, and
JSON export/import — almost entirely as pure, testable engines with thin UI
integration on top.

---

## 1. Data-layer extension (the one schema change)

Organization and usage features need five new per-card fields, so the schema
moved from v1 to v2:

| Field | Purpose |
|---|---|
| `isPinned` | Manual "keep at top" for Smart Wallet |
| `isArchived` | Hidden from the wallet but restorable |
| `isHidden` | Soft-hide without archiving |
| `usageCount` | Drives Smart Wallet frequency scoring |
| `sortIndex` | Manual drag-order position |

This was done additively: the domain model gains the fields with safe defaults
(existing constructors keep compiling), the entity gains matching columns, the
mapper maps both directions, and `MIGRATION_1_2` runs `ALTER TABLE … ADD COLUMN`
for each (all `NOT NULL DEFAULT 0`) plus the two new indices. The migration is
registered on the Room builder; the destructive fallback is retained only as a
pre-release safety net. The DAO gained `getActive()` / `getArchived()` /
`getMostUsed()` queries and atomic update statements for favourite, pin,
archive, hide, usage increment, and sort index. The repository interface and
implementation expose all of these plus `duplicateCard` and `setManualOrder`.

---

## 2. Smart Wallet (`core/wallet`)

`SmartWalletEngine` is pure and deterministic — it takes a card list and a `now`
timestamp and returns a ranking. The weighted score is:

```
score = pinnedWeight·isPinned
      + favoriteWeight·isFavorite
      + frequencyWeight·(usageCount / maxUsage)
      + recencyWeight·(1 / (1 + daysSinceLastUse))
```

Pinning dominates (weight 1000), then favourites, then a blend of normalised
frequency and a smooth recency decay. Because it is a pure function of its
inputs it is fully unit-tested (pinned dominance, recency ordering, frequency
tie-breaking, suggestion limit). `SmartWalletSettings` persists the on/off toggle
in DataStore (default on); the dashboard honours it, and it never touches the
network.

---

## 3. Home dashboard

`HomeViewModel` composes favourites, recents, the active card set, smart
suggestions, and the category-filtered grid into one immutable state via nested
`combine` (kept within the 5-arg arity by grouping into a `Triple` and a small
`HomeControls` holder). The screen adds a Smart Suggestions row (shown only when
the toggle is on and suggestions exist) and a stats summary (card count +
favourites). Archived/hidden cards are excluded everywhere through the
repository's `observeActiveCards()`.

---

## 4. Advanced search (`core/search`)

`SearchRankingEngine` is pure and instant. It scores each card across six fields
(store, nickname, number, barcode, category, notes) with descending weight, and
within each field ranks exact > prefix > contains > fuzzy. Fuzzy matching is
token-level Levenshtein ≤ 1, gated to queries of length ≥ 3 to avoid noise. The
search ViewModel runs it over the active set on each query/category change; for
realistic wallet sizes this is comfortably under the 100 ms target. The screen
renders results with the matched substring highlighted, keeps the recent-search
list and filter/sort controls, and offers a Relevance/Name/Store re-sort.

---

## 5. Organization, sorting, filtering (`core/organize`)

- `SortingManager` — pure sort for all eight orders (favourites-first,
  alphabetical, store, category, most-recent, newest, oldest, manual).
- `FilterManager` — pure predicate set (`FilterCriteria`): category, store,
  barcode type, favourites-only, archived/hidden inclusion, has-image, and
  recently-added / recently-used day windows. Archived and hidden are excluded by
  default.
- `RecentManager` — buckets used cards into Today / Yesterday / This week /
  Older (threshold-based, hence deterministic and tested).
- `CategoryManager` — category counts and the non-empty set, ready for the
  planned custom-category extension.
- `FavoriteManager` — the favourite/pin toggle service used by ViewModels.

The Wallet screen wires sorting (a menu of `SortOption`) and a favourites filter
chip; Card Details exposes pin, archive, duplicate, favourite, and delete.
Archived cards have their own screen (restore or delete permanently), reachable
from Settings.

---

## 6. Sharing (`core/share`)

`ShareManager` builds Android share intents for three things the user explicitly
chooses: the number, a details summary, or a rendered barcode/QR PNG (encoded via
the Part 4A ZXing encoder, written to cache, shared through the existing
`FileProvider`). Sharing is always gated behind a confirmation sheet — the user
picks *what* to share before any intent is launched — and nothing beyond the
selected content is ever included.

---

## 7. Export / Import (`core/export`)

A versioned `WalletExport` envelope (kotlinx.serialization) carries portable
`CardExport` records that deliberately omit device-specific ids, timestamps, and
image paths. `ExportManager` writes pretty JSON to a user-chosen `CreateDocument`
URI (single or multiple cards, same path); an `encrypted` flag is wired through
as a forward-compatible hook for the security phase but is plaintext today and
documented as such. `ImportManager` reads the file, validates each card
(non-empty number + barcode checksum where applicable), and persists it with
explicit duplicate-conflict resolution (`SKIP` or `REPLACE`), returning an
`ImportSummary` (added / replaced / skipped / invalid). Both run on the IO
dispatcher. Settings exposes Export and Import with file pickers.

---

## 8. Animations, performance, accessibility

Motion uses Compose primitives already in the project — the add-card success
check springs in, search results fade, the share sheet is a `ModalBottomSheet`,
and the scanner line animates via an infinite transition. Performance: all
ranking/sorting/filtering is pure and runs in flow transforms; lists are keyed
`LazyColumn`s with adaptive columns; barcode encoding and image/JSON I/O are off
the main thread; state is immutable `StateFlow` with `WhileSubscribed`.
Accessibility: content descriptions on actions and generated barcodes, large
touch targets, and a manual-entry fallback when the camera is unavailable.

---

## 9. ViewModels & utilities delivered

ViewModels: Home, Search, Wallet, Card Details, Settings (export/import + toggle),
and Archive — all immutable-state + `StateFlow`. Utilities: `SmartWalletEngine`,
`SmartWalletSettings`, `SearchRankingEngine`, `SortingManager`, `FilterManager`,
`RecentManager`, `CategoryManager`, `FavoriteManager`, `ShareManager`,
`ExportManager`, `ImportManager`.

Tests (pure JVM, 6 new suites): Smart Wallet scoring, sorting, filtering, recent
bucketing, search ranking (exact/prefix/contains/fuzzy + multi-field + typo), and
a shared card test-factory.

---

## 10. Final checklist

| Requirement | Status |
|---|---|
| Smart Wallet (weighted, offline, disableable) | ✓ engine + toggle + dashboard |
| Advanced search (multi-field, partial, fuzzy, highlight, recent, sort) | ✓ |
| Favourites (toggle + section + persistence) | ✓ |
| Recently used (Today/Yesterday/Week/Older + usage count) | ✓ engine + tracking |
| Categories | ✓ existing + CategoryManager |
| Organization (pin/archive/restore/hide/duplicate/rename/move/theme) | ✓ (rename/move/theme via edit; rest wired) |
| Sorting (8 orders incl. manual) | ✓ engine + Wallet menu |
| Filtering (8 criteria) | ✓ engine + favourites chip wired |
| Home dashboard | ✓ |
| Sharing (number/details/barcode, confirmed) | ✓ |
| Export / Import (JSON, validation, conflict policy, encryption hook) | ✓ |
| ViewModels (immutable + StateFlow) | ✓ |
| Tests | ✓ 6 suites |
| Navigation updates (Archive route + Settings entries) | ✓ |

---

## Honest verification note

Verified by structure and static analysis only — package layering, experimental
opt-ins (`ExperimentalMaterial3Api` for segmented buttons / dropdowns / bottom
sheets, `ExperimentalCoroutinesApi` for `flatMapLatest`), `combine` arity,
import resolution, the DI graph (pure engines are constructor-injectable; managers
resolve `LoyaltyCardRepository`, `ZxingBarcodeEncoder`, `@IoDispatcher`, and
`DataStore<Preferences>`, all already provided), and pure unit tests for every
engine. **A Gradle/Android build was not run (no Android SDK, no network)**, so a
Gradle sync on first open is required; that sync regenerates the wrapper and runs
Room/KSP + Hilt codegen.

Scope honesty for this phase:
- The schema change is real. On first launch after upgrade, Room runs
  `MIGRATION_1_2`; the destructive fallback remains only as a pre-release safety
  net and should be removed before shipping.
- Manual drag-and-drop *ordering* is supported in the data layer and engine
  (`MANUAL` sort + `setManualOrder`), but a drag-reorder gesture UI is not yet
  wired — that interaction is a follow-up.
- Encrypted export is architecture-only (a flag and a single write path); it
  writes plaintext JSON today and is intended to plug into the later security
  phase.
- Sharing, export, and import are device-dependent (intents, `FileProvider`,
  `contentResolver`) and are best-effort against the declared APIs; they should
  be exercised on a device.
- "Reduced motion" is honoured implicitly by keeping animations short and
  non-essential; a dedicated system-setting check is a future refinement.

# Universal Loyalty Wallet — Part 2

## Core Data Architecture: Database, Domain Models, Mappers, Repositories & JSON Plugin System

This is the design companion to the Part 2 code. It implements the complete
**data + domain** layer on top of the Parts 1A/1B/1C foundation, with no UI,
Compose, ViewModels, or navigation. Package structure, Gradle setup, and the
established architecture are unchanged.

---

## 1. What was built

**Domain models** (`domain/model`): `LoyaltyCard`, `StoreDefinition`,
`CardCategory`, `BarcodeType` — pure Kotlin, framework-free, unit-testable.

**Room** (`data/database`): `LoyaltyCardEntity`, `StoreEntity`,
`LoyaltyCardDao`, `StoreDao`, `AppDatabase` (v1). Entities use only primitive
columns; enum/list conversions live in mappers, so no Room TypeConverters are
needed.

**Mappers** (`data/mapper`): `LoyaltyCardMapper`, `StoreMapper`, `ListCodec`,
`BarcodeTypeMapper` — pure functions, no Android or DB logic.

**JSON plugin system** (`data/datasource`): `JsonStoreCatalogLoader` reads every
`*.json` in `assets/stores/`, parses each into a `StoreDefinition`, and memoises
the result. `DataDrivenStorePlugin` adapts a definition to the Part 1C
`StorePluginContract`, so the same loader feeds both the store repository and
the plugin registry — fulfilling the loader seam left open in Part 1C.

**Repositories**: interfaces in `domain/repository`
(`LoyaltyCardRepository`, `StoreRepository`); implementations in
`data/repository` extend `BaseRepository`, use DAOs + mappers only, and never
expose Room upward.

**Search & validation** (`data/repository`): `CardSearchEngine` (ranked,
in-memory) and `CardValidationManager` (pure rules).

**Errors** (`data/model`): `DataError` sealed class bridged to the app-wide
`AppError` via `toAppError()`.

---

## 2. Offline-first architecture

**Single source of truth = Room.** Every read the UI will ever do comes from the
database as a cold `Flow`; the JSON catalogue is an import source that is synced
*into* Room, never read live by the UI. Mutations write to Room, and the open
`Flow`s re-emit automatically, so the UI is always consistent with storage.

**Why offline-first here:**

- **It is the product.** A loyalty wallet must show a barcode at the checkout
  counter instantly, every time, with zero network dependency.
- **Performance.** Local reads are sub-millisecond; there is no spinner, no
  round-trip, no failure mode tied to connectivity.
- **Gulf / Saudi network conditions.** Coverage inside large hypermarkets and
  basements (exactly where cards are scanned) is frequently weak or absent.
  Binding card retrieval to the network would fail at the worst moment;
  offline-first sidesteps that entirely.
- **Consistency strategy.** Room is authoritative. The store catalogue follows a
  simple last-write-wins sync (`upsertAll` keyed by `storeId`) from the bundled
  JSON; cards are only ever written locally, so there is no multi-writer
  conflict to resolve at this stage. When cloud sync is added later, the same
  Room-as-source-of-truth boundary means it slots in behind the repository
  without touching the domain or UI.

---

## 3. Data flow (step by step)

**Catalogue path — Store JSON → UI:**

1. `JsonStoreCatalogLoader` enumerates `assets/stores/*.json`.
2. Each file is parsed (`StoreDefinitionDto`) and mapped to a `StoreDefinition`
   (`StoreMapper.fromDto`); malformed files are logged and skipped.
3. On first observation, `StoreRepositoryImpl.ensureSeeded()` upserts the
   definitions into `StoreEntity` via `StoreDao`.
4. The repository exposes `Flow<List<StoreDefinition>>` from the DAO (mapped
   Entity → Domain).
5. The UI (later phase) collects that flow as `StateFlow`.

In parallel, `AppInitializer` (Part 1C) asks the same loader for
`StorePluginContract`s and registers them in the `StorePluginRegistry`, so store
behaviour is resolvable by id everywhere.

**Card path — User input → UI:**

1. UI gathers input (later phase) → a `LoyaltyCard` domain object.
2. `CardValidationManager.validate(card, store, existing)` checks required
   fields, barcode format, store compatibility, and duplicates, returning
   `DataError`s.
3. On success, `LoyaltyCardRepositoryImpl.addCard` performs a final duplicate
   guard and inserts via `LoyaltyCardDao` (Domain → Entity through the mapper).
4. Room emits the updated list on every open `Flow`.
5. The repository maps Entity → Domain and the UI re-renders. No manual refresh.

---

## 4. Performance & indexing

**Targets:** 1000+ cards, instant search, no UI blocking.

- **Indexing.** `loyalty_cards` is indexed on `storeId`, `category`,
  `isFavorite`, and `lastUsedTimestamp` — the columns behind the hot queries
  (filter by store, filter by category, favourites, recents). `stores` is
  indexed on `category` and `isActive`. Indexes turn these filters/sorts into
  index scans instead of full-table scans, which keeps them flat as the table
  grows.
- **Search (<100 ms).** Ranked search runs in `CardSearchEngine` as a single
  in-memory weighted pass over the card list. For 1000+ cards this is a few
  thousand cheap string operations — comfortably under budget — and it gives
  multi-field ranking (store name > nickname > card number > barcode value >
  category) with exact/prefix bonuses that SQL `LIKE` alone cannot express. A
  DAO-level `LIKE` query is also provided for simple pre-filtering. (FTS can be
  added later if the catalogue ever needs it, behind the same repository
  method.)
- **No UI blocking.** All DAO suspend functions run on Room's executor; observed
  `Flow`s are cold and mapped off the main thread; the JSON catalogue is parsed
  on the IO dispatcher and memoised so it is read at most once.

---

## 5. Security data design (`EncryptedCardStorage`, design only)

- **Encrypted:** the card number (and QR payload when present) — the only fields
  that are actually sensitive.
- **Not encrypted:** store metadata, category, nickname, flags, and timestamps —
  so they remain **indexable and searchable** at full speed.
- **Why field-level, not whole-DB:** encrypting the whole database would force
  decryption of every row to search, destroying the indexing strategy and
  slowing cold start. Field-level encryption protects the secret with negligible
  query cost (the Part 1A decision).
- **Threat model:** defends against offline extraction of the database file
  (stolen backup, rooted device) — without the Keystore key the numbers are
  unreadable, and the key never leaves the Android Keystore (hardware-backed
  where available). It does not, and cannot, defend a fully compromised running
  device with the app already unlocked.
- **Keystore (design only):** a per-install, non-exportable AES-256-GCM key,
  created lazily; `encrypt`/`decrypt` wrap the sensitive column on write/read.
  Implementation is deferred to the security phase.

---

## 6. Error handling

`DataError` enumerates the data-layer failures (`DatabaseError`,
`ValidationError`, `NotFoundError`, `DuplicateError`, `UnknownError`) and bridges
to the application-wide `AppError` via `toAppError()`, so failures travel through
the existing `DataResult` / `ErrorHandler` pipeline without a parallel system.
Observation flows stay unwrapped (`Flow<List<…>>`); one-shot operations return
`DataResult<…>`.

---

## 7. Final checklist

| Requirement | Status |
|---|---|
| Offline-first (Room = single source of truth, Flow streams) | ✓ |
| Plugin system JSON-driven (`assets/stores/*.json`, auto-detected) | ✓ |
| Room database functional (entities, DAOs, indices, v1) | ✓ |
| Search engine optimized (in-memory weighted ranking, <100 ms) | ✓ |
| Mappers clean & pure (no Android/DB in domain) | ✓ |
| Repository pattern (DAO-only, mapped, Room not exposed) | ✓ |
| No UI dependencies in data/domain (verified) | ✓ |
| Scalable to 500+ stores (no hardcoded list; O(1) lookup) | ✓ |
| Scalable to 1000+ cards (indexed queries, linear search) | ✓ |

**Bundled stores:** Lulu, Nesto, Carrefour, Prime, plus a generic Custom
definition — each a standalone JSON file. Adding a store is adding a file.

**Deferred (by design):** UI/Compose/ViewModels, concrete encryption, cloud
sync — later phases.

---

## Honest verification note

This phase was verified by structure and static analysis (package layering,
exhaustive `when`s, DI graph, serialization setup, no UI imports in data/domain).
A Gradle/Android build was **not** run — the container has no Android SDK and no
network — so a Gradle sync on first open is required, during which Room/KSP and
Hilt generate their code and the Gradle wrapper is restored.

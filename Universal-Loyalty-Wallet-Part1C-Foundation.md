# Universal Loyalty Wallet — Part 1C

## Final Foundation: Plugin Architecture, Security Design, Error System & Architecture Rules

This document is the design companion to the code delivered in Part 1C. It
explains the *why* behind each foundation layer. No UI, database, repository
implementation, or business logic is introduced here — only the architecture
that everything else will be built on. Package structure, build system, and
design-system strategy from Parts 1A/1B are unchanged.

---

## 1. Plugin architecture

Every retailer differs only in a handful of details: identity, branding,
accepted barcode symbologies, the card template, and the rules for validating
and formatting a card number. The plugin system captures exactly that variation
behind one stable contract.

**`StorePluginContract`** (code) exposes `getStoreId`, `getStoreName`,
`getSupportedBarcodeTypes`, `getTheme`, `getCardTemplate`, `validateCard`, and
`formatCard`. Supporting data types `StoreTheme` and `CardTemplate` are
framework-agnostic (hex colour strings, not Compose `Color`s), so the plugin
layer carries no UI dependency.

**`StorePluginRegistry`** (code) is the single resolution point.

- *Why a registry?* Feature code asks the registry for a plugin by id and
  depends only on the contract — never on a concrete store. Stores cannot
  introduce new compile-time dependencies into the rest of the app.
- *Scaling to 500+ stores.* Resolution is an O(1) `ConcurrentHashMap` lookup
  regardless of catalogue size, and plugins are registered from data (the
  bundled JSON catalogue, Part 2) rather than code — so adding stores never
  edits the registry or any feature.
- *Avoiding tight coupling.* A generic fallback (`GenericStorePlugin`) is always
  present, so `resolve()` never returns null; unknown ids degrade gracefully to
  custom-card behaviour. Duplicate registrations are refused and surfaced as a
  typed `AppError.Plugin.DuplicateRegistration` rather than silently
  overwriting.

**`StorePluginLoader`** (interface design only) is the seam for *where* plugins
come from. The shipped `BuiltInStorePluginLoader` contributes none yet (the
fallback covers every id). A JSON-catalogue-backed loader slots in here in
Part 2 without touching the registry or its callers. JSON parsing is
deliberately deferred.

---

## 2. Security foundation (design only)

No security code is implemented in this phase. The contracts are defined
(`CryptoManager`, `BiometricAuthenticator`, `SessionManager`, `PinAuthenticator`,
`SecurePreferences`, `ScreenProtector`); the blueprint below governs their
implementation.

### Security layers

1. **App access** — biometric / PIN gate plus an inactivity auto-lock before any
   card is visible.
2. **Data storage** — card numbers encrypted at rest with an Android
   Keystore-backed key; sensitive flags in EncryptedSharedPreferences.
3. **Runtime protection** — optional `FLAG_SECURE`, clipboard hygiene, and
   awareness of compromised devices.
4. **Backup** — sensitive material excluded from cloud backup / device transfer
   (already wired in the manifest's backup rules); a secure, opt-in export
   arrives later.

### Threat model

- **Unauthorized app access** → biometric + PIN gate and auto-lock.
- **Screen-capture leakage** → per-screen `FLAG_SECURE` on screens that reveal a
  number.
- **Rooted-device access** → minimise plaintext at rest; keys are non-exportable
  and hardware-backed where available.
- **Data-extraction attacks** → field-level encryption of card numbers; the DB
  file alone yields no usable numbers.
- **Clipboard leaks** → avoid auto-copying secrets; clear the clipboard after a
  short timeout when a copy is unavoidable.

### Mechanisms (to implement later)

Biometric authentication bound to a Keystore `CryptoObject` (so authentication
actually gates decryption, not just the UI); a salted-hash PIN fallback with
rate limiting; a session-timeout manager driven by activity and lifecycle; a
secure key-storage strategy via the Keystore; and encrypted preferences for
sensitive flags only.

### Android Keystore strategy

- **Key generation** — a per-install AES-256-GCM key generated in the Keystore,
  hardware-backed and non-exportable, created lazily on first need.
- **Encryption scope** — **encrypted:** card numbers and any
  personally-identifying card fields. **Not encrypted:** store metadata
  (names, brand colours, templates) and non-sensitive preferences, so that
  search stays fast on plain indexed columns (the field-level decision from
  Part 1A).

### Screenshot protection

`FLAG_SECURE` is **opt-in per screen** rather than global: usability (most
screens screenshot freely) is balanced against protecting screens that display a
live barcode/number. `ScreenProtector` exposes enable/disable so a screen
toggles it on entry and off on exit.

---

## 3. Global error handling

`AppError` (extended this phase) is the complete taxonomy: `Network`,
`Database`, `Validation`, `Security`, **`Plugin`** (new), and `Unknown`.

`ErrorMapper.toAppError()` (code) is the one place raw `Throwable`s become typed
errors; it intentionally rethrows `CancellationException` so coroutine
cancellation is never swallowed.

`ErrorHandler` (code) is the central sink: it logs each error once (type only —
never card contents), and maps every `AppError` to a user-facing `UiText`
string resource in exactly one place.

**UI error strategy (no UI yet).** Results travel as `DataResult` through
repositories and use cases; ViewModels (later phase) will expose errors as
`UiText` in their state `StateFlow`, and the UI renders them with the existing
`ErrorState` component — errors never surface as raw exceptions or stack traces.

---

## 4. Base abstractions

- **`BaseRepository`** (code) supplies `safeCall` (one-shot) and `safeFlow`
  (streaming) helpers that wrap failures into typed `DataResult`s and preserve
  cancellation. Every repository extends this so none leaks raw exceptions. It
  touches no data source itself.
- **`BaseUseCase`** / **`FlowUseCase`** (code) give each unit of business logic a
  single named entry point that runs on an injected dispatcher and returns a
  `DataResult`. Use cases improve testability (pure, no Android, no UI) and
  isolate business logic out of ViewModels and repositories.

---

## 5. Dispatcher strategy

`DispatcherProvider` (from Part 1B) abstracts IO / Default / Main / Main-immediate
behind qualifiers, and base classes accept a `CoroutineDispatcher` directly.
Injecting dispatchers (instead of referencing `Dispatchers.IO`) lets tests
substitute a single test dispatcher and removes hidden coupling to the real
schedulers — the foundation of deterministic coroutine tests.

---

## 6. Logging strategy

`AppLogger` (code) is a structured facade over Timber with a `redact()` helper.
Rules: verbose/debug output only in debug builds (release uses the redacting
`CrashReportingTree`), never log sensitive values, and keep a consistent
`tag: message` structure. Depending on the interface keeps call sites testable.

---

## 7. Application initialization blueprint

`AppInitializer` (code) is the single ordered startup entry point. Order and
rationale:

1. **Logging** — planted first by the `Application`, so every later step can log.
2. **Security layer** — prepared before any data access (no-op until the
   security phase) so encrypted reads/writes always have a key.
3. **DataStore** — created lazily on first access; not blocking cold start.
4. **Plugin registry preload** — off the main thread; the fallback is present
   from construction so features are safe even before it completes.
5. **Architecture validation** — last, debug-only, once everything is wired.

Heavy work runs on the application scope so startup returns immediately.

---

## 8. Feature module contract

Documented by the `@FeatureContract` marker (code). Each feature must: own its
ViewModel/UI; never touch the database directly; obtain data only through
repositories (via the domain layer); never depend on another feature; and
communicate cross-feature only through the domain layer or navigation. This
independence is what lets features be built, tested, and later extracted into
their own Gradle modules without ripple effects.

---

## 9. Architecture validation

`ArchitectureValidator` (code) runs at startup (debug) and checks the invariants
that can only be verified at runtime: the registry always resolves unknown ids
to the fallback, plugin ids are unique and non-blank, and every plugin declares
at least one barcode symbology. Purely structural rules (no feature→feature
dependency, no UI→DB access) are best enforced at build time with a static
architecture test (Konsist/ArchUnit) added in a later phase — noted here so the
intent is explicit rather than implied.

---

## 10. Performance guardrails

- Never block the main thread; all IO uses an IO dispatcher.
- No heavy work in UI; offload to use cases on background dispatchers.
- Flows stay cold unless intentionally cached/shared.
- Avoid recomposition-heavy patterns (enforced once UI begins): stable state,
  hoisted state, keyed lists.

---

## Final foundation summary & verification

| Requirement | Status |
|---|---|
| Plugin system scalable (O(1), data-driven, fallback) | ✓ code |
| Security model defined (layers, threats, mechanisms, Keystore, FLAG_SECURE) | ✓ design |
| Error handling centralized (`AppError` + `ErrorMapper` + `ErrorHandler`) | ✓ code |
| Architecture modular (`@FeatureContract` rules) | ✓ code + rules |
| Fully testable base layers (`BaseUseCase`, `FlowUseCase`, `BaseRepository`) | ✓ code + tests |
| Clean separation of concerns | ✓ |
| Architecture validation safeguard | ✓ code |
| App initialization design (ordered) | ✓ code |
| Logging system (structured, redacting) | ✓ code |
| Dispatcher abstraction | ✓ (1B, reused) |

**Out of scope this phase (by design):** UI/Compose, Room schema, repository
implementations, JSON plugin loading, and concrete security implementations —
all reserved for Part 2 onward.

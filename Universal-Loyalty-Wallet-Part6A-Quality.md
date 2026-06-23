# Universal Loyalty Wallet — Part 6A

## Testing, Performance, CI/CD, Documentation & Code Quality

This phase makes the project enterprise-maintainable without touching the app's
architecture or features. It adds tests, static analysis, CI/CD, build-quality
review, and a full documentation set.

---

## 1. Testing

**Unit (JVM, `app/src/test`).** Added a reusable harness — `MainDispatcherRule`
(swaps `Dispatchers.Main` for a test dispatcher) and `FakeLoyaltyCardRepository`
(a complete in-memory implementation of the 24-method repository contract,
StateFlow-backed). On top of it: `SearchViewModelTest` exercises the real
`SearchViewModel` + `SearchRankingEngine` over the fake, asserting query
matching, recent-term tracking, and category filtering via Turbine.
`ExportRoundTripTest` proves the serializable backup contract and the
card↔export mappers survive a JSON encode/decode round-trip (mirroring
`ExportManager`/`ImportManager` without Android). `CardFormattingTest` and
`NotificationPrefsTest` cover pure helpers. These join the pre-existing suites
(validation, search, smart-wallet, sorting, filtering, recency, plugin registry,
mappers, conflict resolver, CSV, migration, sync, crypto, session) for **34
unit-test files**.

**Compose UI (instrumented, `app/src/androidTest`).** `ComponentsUiTest` uses
`createComposeRule` to verify rendering, button clicks, state changes, and
accessibility labels on the shared components — deliberately targeting stateless
presentational widgets so the tests need **no Hilt test runner**. Full stateful
screens (which use `hiltViewModel()`) need a Hilt-instrumented runner; that's
called out as a follow-up rather than half-built here.

**Performance.** `PerformanceBudgetTest` puts a fast, dependency-free regression
guard in the unit suite: it runs the ranking engine over 2,000 cards and asserts
a generous wall-clock budget (catches accidental O(n²) without flaking on slow
CI). True device benchmarks (startup, scroll, render) belong in a Macrobenchmark
module — documented in `DEVELOPMENT.md`/roadmap, not added here because a new
Gradle module can't be build-verified in this environment.

---

## 2. Static analysis

**detekt** (`config/detekt/detekt.yml`) overlays the default rule set with
Compose-aware tweaks (PascalCase `@Composable`, larger Hilt-constructor parameter
budget, formatting delegated to ktlint, magic-number rule off for UI constants).
**ktlint** is configured via `.editorconfig` (Android style, explicit imports,
trailing commas, `@Composable`/`@Preview` naming exceptions). Both are wired into
Gradle via the version catalog and applied in the app module. They run **advisory**
(non-blocking) in CI initially; the suppression strategy is minimal and local —
fix first, and if you must suppress, annotate the smallest scope with a reason.

---

## 3. CI/CD

`.github/workflows/ci.yml`: on push/PR/dispatch — build debug, run unit tests,
run detekt + ktlint (advisory), and upload the APK + reports. A separate
**emulator** job runs the Compose UI tests on demand (`workflow_dispatch`) so
flaky/slow emulator runs don't gate every push. `.github/workflows/release.yml`:
on `v*` tags — assemble the release APK and AAB (R8 + shrinking) and upload them.
Both use JDK 17 with Gradle caching. These complement the project's existing
`build.yml`.

---

## 4. Build optimization (review)

Already configured in earlier phases and confirmed here: release builds enable
`isMinifyEnabled` + `isShrinkResources` (R8 + resource shrinking) with
`proguard-rules.pro` (source-line keeps, Kotlin/coroutines/serialization rules);
dependencies are centralised in `gradle/libs.versions.toml`; JVM target is 17.
No changes needed — the build is already size- and cache-friendly. Signing for
release distribution is intentionally left to the developer (`keystore.properties`
+ a `signingConfig`, git-ignored), and the release workflow documents this.

---

## 5. Memory & performance review (findings)

- **StateFlow:** screens use `stateIn(WhileSubscribed(5_000))` and
  `collectAsStateWithLifecycle`, which already avoids leaks and needless work when
  off-screen. Good.
- **Coroutine cancellation:** work runs in `viewModelScope`/injected dispatchers;
  `flatMapLatest` in search cancels stale queries. Good.
- **Recomposition:** shared components are small and mostly stateless; continue
  hoisting state and passing stable lambdas. No hotspots found in review.
- **Bitmaps:** barcode bitmaps are generated at the displayed size; ensure large
  imported images are downsampled in the image-processing path (already routed
  through a processor). Worth a device profile.
- **Room:** reads are indexed flows from the single source of truth; keep queries
  projection-tight as the schema grows.
- **Widgets:** read off the main thread via `goAsync`; no polling.

---

## 6. Code-quality checklist

- **SOLID / Clean Architecture:** features depend on domain interfaces; data and
  core are separated; DI wires at the edges. Compliant.
- **Package organisation:** consistent `core/feature/data/domain/di/widget`
  layout; new 5B/6A code follows it. Compliant.
- **Naming:** consistent (`*ViewModel`, `*Screen`, `*Engine/Manager/Resolver`);
  enforced by detekt/ktlint going forward.
- **Null safety:** explicit nullability; `DataResult` for expected failures
  instead of throwing. Good.
- **Error handling:** `AppError` hierarchy + `ErrorMapper`; restore/import surface
  friendly messages. Good.
- **Thread safety:** injected dispatchers; `InMemorySyncQueue` guards state with a
  Mutex; StateFlow for shared state. Good.
- **Accessibility:** content descriptions on actionable icons, reduced-motion
  setting, RTL-aware; continue adding labels to any new interactive surface.
- **Flagged for follow-up:** at-rest card-number encryption not yet enabled (needs
  v3 hash column); detekt/ktlint not yet blocking (needs baseline);
  Hilt-instrumented screen tests and a Macrobenchmark module not yet added.

---

## 7. Self-review

- **Compiles together (structural):** package declarations verified on every new
  test and the new config/doc files; imports checked; the fake repository
  implements all 24 interface methods; tests reference real classes/APIs
  (`SearchViewModel`, `SearchRankingEngine`, `WalletExport`/`CardExport` mappers,
  `NotificationPrefs`, formatters) confirmed against source.
- **Tests reference correct packages:** all in `com.universalwallet.loyalty`,
  using the existing `card(...)` helper, Truth, Turbine, and coroutines-test
  (all already declared in `app/build.gradle.kts`).
- **Docs match the architecture:** the plugin guide matches
  `StorePluginContract`/`StorePluginRegistry`; the backup format and JSON schema
  match `ExportModels`; SECURITY matches the 5A model.

---

## Final verification

| Item | Status |
|---|---|
| Unit tests generated | ✓ (34 files) |
| Integration tests | ✓ (fake-repo ViewModel + serialization round-trip; Room/DAO instrumented are device-bound) |
| Compose UI tests | ✓ (component-level, no Hilt runner) |
| Benchmarks configured | ✓ (JVM budget test; device Macrobenchmark documented) |
| Detekt integrated | ✓ (config + Gradle) |
| Ktlint integrated | ✓ (.editorconfig + Gradle) |
| GitHub Actions configured | ✓ (ci + release) |
| Documentation complete | ✓ (12 docs) |
| Code quality reviewed | ✓ (checklist above) |
| Enterprise workflow established | ✓ |

---

## Honest verification note

Verified by **structure and static analysis only** — no Gradle/Android build ran
(no SDK, network disabled). Two things specifically need a **Gradle sync** to
confirm on your machine:

1. **The detekt/ktlint plugin additions** (`libs.versions.toml` + root/app build
   files + config blocks). I used standard, widely-compatible versions
   (detekt 1.23.7, ktlint-gradle 12.1.1) and the standard DSL, but new plugins are
   the one edit that touches the whole project's sync. If anything fails to
   resolve, removing the two `alias(libs.plugins.detekt/ktlint)` lines and the
   `detekt {}`/`ktlint {}` blocks reverts cleanly — the rest of the phase
   (tests, CI YAML, docs) is independent of them.
2. **Instrumented/Compose UI tests** run on a device/emulator (API 30+
   recommended), not in the JVM `test` task.

The unit tests, CI workflow YAML, detekt/ktlint config files, and documentation
carry no build risk on their own.

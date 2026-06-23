# Code Style

## Language & formatting
- **Kotlin official style**, 4-space indent, 140-column soft limit, trailing
  commas on multi-line call/declaration sites, explicit imports (no wildcards).
- Formatting is enforced by **ktlint** (`.editorconfig`); run `./gradlew
  ktlintFormat` to auto-fix. `@Composable` and `@Preview` functions are allowed
  to be PascalCase.

## Static analysis
- **detekt** (`config/detekt/detekt.yml`, overlaying defaults) covers complexity,
  naming, and suspicious patterns. Formatting rules are off (ktlint owns them).
- Both run in CI as advisory checks today; they will become blocking once a clean
  baseline exists. Keep **suppressions minimal** and local: prefer fixing the
  issue; if you must suppress, annotate the smallest scope with a one-line reason
  (`@Suppress("RuleName") // why`).

## Naming
- Classes/objects: `PascalCase`. Functions/properties: `camelCase`. Constants:
  `UPPER_SNAKE_CASE`. Composables: `PascalCase`.
- ViewModels end in `ViewModel`; Compose screens end in `Screen`; pure engines end
  in `Engine`/`Manager`/`Resolver`/`Validator` per their role.

## Architecture conventions
- Features depend on **domain interfaces**, never on Room or `data/` types.
- Keep business logic in pure engines where possible (so it's JVM-testable);
  ViewModels orchestrate, they don't compute heavy logic inline.
- Return `DataResult<T>` for expected failures; don't throw for control flow.
- One public `StateFlow<UiState>` per screen; one-shot effects via a channel +
  `ObserveAsEvents`. Use `collectAsStateWithLifecycle` in Compose.

## Compose
- Hoist state; prefer stateless composables with `@Preview`s.
- Use `Spacing`/`WalletIcons`/theme tokens, not hard-coded dp/colour literals.
- Provide `contentDescription` for actionable icons; respect RTL.
- Avoid unstable lambdas/objects in hot paths; remember derived state.

## Coroutines
- Inject dispatchers (`@IoDispatcher`/`@DefaultDispatcher`); never hard-code
  `Dispatchers.IO` in testable code.
- Respect structured concurrency; let `viewModelScope` cancel work.

## Documentation
- KDoc public engines/managers and non-obvious decisions; explain *why*, not what.
- Update `ARCHITECTURE_STATE.md` when adding components.

# Contributing

Thanks for your interest in Universal Loyalty Wallet! This is primarily a solo
project, but contributions and issues are welcome.

## Before you start
- Read **[ARCHITECTURE.md](ARCHITECTURE.md)** and **[CODE_STYLE.md](CODE_STYLE.md)**.
- Check `ARCHITECTURE_STATE.md` for the current component map and known gaps.
- Open an issue to discuss substantial changes before writing code.

## Workflow
1. Fork and branch from `main` (e.g. `feature/widget-resize`,
   `fix/import-undo`).
2. Make focused commits with clear messages.
3. Run the local checks below — they mirror CI.
4. Open a pull request describing **what** and **why**, with screenshots for UI.

## Local checks

```bash
./gradlew testDebugUnitTest     # unit tests (must pass)
./gradlew ktlintFormat          # auto-format
./gradlew detekt ktlintCheck    # static analysis (advisory)
./gradlew assembleDebug         # build
# Instrumented/Compose UI tests (needs a device/emulator):
./gradlew connectedDebugAndroidTest
```

## Testing expectations
- **New pure logic** (engines, mappers, validators) → JVM unit tests.
- **New ViewModel** → test with `FakeLoyaltyCardRepository` +
  `kotlinx-coroutines-test` + Turbine (`MainDispatcherRule` swaps the Main
  dispatcher). See `SearchViewModelTest`.
- **New shared component** → a Compose UI test via `createComposeRule`
  (`ComponentsUiTest`).
- **Device-dependent code** (Keystore, BiometricPrompt, CameraX, DAOs, widgets,
  notifications) → verify on a device; add instrumented tests where practical.
- Aim to keep coverage high (target ≥ 80% on the testable, non-UI layers).

## Adding a store
See **[PLUGIN_DEVELOPMENT_GUIDE.md](PLUGIN_DEVELOPMENT_GUIDE.md)**.

## Commit / PR hygiene
- Keep PRs small and reviewable. Don't mix refactors with features.
- Update docs (`CHANGELOG.md`, `ARCHITECTURE_STATE.md`) alongside code.
- Never commit secrets, signing keys, or generated build output.

## Code of conduct
Be respectful and constructive. Assume good faith.

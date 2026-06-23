# Development Guide

Everything you need to build, run, debug, and release the app.

## 1. Prerequisites
- **JDK 17** (Temurin recommended).
- **Android Studio** (Ladybug or newer) or the Android command-line tools.
- **Android SDK**: compileSdk/targetSdk **35**, minSdk **26**. Build-tools and
  platform 35 installed.
- Internet access on first sync (downloads Gradle + dependencies).

## 2. Project setup
```bash
git clone https://github.com/acbikram/UniversalLoyaltyWallet.git
cd UniversalLoyaltyWallet
```
Open in Android Studio and let it sync (regenerates the Gradle wrapper and runs
Hilt/Room/KSP codegen), or from the CLI:
```bash
./gradlew help          # triggers wrapper + dependency resolution
```
> If the Gradle wrapper jar isn't present (fresh export), run
> `gradle wrapper --gradle-version 8.x` once with a local Gradle, then use
> `./gradlew` thereafter.

## 3. Build
```bash
./gradlew assembleDebug      # debug APK -> app/build/outputs/apk/debug/
./gradlew assembleRelease    # release APK (R8 + shrink; unsigned by default)
./gradlew bundleRelease      # release AAB for Play
```

## 4. Run / emulator requirements
- An emulator or device on **API 26+**. API 30+ recommended for Compose UI tests.
- For BiometricPrompt testing, enrol a fingerprint/PIN in the emulator.
- For widgets, add them from the launcher's widget picker after install.

## 5. Tests
```bash
./gradlew testDebugUnitTest               # JVM unit tests (fast)
./gradlew connectedDebugAndroidTest       # instrumented + Compose UI (device)
```
Reports: `app/build/reports/tests/` and `app/build/reports/androidTests/`.

## 6. Static analysis
```bash
./gradlew ktlintFormat       # auto-fix formatting
./gradlew ktlintCheck        # verify
./gradlew detekt             # complexity/naming/smells
```
Config: `.editorconfig` (ktlint), `config/detekt/detekt.yml` (detekt).

## 7. Dependency updates
- Versions live in `gradle/libs.versions.toml` (single source of truth).
- Bump a `[versions]` entry, sync, build, and run unit tests.
- Review security-relevant libraries (biometric, security-crypto, datastore) and
  Compose BOM bumps carefully; check release notes for breaking changes.

## 8. Debugging
- **Logs:** Timber (`adb logcat | grep Timber` / filter by tag). Debug builds are
  verbose; release redacts.
- **Developer Mode:** enable in Settings → Security to access the DB inspector,
  store/plugin validators, demo-card generator, sync simulator, notification
  tester, architecture validator, and the memory monitor.
- **Layout Inspector / Compose preview** for UI; **Database Inspector** for Room.

## 9. Release checklist
1. Bump `versionCode`/`versionName` in `app/build.gradle.kts`.
2. Update `CHANGELOG.md` and `ARCHITECTURE_STATE.md`.
3. `./gradlew testDebugUnitTest detekt ktlintCheck` — all green/clean.
4. `./gradlew connectedDebugAndroidTest` on at least one emulator.
5. Configure signing (`keystore.properties`, git-ignored) and a `signingConfig`.
6. `./gradlew bundleRelease`; verify the AAB installs and launches.
7. Tag `vX.Y.Z` — the **release** workflow builds artifacts.
8. Smoke-test: add/scan a card, lock/unlock, backup + restore, a widget.

## 10. Troubleshooting
- **Gradle sync fails on plugins (detekt/ktlint):** confirm the versions in
  `libs.versions.toml` resolve for your Gradle/AGP; run with `--stacktrace`.
- **KSP/Hilt errors:** do a clean build (`./gradlew clean`) and re-sync; ensure
  every `@Inject`ed type has a provider/binding.
- **Compose UI test can't find a node:** check the exact text/`contentDescription`
  and that the component is wrapped in a theme.
- **`.git` corruption on Android FUSE storage (Termux):** work from a clean clone
  folder and push with a trailing `/.` copy, per the project's established flow.
- **Biometric prompt does nothing:** the host must be a `FragmentActivity`
  (it is) and a credential must be enrolled on the device.

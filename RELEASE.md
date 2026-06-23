# Release Guide

How to cut a release of Universal Loyalty Wallet.

## Versioning strategy
- **Semantic versioning** `MAJOR.MINOR.PATCH` in `versionName` (currently
  `1.0.0`); integer `versionCode` increments by 1 every uploaded build.
- Breaking data/schema changes → MAJOR; new features → MINOR; fixes → PATCH.
- Tag releases `vX.Y.Z`; the `release.yml` workflow builds artifacts on tags.

## Release build configuration
Defined in `app/build.gradle.kts`:
- `release { isMinifyEnabled = true; isShrinkResources = true }` (R8 + resource
  shrinking) with `proguard-android-optimize.txt` + `proguard-rules.pro`.
- `debug` uses an `applicationIdSuffix = ".debug"` so debug and release can coexist.
- Signing is **file-guarded**: if `keystore.properties` exists at the project
  root, a `release` signing config is created from it; otherwise the release
  build is unsigned (still assembles). No keys live in the repo.

## R8 / ProGuard review
- Keeps: source line numbers (readable traces), Kotlin metadata, coroutines,
  and `@Serializable` classes/`serializer()` (backup format relies on them).
- After any new reflection/serialization usage, add a keep rule and **smoke-test
  the minified build** (R8 can strip reflectively-used members).

## Build & sign
```bash
# Unsigned (no keystore.properties present):
./gradlew assembleRelease          # APK
./gradlew bundleRelease            # AAB (preferred for Play)

# Signed: create keystore.properties from the template first (see DEPLOYMENT.md)
cp keystore.properties.template keystore.properties   # then edit
./gradlew bundleRelease
```
Outputs: `app/build/outputs/apk/release/` and `app/build/outputs/bundle/release/`.

## Pre-release checklist
1. Bump `versionCode` / `versionName`.
2. Update `CHANGELOG.md`, `ROADMAP.md`, `ARCHITECTURE_STATE.md`.
3. `./gradlew testDebugUnitTest detekt ktlintCheck` — green/clean.
4. `./gradlew connectedDebugAndroidTest` on an emulator (API 30+).
5. Build the **minified** release; run the smoke test in `PLAY_READINESS.md`.
6. Confirm `PRIVACY.md` is hosted and the Data Safety form matches the build.
7. Tag `vX.Y.Z` and push; attach artifacts / upload AAB to Play.

## Rollout
- Use Play **staged rollout** (e.g. 10% → 50% → 100%); watch crash/ANR metrics.
- Keep the upload key safe; rely on **Play App Signing** for the distribution key.

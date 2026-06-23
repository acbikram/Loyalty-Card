# Deployment Guide

End-to-end steps to publish Universal Loyalty Wallet to Google Play. Pairs with
`RELEASE.md` (build mechanics) and `PLAY_READINESS.md` (policy gates).

## 1. One-time setup
- Create a **Google Play Console** account and a new app entry.
- Enroll in **Play App Signing** (Google holds the distribution key; you hold the
  upload key).
- Generate an **upload keystore** (keep it backed up and secret):
  ```bash
  keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048 \
          -validity 10000 -alias upload
  ```
- Configure local signing (never committed):
  ```bash
  cp keystore.properties.template keystore.properties
  # edit storeFile / storePassword / keyAlias / keyPassword
  ```
  `.gitignore` already excludes `keystore.properties`, `*.jks`, `*.keystore`.

## 2. Build the upload artifact
```bash
./gradlew clean bundleRelease
# -> app/build/outputs/bundle/release/app-release.aab
```

## 3. Store listing
- Title, short & full descriptions (EN + AR), icon, feature graphic, phone +
  tablet screenshots. Category **Tools** (or Finance). See `PLAY_READINESS.md`.
- Add the **Privacy Policy URL** (host `PRIVACY.md`).

## 4. App content declarations
- **Data safety**: complete per `DATA_SAFETY.md` ("No data collected").
- **Content rating**: IARC questionnaire.
- **Ads**: No. **Target audience**: **[decide]**.

## 5. Release tracks
- Start on **internal testing** → closed → open/production.
- Upload the AAB, add release notes (from `CHANGELOG.md`), and submit.

## 6. Staged rollout & monitoring
- Begin at a small percentage; monitor **Android vitals** (crashes, ANRs).
- Increase rollout as metrics stay healthy; halt/rollback if regressions appear.

## 7. CI assist
- The `release.yml` workflow builds APK + AAB on `v*` tags and uploads them as
  artifacts. Signing in CI requires adding the keystore + passwords as **encrypted
  secrets** and materialising `keystore.properties` in the workflow (left out by
  default to avoid storing secrets).

## Troubleshooting
- **"App not signed"** on Play upload → ensure `keystore.properties` exists at
  build time and the `release` signing config applied.
- **Minified release crashes but debug works** → add R8 keep rules for any new
  reflective/serialized types; re-test.
- **Missing translation lint** → keep `values-ar/strings.xml` in sync with
  `values/strings.xml`.

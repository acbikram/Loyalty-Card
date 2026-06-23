# Admin / Maintainer Guide

For the developer-maintainer (and any future team) operating the project. For
contribution mechanics see `CONTRIBUTING.md`; for build/run see `DEVELOPMENT.md`.

## Responsibilities
- Triage issues, review PRs, keep dependencies current, cut releases, and keep
  privacy/Data-Safety disclosures accurate.

## Configuration surfaces
- **Versions:** `gradle/libs.versions.toml` (single source).
- **Feature flags:** `core/featureflags/FeatureCatalog` (catalogue) +
  `FeatureFlagManager` (resolution). Add a flag in the catalogue, gate code via
  `isEnabled(feature)`. Categories: FREE / PREMIUM / EXPERIMENTAL / DEVELOPER.
- **Entitlements / premium:** `core/billing`. The app ships free
  (`LocalEntitlementProvider`). To wire real billing, implement `BillingGateway` +
  an `EntitlementProvider` and rebind in `di/BillingModule`. Free-tier caps live
  in `FreeTierLimits`.
- **Localization:** add a `values-<lang>/strings.xml`, list the locale in
  `res/xml/locales_config.xml`, and add an `AppLanguage` entry.
- **Security:** see `SECURITY.md`. Keystore engine, sessions, backup crypto.

## Developer Mode
- Enable in Settings → Security. Tools: DB inspector, store/plugin validators,
  demo-card generator, **sync simulator**, **notification tester**, **architecture
  validator**, memory monitor. Intended for debugging only; ships off.

## Premium / monetization operations (when enabled)
- Premium is entitlement-driven; never hard-code "is premium" in features — always
  ask `FeatureFlagManager`. To grant premium locally for testing, use the local
  override (`LocalEntitlementProvider.setLocalPremium`) via Developer Mode.

## Release operations
- Follow `RELEASE.md` + `DEPLOYMENT.md`. Keep the **upload key** backed up and
  secret; rely on Play App Signing. Use staged rollouts and watch Android vitals.

## Data & privacy governance
- Any change that causes data to leave the device (e.g. enabling cloud sync)
  **must** update `PRIVACY.md` and the Play **Data Safety** form **before** release,
  and should be **opt-in** and encrypted.

## Monitoring & support
- No backend to operate. Support is via store reviews / GitHub issues. There is no
  server-side user data to manage or delete.

## Technical-debt register
- At-rest card-number column encryption not yet enabled (needs Room v3 + hash
  column). detekt/ktlint advisory (not blocking) pending a clean baseline. Most
  Compose UI strings are inline rather than externalized (localization framework
  is in place; full string extraction is incremental). Cloud/AI/Wear are
  architecture-only. See `ARCHITECTURE_STATE.md`.

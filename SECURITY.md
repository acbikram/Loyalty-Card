# Security

## Reporting a vulnerability

Please report security issues privately to the maintainer (GitHub: **acbikram**)
rather than opening a public issue. Include reproduction steps and affected
versions. You'll get an acknowledgement and a fix timeline; coordinated
disclosure is appreciated.

## Security model

**Threat model.** The app defends primarily against *offline* compromise — a lost
device, an extracted backup, or a pulled database file. It cannot defend a fully
compromised, running, unlocked device on which the app is already authenticated;
no client-side scheme can.

### App Lock & sessions
- Optional App Lock via **biometric**, **PIN**, or **device credential**.
- The PIN is never stored — only a per-user random salt and a PBKDF2 hash, with
  constant-time verification (`PinHasher`).
- Session locking (idle timeout, lock-on-background, short grace window) is pure
  and unit-tested (`SessionPolicy`); the host activity is a `FragmentActivity`
  required by BiometricPrompt.

### Encryption
- `KeystoreEncryptionManager` uses a hardware-backed AES-256/GCM key that never
  leaves the Android Keystore. Payloads are versioned (`v1:iv:cipher`) and GCM
  provides integrity; `rotateKey()` enables future rotation.
- **Backups** can be password-encrypted (`PasswordCrypto`: PBKDF2 → AES-GCM).
- **At-rest card-number encryption** is architected and ready but not yet enabled
  on the live columns — see the roadmap (needs a deterministic hash column so
  search/duplicate-detection keep working). Static store metadata is never
  encrypted, by design, to keep search fast.

### Other protections
- **Screenshot protection** toggles `FLAG_SECURE` (blocks screenshots, hides the
  app-switcher preview).
- **Notifications never contain card numbers.**
- **Logging:** Timber in debug; a redacting tree in release. The Developer-Mode
  `AppLog` is off by default and documented never to receive secrets.

## Handling of secrets
- No secrets are committed to the repo. Release signing keys and any future cloud
  credentials must be provided via CI secrets / local `keystore.properties`
  (git-ignored), never checked in.

## Dependencies
- Keep dependencies current (see `DEVELOPMENT.md`). Security-relevant libraries
  (biometric, security-crypto, datastore) should be reviewed on update.

# Universal Loyalty Wallet — Part 5A

## Security, Encryption, Biometrics, App Lock, Backup & Developer Mode

This is the design companion to the Part 5A code. It adds the security layer on
top of Parts 1–4B without changing the existing architecture: App Lock with
biometric / device-credential / PIN authentication, session management, Android
Keystore encryption, password-encrypted backup and a validated restore preview,
a hidden Developer Mode, and the security settings UI.

---

## 1. Where security lives

Everything new is under `core/security`, `core/backup`, `core/developer`, and
three new feature screens (`lock`, `security`, `developer`). The only change to
existing wiring is `MainActivity` becoming a `FragmentActivity` (required by
BiometricPrompt) and gaining a lock gate; the navigation graph gained two routes.

---

## 2. App Lock & authentication

`BiometricAuthenticator` wraps AndroidX `BiometricPrompt`: it reports
availability (available / none-enrolled / no-hardware / unavailable) and runs the
prompt with `BIOMETRIC_STRONG`, optionally adding `DEVICE_CREDENTIAL` (PIN /
pattern / password) as a fallback. The host activity owns the prompt because it
needs the `FragmentActivity`; the lock screen simply asks it to start.

The optional **PIN** is handled by `PinManager` + `PinHasher`: the PIN is never
stored — only a per-user random salt and a PBKDF2 hash (120k iterations), with
constant-time verification.

Users can enable/disable App Lock, choose the method (biometric / PIN / device
credential), set the auto-lock timeout, require auth on launch, and require auth
after inactivity / on backgrounding — all from Security settings.

---

## 3. Session management

`SessionManager` holds the runtime lock state (`StateFlow<Boolean>`) and the
interaction/background timestamps; the *decision* logic is the pure
`SessionPolicy`, so it is unit-tested without any lifecycle. Rules: never lock
when App Lock is off; a short grace window (2 s) avoids re-auth on quick app
switches; lock on background when configured; otherwise lock once the away/idle
time crosses the timeout. `MainActivity` feeds it `onStart`/`onStop` and
`SecurityManager.initializeOnLaunch()` sets the initial state.

---

## 4. Encryption (Android Keystore)

`KeystoreEncryptionManager` (behind the `Encryptor` interface) uses a
hardware-backed AES-256/GCM key that never leaves the Keystore. Payloads are
**versioned** (`v1:iv:cipher`) and GCM gives integrity, so tampering fails
decryption. Key rotation is designed in: `rotateKey()` mints `v2` while old `v1`
payloads stay decryptable because the version travels with the data, enabling
background re-encryption later.

**Honest scope:** encryption is **fully wired for backups** (below). Turning on
field-level encryption of *card numbers* at rest needs deterministic equality
(an HMAC hash column) so duplicate-detection and search keep working — a v3
migration deferred to the hardening pass. The engine is ready; static store
definitions are never encrypted, by design.

---

## 5. Backup & restore

`BackupManager` serializes all cards (reusing the Part 4B `ExportManager`) and,
when the user supplies a password, encrypts the JSON with `PasswordCrypto`
(PBKDF2 → AES-GCM) before writing to the chosen file; otherwise it writes plain
JSON (the user's explicit choice). `CloudBackupTarget` is an interface only — no
live cloud integration ships here.

`RestoreManager` reads the file, auto-detects encryption and decrypts (prompting
for the password), parses, runs `ImportValidator`, and builds a **RestorePreview**
(new vs conflicting cards, valid count, issues) — shown to the user *before* any
write. On confirmation it applies with a SKIP/REPLACE conflict policy.
`ImportValidator` is pure and unit-tested.

---

## 6. Security settings UI

`SecuritySettingsScreen` exposes App Lock and its sub-options, the auth method
and timeout (chips), PIN set/change/remove (dialog), screenshot protection,
clipboard protection, create/restore backup (with password + restore-preview
dialogs), and the Developer Mode toggle. Reached from Settings → "Security & App
Lock".

Screenshot protection toggles `FLAG_SECURE` on the window (blocks screenshots and
hides content in the app switcher), applied reactively in `MainActivity`.

---

## 7. Developer Mode (hidden)

Enabled only via the Security settings toggle, `DeveloperModeScreen` exposes a
database inspector (live counts), store and plugin validators, a demo-card
generator (drawn from the real catalogue), a debug-logging switch, and a runtime
performance/memory monitor. It is clearly separated from user features and
unreachable unless explicitly enabled. `AppLog` gates all logging — off by
default, and call sites never pass sensitive values.

---

## 8. Error handling & accessibility

Errors map to friendly messages via `SecurityError` (keystore unavailable,
encryption/decryption failed, corrupted/invalid backup, wrong password, auth
failed). Auth and restore failures surface as snackbars, never as crashes.
Accessibility: content descriptions on lock controls and icons, large touch
targets, clear inline error text, and standard text-field keyboard handling.

---

## 9. Tests

New pure-JVM suites: `PinHasherTest` (determinism, verify, salt uniqueness),
`PasswordCryptoTest` (round-trip, wrong-password failure, prefix detection,
no-plaintext-leak), `SessionPolicyTest` (all lock rules), and
`ImportValidatorTest` (valid / missing-field / unknown-type / newer-version /
empty). These join the Part 1–4B suites (23 test files total).

---

## 10. Self-review (per the brief)

- **Compiles**: verified structurally — package declarations on every new file,
  targeted import checks across all new managers/screens, opt-in annotations,
  DI graph closure, and signature checks against existing components.
- **Integrates cleanly**: no architecture change; only `MainActivity`'s base
  class and the nav graph/Settings entry changed. Managers reuse existing
  providers.
- **No sensitive logging**: `AppLog` is off by default and documented to never
  receive card numbers/PINs/decrypted fields; the PIN is only ever hashed.
- **Accessibility**: addressed as above.
- `ARCHITECTURE_STATE.md` updated with components, managers, settings, and
  remaining work.

---

## Final-verification checklist

| Item | Status |
|---|---|
| App Lock (enable/disable, method, timeout, on-launch, on-inactivity) | ✓ |
| Biometric authentication (+ device-credential fallback) | ✓ |
| PIN support (salted PBKDF2, never stored) | ✓ |
| Android Keystore integrated (AES-256/GCM, versioned, rotation-ready) | ✓ |
| Session management (idle/background/grace) | ✓ |
| Encrypted backup + JSON export + encrypted import | ✓ (backups) |
| Backup validation + conflict detection + restore preview | ✓ |
| Screenshot protection | ✓ |
| Developer Mode (inspector, validators, demo gen, logging, perf) | ✓ |
| Security utilities (the requested manager set) | ✓ |
| Error handling with friendly messages | ✓ |
| Tests generated | ✓ |

---

## Honest verification note

Verified by **structure and static analysis only** — a Gradle/Android build was
not run (no Android SDK, no network), so a Gradle sync is required on first open
(it regenerates the wrapper and runs Hilt/Room codegen). Device-dependent code —
Android Keystore, BiometricPrompt, `FLAG_SECURE`, the FragmentActivity lock gate,
and file I/O via `contentResolver` — is best-effort against the declared APIs and
must be exercised on a device. As noted in §4, at-rest card-number encryption is
architected and ready but not yet switched on in the live read/write path
(backups are fully encrypted); enabling it cleanly needs a small v3 migration for
a deterministic hash column, which is called out as remaining work.

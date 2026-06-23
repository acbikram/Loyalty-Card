# Privacy Policy / Privacy Notice

_Last updated: see CHANGELOG. This is a developer-prepared notice; have it
reviewed before publishing. Placeholders in **[brackets]** need a final
business/legal decision._

Universal Loyalty Wallet is built **privacy-by-design**: it keeps your loyalty
cards on your device and is designed so that, in the current build, **no personal
data leaves your device**.

## The short version
- Your cards live **only on your device**. There is no account and no sign-up.
- The app **does not request the INTERNET permission**, so the current build
  cannot transmit your data anywhere.
- No advertising, no analytics SDKs, no third-party trackers.
- You can export, back up, and delete everything yourself, at any time.

## What data is stored locally
- **Card data** you add: store, card/membership number, barcode value/type,
  optional nickname, notes, category, favourite/pin/archive flags, usage counts,
  and an optional card image.
- **Preferences**: theme, language, notification toggles, feature flags, and
  security settings (e.g. lock method).
- **Security material**: if you set a PIN, only a salted PBKDF2 **hash** is stored
  — never the PIN itself.

All of the above is stored in the app's private storage (Room database +
DataStore), accessible only to the app.

## What is encrypted
- An Android **Keystore**-backed AES-256/GCM key is available and used for
  sensitive payloads; the key never leaves the secure hardware.
- **Backups** you create can be **password-encrypted** (PBKDF2 → AES-GCM).
- **[Planned]** At-rest encryption of the card-number column is architected and
  will be enabled in a future update; until then card numbers are stored in the
  app's private database but not separately encrypted at the column level.
- Static store metadata (names, categories) is intentionally not encrypted so
  search stays fast; it is not sensitive personal data.

## What is optional
- **Camera** — only used when you scan a card. The camera feature is not required
  to use the app (you can type cards in manually).
- **Notifications** — reminders are off unless you enable them (Android 13+ asks
  for permission).
- **Biometric / App Lock** — entirely optional.
- **Card images** — only stored if you add them.

## What leaves the device
- **Nothing, in the current build.** There is no networking permission and no
  analytics.
- **Backups/exports** leave the device **only when you explicitly create one** and
  choose where to save it (your file, your cloud drive of choice via the system
  file picker). The app does not upload anything itself.
- **[Future] Cloud sync** is architecture-only today. If a future version adds it,
  it will be **opt-in**, end-to-end encrypted via the existing crypto, and this
  notice plus the Data Safety form will be updated **before** release.

## User controls
- Add, edit, archive, hide, and **delete** any card; deletions are immediate and
  permanent in local storage.
- Export your wallet (JSON/CSV) and create encrypted backups.
- Choose your language, lock method, screenshot protection, and notification
  preferences.
- Uninstalling the app removes all app data from the device.

## Backup behaviour
- Backups are created **on demand** by you and written to a location **you**
  choose. They can be password-encrypted. The app performs no automatic cloud
  backup. (Android's system auto-backup rules are configured conservatively; see
  `data_extraction_rules`/`backup_rules`.)

## Import/export behaviour
- Import reads a file **you** select (JSON/CSV/image/encrypted backup), previews
  it, and lets you resolve duplicates before anything is written. An **undo**
  removes exactly what was just imported.

## Children
- The app is a general-utility tool and is **[not directed to children]**; adjust
  per your store listing and applicable law.

## Contact
- Privacy questions / data requests: **[contact email]** (GitHub: acbikram).

## Changes
- Material changes will be reflected here and, where relevant, in the Play Data
  Safety form before they take effect.

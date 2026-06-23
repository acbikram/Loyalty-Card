# Google Play Data Safety — Preparation

This document maps the app's behaviour to the **Play Console → App content → Data
safety** form. Confirm each answer against the shipping build before submitting;
**[brackets]** mark business/legal decisions.

## Summary answers

| Form question | Answer | Notes |
|---|---|---|
| Does your app collect or share any of the required user data types? | **No** (current build) | No account, no analytics, no networking permission. All data stays on-device. |
| Is all user data encrypted in transit? | **Not applicable** | The app makes no network transmissions (no INTERNET permission). |
| Do you provide a way for users to request data deletion? | **Yes** | In-app delete per card; uninstall removes all data. No server data exists. |

> Because the current build neither collects (sends off device) nor shares data,
> most of the Data Safety form is **"No data collected."** The detail below
> documents on-device processing for transparency and to pre-fill the form if a
> future networked feature changes the answers.

## Data types present on-device (not "collected" per Play definition)
Play defines "collected" as transmitted off the device. The following are stored
**locally only**, so they are **not** "collected":

| Data | Stored locally | Sent off device | Purpose |
|---|---|---|---|
| Loyalty card numbers / barcodes | Yes | No | App functionality |
| Optional nickname / notes | Yes | No | App functionality |
| Optional card images | Yes | No | App functionality |
| App preferences / settings | Yes | No | App functionality |
| PIN (as salted hash only) | Yes (hash) | No | Security |

## Data shared
- **None.** No third parties, no SDKs that transmit data, no advertising.

## Data stored
- On-device in the app's private Room database and DataStore. See `PRIVACY.md`.

## Encryption
- Android Keystore (AES-256/GCM) available for sensitive payloads; optional
  **password-encrypted backups**. **[Planned]** column-level card-number
  encryption. No data in transit (nothing is transmitted).

## User deletion
- Per-card deletion in-app (immediate). Full deletion by uninstall. There is no
  account or server, so there is no remote data to delete.

## Backup
- User-initiated exports/backups only, to a user-chosen location, optionally
  encrypted. Android system auto-backup is configured via
  `data_extraction_rules` / `backup_rules`; **[confirm whether to allow
  cloud auto-backup of the app's data or disable it for sensitive fields]**.

## If/when cloud sync ships (future)
Update the form to reflect: data types transmitted (card data), purpose
(app functionality / sync), encryption in transit (TLS) and at rest (E2E via app
crypto), and the opt-in nature. Re-review **before** that release.

## Permissions justification (for review notes)
- `CAMERA` — scanning card barcodes (optional; feature not required).
- `USE_BIOMETRIC` — optional App Lock.
- `POST_NOTIFICATIONS` — optional reminders (Android 13+).
- No `INTERNET` permission is requested.

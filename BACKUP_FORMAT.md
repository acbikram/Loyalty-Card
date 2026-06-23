# Backup Format

The app exports a wallet as a single JSON document. Backups can be **plaintext**
or **password-encrypted**; both restore through the same validated pipeline with
a preview and conflict handling.

## Container

| Field | Type | Notes |
|---|---|---|
| `version` | int | Backup schema version. Current: **1** (`WalletExport.CURRENT_VERSION`). A newer version than the app supports is rejected. |
| `exportedAt` | long | Unix epoch millis when exported. |
| `cards` | array | The exported cards (see below). |

## Card entry (`CardExport`)

| Field | Type | Default | Notes |
|---|---|---|---|
| `storeId` | string | — | Stable store id (plugin key). |
| `storeName` | string | — | Display name. |
| `cardNumber` | string | — | The card/membership number. |
| `barcodeValue` | string | — | Encoded barcode value (often == number). |
| `barcodeType` | string | — | `BarcodeType` name, e.g. `EAN13`, `QR`. |
| `qrCodeValue` | string? | null | Optional separate QR payload. |
| `customerName` | string? | null | Optional. |
| `nickname` | string | "" | User label. |
| `notes` | string | "" | Free text. |
| `category` | string | `GENERAL` | `CardCategory` name. |
| `isFavorite` | bool | false | |
| `colorThemeId` | string | "default" | Card colour style id. |

## Encryption
- A plaintext backup is the JSON above.
- An **encrypted** backup wraps that JSON with `PasswordCrypto` (PBKDF2 →
  AES-GCM). The restore path auto-detects encryption and prompts for the
  password. Encryption is detected by the payload prefix, not the file extension.

## Restore semantics
- The backup is parsed and validated (`ImportValidator`): unknown barcode types,
  missing required fields, and unsupported versions are reported.
- A **preview** shows new vs. conflicting cards and the valid count before any
  write.
- Conflicts (same logical card already present) are resolved by policy: **SKIP**
  or **REPLACE**.
- The **import wizard** additionally supports CSV and single-image imports and an
  **undo** that removes exactly the cards just added.

## Example
See `JSON_SCHEMA.md` for a formal schema and a complete example document.

## Compatibility
- Forward: a backup from an older `version` is accepted and upgraded as needed.
- Backward: a backup from a newer `version` than the installed app is rejected
  with a clear message (don't silently drop unknown fields that matter).

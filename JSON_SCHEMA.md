# Backup JSON Schema

A formal description of the wallet backup document (see `BACKUP_FORMAT.md` for
prose). The schema below is JSON Schema (draft 2020-12) and matches
`WalletExport` / `CardExport` (`core/export/ExportModels.kt`). Optional fields use
their documented defaults when absent (the parser is lenient and ignores unknown
keys).

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "WalletExport",
  "type": "object",
  "required": ["exportedAt", "cards"],
  "properties": {
    "version": { "type": "integer", "default": 1, "minimum": 1 },
    "exportedAt": { "type": "integer", "description": "Unix epoch millis" },
    "cards": {
      "type": "array",
      "items": { "$ref": "#/$defs/CardExport" }
    }
  },
  "$defs": {
    "CardExport": {
      "type": "object",
      "required": ["storeId", "storeName", "cardNumber", "barcodeValue", "barcodeType"],
      "properties": {
        "storeId": { "type": "string" },
        "storeName": { "type": "string" },
        "cardNumber": { "type": "string" },
        "barcodeValue": { "type": "string" },
        "barcodeType": {
          "type": "string",
          "enum": ["EAN13","EAN8","CODE128","CODE39","CODE93","UPC","ITF","PDF417","QR","AZTEC"]
        },
        "qrCodeValue": { "type": ["string", "null"], "default": null },
        "customerName": { "type": ["string", "null"], "default": null },
        "nickname": { "type": "string", "default": "" },
        "notes": { "type": "string", "default": "" },
        "category": {
          "type": "string",
          "default": "GENERAL",
          "enum": ["SUPERMARKET","PHARMACY","FUEL","RESTAURANT","COFFEE","ELECTRONICS","FASHION","GENERAL"]
        },
        "isFavorite": { "type": "boolean", "default": false },
        "colorThemeId": { "type": "string", "default": "default" }
      }
    }
  }
}
```

## Example document

```json
{
  "version": 1,
  "exportedAt": 1717000000000,
  "cards": [
    {
      "storeId": "lulu",
      "storeName": "Lulu Hypermarket",
      "cardNumber": "6291041500213",
      "barcodeValue": "6291041500213",
      "barcodeType": "EAN13",
      "nickname": "Main",
      "category": "SUPERMARKET",
      "isFavorite": true,
      "colorThemeId": "default"
    },
    {
      "storeId": "boots",
      "storeName": "Boots",
      "cardNumber": "1234567890123",
      "barcodeValue": "1234567890123",
      "barcodeType": "QR",
      "qrCodeValue": "https://boots.example/loyalty/1234567890123",
      "category": "PHARMACY"
    }
  ]
}
```

## CSV import (wizard)

The import wizard also accepts CSV with an optional header and columns:

```
storeName,cardNumber,barcodeType,nickname,category
Lulu,6291041500213,EAN13,Main,SUPERMARKET
Boots,1234567890123,QR,,PHARMACY
```

Only `storeName` and `cardNumber` are required; `barcodeType` defaults to
`CODE128` and `category` to `GENERAL` when omitted or unrecognised. Quoted fields
(e.g. `"Lulu, Mall"`) are supported.

# Universal Loyalty Wallet — Part 4A

## Barcode Engine, Scanner, Generator, Image Import & Card-Creation Flow

This is the design companion to the Part 4A code. It adds the complete
card-creation engine on top of Parts 1–3 without changing any earlier
architecture: barcode scanning (ML Kit + CameraX), barcode generation (ZXing),
image import with auto-detection, a validation/checksum engine, duplicate
detection with a replace flow, a live card-creation preview, and the navigation
that ties it together. The dependencies and manifest were already provisioned in
Part 1 (CameraX, ML Kit, ZXing, exifinterface, the `CAMERA` permission, and a
`FileProvider`), so this phase only adds code.

---

## 1. What was built

**Barcode engine (`core/barcode`)**
- `BarcodeValidator` — pure, framework-free validation: character set, length,
  and mod-10 check digits for EAN-13 / EAN-8 / UPC-A, even-length for ITF, and
  any non-empty payload for the 2D symbologies. Also `detectType()` for
  auto-detecting a symbology from a manually typed number.
- `BarcodeFormats` — bridges `BarcodeSymbology` to ZXing `BarcodeFormat` (for
  generation) and ML Kit format constants (for scanning), keeping both
  third-party enums out of feature code.
- `ZxingBarcodeEncoder` — implements the Part 1 `BarcodeEncoder` contract,
  rendering any symbology to a crisp `Bitmap` at print quality, with
  configurable colours and a non-throwing `encodeOrNull` for UI.
- `BarcodeAnalyzer` — a CameraX `ImageAnalysis.Analyzer` running ML Kit on each
  frame, reporting the first recognised code and always closing the frame.
- `BarcodeImageDecoder` — detects a barcode inside a still image (screenshot /
  photo) for the import flow.

**Image pipeline (`core/image`)**
- `CardImageStore` — saves card images to app-private `files/card_images`
  (sandboxed, not world-readable) and returns absolute paths.
- `ImageProcessor` — decode → EXIF-orient → downscale → JPEG-compress, plus
  `rotate` and `cropToRect` helpers; all off the main thread.

**Permissions (`core/permission`)**
- `rememberCameraPermissionState()` — Activity-Result-based camera permission
  state (no third-party permission library).

**Generation UI (`core/cards`)**
- `BarcodeImage` — a self-sizing composable that encodes a real, scannable
  barcode off the main thread (`produceState`), always black-on-white for
  reliability, falling back to the deterministic placeholder until ready. Now
  used on Card Details (barcode + QR) and the Add-card live preview.

**Scanner (`feature/scanner`)**
- `ScannerViewModel` — torch / zoom / mode (single vs continuous) / pause state
  and duplicate-scan suppression; builds the analyzer bound to its handler.
- `ScannerScreen` — CameraX `PreviewView` via `AndroidView`, framed overlay with
  an animated scan line, torch, zoom slider, mode toggle, permission rationale,
  and "scan again". Orientation-agnostic.

**Card creation (`feature/addcard`)**
- `AddCardViewModel` / `AddCardScreen` extended into the full flow: choose store
  → scan **or** manual entry **or** image import → auto-detected type → live
  premium preview with a generated barcode → duplicate check → save → success
  animation.

**DI** — `BarcodeModule` provides the ML Kit `BarcodeScanner` (restricted to the
supported formats) and binds `BarcodeEncoder` to the ZXing implementation.

**Navigation** — the `Scanner` route is wired; Add-card launches it and receives
the result back through the nav back stack's `SavedStateHandle`.

---

## 2. Card-creation flow

```
Add Card → choose store → (Scan ⟶ ScannerScreen ⟶ result)
                         (Manual entry, type auto-detected)
                         (Import image ⟶ auto-detect barcode)
        → live preview (card face + generated barcode, updates as you type)
        → Save → duplicate? → ask to replace ─┐
                         │ no                  │ replace
                         ▼                     ▼
                      add card            update existing
                         └────────► success animation ► back
```

The scanner returns its result by writing the raw value and symbology to the
previous back-stack entry's `SavedStateHandle`; `AddCardViewModel` observes those
keys and applies them (number + type) to the form. No global state, no singletons
holding scan results.

---

## 3. Validation & duplicate detection

Validation is layered. `BarcodeValidator` provides checksum/format checks and
type detection at the UI/engine level; the existing `CardValidationManager`
remains the authoritative gate inside the repository on save (required fields,
format, store compatibility, duplicates). Whitespace is trimmed before storage.

Duplicate detection runs live in the ViewModel: any existing card with the same
store **and** the same number or barcode value marks the form as a duplicate
(banner shown). On save, the user is asked whether to **replace** the existing
card (overwrites its data) or cancel — never a silent overwrite, never a silent
reject.

---

## 4. Plugin integration

The flow resolves the store's plugin from `StorePluginRegistry` to supply
store-specific behaviour — the default nickname falls back to the plugin's store
name, and a store's declared barcode types seed the form's default. Because
everything goes through the `StorePluginContract`, adding a retailer never
touches the create flow.

---

## 5. Performance & accessibility

- Barcode **encoding** and **image processing** run off the main thread
  (`Dispatchers.Default` / the IO dispatcher); the UI never blocks.
- The scanner uses `STRATEGY_KEEP_ONLY_LATEST` so frames never queue, and each
  `ImageProxy` is closed immediately — the basis for sub-300 ms responsiveness on
  device.
- Content descriptions on every control and the generated barcode; large touch
  targets; a manual-entry path is always offered when the camera is unavailable
  or permission is denied.

---

## 6. Final checklist

| Requirement | Status |
|---|---|
| Barcode scanning (ML Kit + CameraX) | ✓ |
| Barcode generation (ZXing, all formats, print quality) | ✓ |
| Auto barcode-type detection | ✓ |
| Validation incl. checksums | ✓ |
| Duplicate detection + replace prompt | ✓ |
| Image import (gallery) + auto-detect in image | ✓ |
| Image processing (orient, downscale, compress, crop/rotate helpers) | ✓ |
| Card-creation flow with live preview + success animation | ✓ |
| Plugin integration | ✓ |
| Permissions handling | ✓ |
| Navigation updates (scanner route + result return) | ✓ |
| Material 3 compliant | ✓ |

---

## Honest verification note

Verified by structure and static analysis only — package layering, experimental
opt-ins (`ExperimentalGetImage` on the analyzer, `ExperimentalMaterial3Api` for
segmented buttons), import resolution, DI graph, exhaustive `when`s, and pure
unit tests for the checksum/detection logic and symbology mapping. A
Gradle/Android build was **not** run (no Android SDK, no network), so a Gradle
sync on first open is required.

Two caveats specific to this phase: (1) the camera/ML Kit code is hardware- and
SDK-dependent and is best-effort against the declared library versions — it
cannot be exercised in this environment and should be validated on a device; and
(2) ML Kit's barcode model may download on first use, so the very first scan can
be slower than the steady-state target. Interactive cropping and OCR are scaffolded
(processor + decoder building blocks) but the interactive crop editor is left as a
follow-up; screenshot import currently auto-detects the code rather than offering
manual crop.

# Universal Loyalty Wallet — Part 3

## Complete Presentation Layer: Screens, ViewModels, Navigation, Components, Card Design System, Animations & Responsive UI

This is the design companion to the Part 3 code. It builds the **entire Compose
presentation layer** on top of Parts 1A/1B/1C/2 — every screen, ViewModel,
reusable component, the card design system, navigation, theming integration,
animations, responsive layouts, and accessibility. No architectural decision
from earlier phases was changed; the package structure, Gradle setup, theme
engine (1B), and repositories (2) are reused as-is.

Out of scope by instruction and therefore not implemented: barcode scanning,
backup, cloud sync, and widgets. Where the UI references these (e.g. the Add-card
"Scan" button), they appear as clearly disabled, labelled-as-coming controls.

---

## 1. Screen inventory

Eleven screens, each as a stateful wrapper (`XScreen`, collects state + wires
navigation) delegating to a previewable, stateless `XContent(state, callbacks)`:

| Screen | Data source | Highlights |
|---|---|---|
| Splash | onboarding flag (DataStore) | animated logo, routes to Home or Onboarding |
| Onboarding | DataStore (writes flag) | 5-page `HorizontalPager`, Skip / Next / Get-started, dots |
| Home | favourites + recent + all + catalogue | greeting, search entry, category chips, sections, grid/list, **pull-to-refresh**, FAB |
| Search | `searchCards()` | instant search, category filter, sort, recent searches, no-results |
| Wallet | `observeCards()` | full collection, category filter, grid/list, FAB |
| Card Details | `getCard()` + favourite/delete/markUsed | large card, barcode + QR placeholders, copy, share (placeholder), fullscreen preview |
| Edit Card | `getCard()` + `updateCard()`/`deleteCard()` | editable form, validation, delete confirmation |
| Add Card | `observeActiveStores()` + `addCard()` | store picker, category, **10-style** picker, disabled scan/import |
| Store Browser | `observeStores()` | search, category + country filter, alphabetical, brand avatars |
| Statistics | `observeCards()` | totals, favourites, animated per-category bars, recent activity |
| Settings | `ThemeStateHolder` | theme picker, dynamic colour, navigation to About / Stores, disabled future toggles |
| About | static | app identity, version, developer, open-source libraries |

---

## 2. Navigation graph

A single `WalletNavHost` registers every destination from `WalletDestination`,
hosted inside `WalletApp` — a `Scaffold` whose bottom `NavigationBar` shows the
five top-level destinations (**Home, Search, Wallet, Statistics, Settings**) and
hides automatically on full-screen flows (splash, onboarding, add/edit, details,
browser, about).

- **Start destination:** Splash → decides Home vs Onboarding from the persisted
  flag, popping itself off the back stack.
- **Arguments:** `card_details/{cardId}` and `edit_card/{cardId}` use
  `NavType.StringType` arguments, read in the ViewModel via `SavedStateHandle`.
- **Deep links:** `universalwallet://card/{cardId}` opens Card Details directly.
- **Back-stack restoration & state preservation:** tab switches use
  `popUpTo(startDestination){ saveState = true }` + `launchSingleTop` +
  `restoreState`, so each tab keeps its own stack and scroll position.
- **Transitions:** the host defines shared fade + horizontal-slide
  enter/exit/pop transitions for a consistent, 60-fps-friendly feel.

---

## 3. Card design system (10 styles)

The premium card face is driven by data, not hard-coded per screen:

- **`CardStyle`** — the ten required styles: Classic, Glass, Minimal, Gradient,
  Modern, Business, Dark Premium, Neon, Soft, Luxury. A card persists its choice
  in `LoyaltyCard.colorThemeId`; `CardStyle.fromId` resolves it (default Classic).
- **`cardVisual(style, seed?)`** — maps a style to a `CardVisual` (gradient
  stops, content colour, muted colour, light/dark flag). It can optionally tint
  from a store's brand `seed` colour, so future retailer templates drop in
  without new components.
- **`LargeLoyaltyCard`** — full-width premium face: linear-gradient background,
  rounded corners, soft shadow, monospaced number, favourite star, category
  label; one consolidated accessibility description.
- **`CardTile`** — the same language at grid/list scale.
- **`BarcodePlaceholder` / `QrPlaceholder`** — deterministic Canvas-drawn
  *visual* placeholders (intentionally not real, scannable codes — generation is
  out of scope this phase), seeded from the card value so a card always looks the
  same.

Light, dark, and Material You dynamic colour all flow from the 1B theme engine;
the card gradients are independent of the M3 scheme by design, so a card looks
identical regardless of app theme — matching how physical cards behave.

---

## 4. State-management pattern

Every screen follows one shape:

- **Immutable `XUiState`** data class with derived helpers (`isEmpty`,
  `canSave`, …), exposed as `StateFlow` from the ViewModel.
- **Repository streams → state** via `combine` / `flatMapLatest`, collected with
  `stateIn(WhileSubscribed(5s))` so work stops when the UI is away.
- **One-time effects** (navigation-after-save, snackbars) via a `Channel`
  exposed as a `Flow`, consumed by the lifecycle-aware `ObserveAsEvents` helper
  so they fire exactly once and never while stopped.
- **No business logic in the UI** — composables receive state and emit
  callbacks only; ViewModels orchestrate repositories; validation lives in the
  data layer (`CardValidationManager`, invoked by `addCard`/`updateCard`).
- **Recomposition discipline** — stable immutable state, `remember` for local
  UI state, `derivedStateOf` for derived values (e.g. onboarding "is last page"),
  keyed `LazyColumn`/`LazyRow` items.

---

## 5. Responsive strategy

Rather than depend on an Activity-scoped window-size class, responsiveness is
local and composable-driven: `BoxWithConstraints` + `rememberAdaptiveColumns`
classify the available width into COMPACT / MEDIUM / EXPANDED and pick **1–4 grid
columns** accordingly. This covers phones (portrait/landscape), foldables (folded
and unfolded), tablets, and split-screen with a single mechanism, and it adapts
mid-session when a foldable opens or the window is resized. Card collections use
this on Home and Wallet; horizontal sections use width-bounded tiles.

---

## 6. Accessibility

- Every actionable icon carries a `contentDescription`; decorative icons are
  explicitly `null`.
- Cards expose one consolidated spoken description (store, nickname, category,
  favourite) via `clearAndSetSemantics`, instead of leaking raw child text.
- Touch targets meet the 48 dp minimum (`Dimensions.minTouchTarget`, enforced in
  settings rows and buttons).
- Colour is never the only signal (favourite uses a star icon; selection uses
  chips and outlines).
- Text uses the M3 type scale, so system font-scaling (large-font
  accessibility) flows through automatically.

---

## 7. Animations

Fade + slide screen transitions (nav host); springy logo reveal (Splash); paged
onboarding with animated dots; animated statistics bars (`animateFloatAsState`);
the spring-scaling `AnimatedBadge`; ripple feedback on all clickable surfaces.
Durations and easing come from the 1B `MotionDurations` / `MotionEasing` tokens
so motion is one consistent language and stays within a 60-fps budget.

---

## 8. Components added this phase

Reusing the nine 1B components (button, card, top bar, search bar, FAB, dialog,
bottom sheet, snackbar, stateful empty/error/loading), Part 3 adds:
`CategoryChip` + `CategoryChipRow`, `StoreRow`, `SectionHeader`, `SettingsItem`,
`SwitchItem`, `ConfirmationDialog`, `AnimatedBadge`, plus the card-system
composables (`LargeLoyaltyCard`, `CardTile`, `BarcodePlaceholder`,
`QrPlaceholder`). Every public composable has KDoc and at least one `@Preview`
rendered inside `AppTheme` with fabricated data.

---

## 9. Final checklist

| Requirement | Status |
|---|---|
| All 11 screens implemented (real, not placeholders) | ✓ |
| ViewModel per screen, StateFlow + immutable state + one-time events | ✓ |
| Navigation: bottom nav, nested full-screen flows, args, deep links, state restoration | ✓ |
| Reusable component library (1B reused + new) | ✓ |
| Card design system with all 10 styles | ✓ |
| Animations (fade/slide/scale/spring/bars/ripple) | ✓ |
| Responsive (phone/tablet/foldable/landscape/split) | ✓ |
| Accessibility (descriptions, 48dp targets, non-colour signals, font scaling) | ✓ |
| Material 3 + dark/light + dynamic colour (1B engine) | ✓ |
| No business logic in UI; clean layering (feature → domain + core only) | ✓ |
| Every composable has Preview + KDoc | ✓ |
| Excluded by spec: scanning, backup, cloud sync, widgets | ✓ (shown disabled) |

---

## Honest verification note

Part 3 was verified by structure and static analysis only — package layering,
exhaustive `when`s, experimental-API opt-ins (`ExperimentalMaterial3Api`,
`ExperimentalCoroutinesApi`), import resolution, icon/destination existence,
ViewModel injection annotations, and that previews instantiate with fabricated
data inside `AppTheme`. A Gradle/Android build was **not** run — the container
has no Android SDK and no network — so a Gradle sync on first open is required,
during which Compose, Room/KSP, and Hilt generate their code. Given the size of
the Compose surface, treat the first sync as the real compile checkpoint;
anything it flags will be a localised import or signature fix, not a structural
one.

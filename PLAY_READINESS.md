# Google Play Release Readiness Checklist

A pre-submission review against common Play policies and technical gates. Items
marked **[decide]** need a product/business choice.

## Listing & policy
- **Recommended category:** Tools (alternative: Finance). **Tags:** loyalty,
  wallet, cards, barcode. **[decide final category]**
- **Content rating:** complete the IARC questionnaire; expected **Everyone**.
- **Privacy Policy URL:** host `PRIVACY.md` and link it in the listing. **Required.**
- **Data safety form:** complete per `DATA_SAFETY.md` ("No data collected" in the
  current build).
- **Ads:** declare **No ads**.
- **Trademarks:** store names/logos are used only to label the user's own cards;
  **[confirm no infringing brand assets are bundled]** (use generic icons/colours).

## Technical gates
- **Target SDK:** `targetSdk = 35` (meets current Play requirement). compileSdk 35,
  minSdk 26.
- **App Bundle:** ship an **AAB** (`bundleRelease`), not an APK.
- **64-bit:** pure Kotlin/JVM + standard AndroidX; no native ABIs to worry about.
- **Signing:** enroll in **Play App Signing**; upload key via the file-guarded
  `signingConfig` (see `DEPLOYMENT.md`). No keys in the repo.
- **Versioning:** bump `versionCode`/`versionName` each release (currently 1 /
  1.0.0).
- **R8 + resource shrinking:** enabled on release; verify the app runs after
  minification (smoke test below).

## Permissions & background work
- Permissions: `CAMERA`, `USE_BIOMETRIC`, `POST_NOTIFICATIONS` — each maps to a
  user-facing, optional feature; justify in review notes.
- No `INTERNET`, no location, no contacts, no foreground services.
- Background work: none today. **[If WorkManager reminders are added]** they are
  deferrable and battery-friendly; document them.

## Privacy-sensitive features
- Camera used only during scanning; request at point of use; degrade to manual
  entry if denied.
- Notifications gated behind the runtime permission (Android 13+).
- Biometric optional; host is a `FragmentActivity`.

## Accessibility (see Part 6B audit)
- Content descriptions on actionable icons, RTL support, reduced-motion setting,
  large-touch-target components. Run TalkBack and large-font smoke tests.

## Large-screen / foldable
- Adaptive layout helpers (width-class padding/max-width) + responsive columns.
  Verify on a tablet/foldable emulator; ensure no fixed-width clipping.

## Pre-launch smoke test (on a release build)
1. Install the **minified release** AAB/APK on a device.
2. Add a card (scan + manual), view its barcode, copy/share.
3. Enable App Lock; background and resume → lock prompt appears.
4. Create an encrypted backup; restore it on a clean install.
5. Add a home-screen widget.
6. Switch system language to Arabic → UI mirrors (RTL) and translated strings show.
7. Toggle a notification reminder (Android 13+ permission prompt).

## Store assets **[prepare]**
- Icon (adaptive), feature graphic, phone + tablet screenshots (EN + AR), short
  + full descriptions, optional promo video.

## Final gate
- All unit tests green; detekt/ktlint reviewed; release smoke test passed;
  privacy policy live; data safety submitted.

# FAQ

**Do I need an account?**
No. The app is offline-first; there's no sign-up and no account.

**Does my data go to the cloud?**
No. The current build doesn't even request internet access, so it can't transmit
your data. Backups leave your device only when *you* create one and choose where
to save it.

**Is my card data encrypted?**
Backups can be password-encrypted, and an Android Keystore key protects sensitive
payloads. Column-level encryption of card numbers is planned for a future update.
See `PRIVACY.md`/`SECURITY.md`.

**How do I move to a new phone?**
Create an (encrypted) backup, install the app on the new phone, and use the import
wizard to restore.

**Why does it want the camera / notifications?**
Camera is only for scanning cards (you can type them instead). Notifications power
optional reminders and are off until you enable them.

**Does it support Arabic / RTL?**
Yes — English and Arabic, with full right-to-left layout. On Android 13+ you can
set the app's language in system Settings.

**Is there a free version? What's premium?**
The app is free. A premium tier is architected (unlimited cards/widgets, advanced
themes, and future cloud sync / family sharing) but billing isn't enabled yet.

**What happens if scanning fails?**
Use manual entry or import a photo of the card. Some older/odd symbologies may
need manual entry.

**How many cards can I store?**
The free tier targets a generous limit; premium removes caps. Limits are
configurable by the maintainer.

**Can I export to a spreadsheet?**
Yes — CSV export, and CSV import via the wizard. See `BACKUP_FORMAT.md`.

**Is it open source / who makes it?**
Maintained by acbikram. See the repository and `CONTRIBUTING.md`.

**How do I delete my data?**
Delete cards individually in-app, or uninstall to remove everything. There's no
server copy.

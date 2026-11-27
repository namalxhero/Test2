# Changelog

## Next
- **Root screen comes first**: the app opens on a full-screen "Checking root…" page. If root is granted it fades away and the main screen is there; if not, it stays on "Root is needed" with Try again (no Skip).
- **Size** (Edit card): make the whole video bigger or smaller (30-200%) and move it left/right/up/down. At 100% the whole video fits the screen; the empty space is filled with a soft glow of the video's own edges. Shows live in the preview and goes into the final .qmg.
- **Faster preview start**: a freshly picked video starts loading at once (no wait) and the first round of frames is lighter, so it starts playing sooner.
- **One fit, nothing cut off**: Fill, Stretch, Fit-with-bars and Crop are gone. Every video is fitted to the screen as a whole, and the sides are filled with a soft blurred copy (no bars, no crop).
- **Explore** replaces the Gallery tab (shown as templates). The "Share my animation" button is gone; when you save the module .zip the app asks if you also want to share it on Explore.
- After root is granted the app goes back to the main Create screen.
- **Customize** (Tools tab): 5 Paper looks (Paper, Paper glass, Paper dark, Paper ink glass, Paper sakura glass), each with its own colours and its own pair of fonts. The old Ocean look is gone. Choose how the Edit card is laid out: all in one list, or tabs (Trim / Fit / Crop / Look). Your choice is remembered.
- **Real glass** in the three glass looks: cards and the nav bar show a frosted (blurred) slice of the background, with a bright rim, an inner glow and solid shapes behind that turn soft under the glass. Blur needs Android 12+; older phones get the same look without the blur.
- Glass looks now use organic liquid blobs and water-drop highlights behind and on the glass (own design, no switch).
- **Jelly nav bar**: the glass bar has a bubble that stretches and snaps to the chosen tab; the flat bar of Paper / Paper dark has a sliding bar with a small bounce. Cards no longer run a wave animation (lighter on weak phones).
- **Line icons** instead of emoji: a drawn icon set for the nav bar, buttons, chips, hints and toasts.
- **New app icon**: a "Q" with a boot-ring arc on a teal-to-navy gradient (also a themed/monochrome icon for Android 13+).
- Fonts bundled (SIL Open Font License, see `FONTS.md`): Young Serif, Instrument Sans, Bricolage Grotesque, Plus Jakarta Sans, Gloock, Figtree, Unbounded, Outfit, Playfair Display, Sora.
- **Gallery tab**: browse animations shared by other users (stored in a public GitHub repo, no server). Tap Install and it goes straight into your phone with root. "Share my animation" saves your zip + a small picture and opens GitHub's upload page; the owner approves it by pull request. Items made for your own screen size are marked ✓. Set the repo in `Gallery.kt` (`REPO`); a ready-made starter is in `gallery-template/`.
- **Root is required**: without root the app shows a full-screen "Root is needed" screen with a "Try again" button (no Skip).
- **Silent diagnostics**: runs in the background (on app start, after every reboot and every 12 hours). It checks your root manager, the metamodule, and whether the module's files are really mounted over the stock animation (md5 comparison), and shows a notification when the module is installed but not active. No extra card; the Safety card has a "Copy diagnostic report" button for support. It never asks for root by itself.
- **Metamodule check is mandatory**: as soon as root is granted (about a second) the app checks whether KernelSU has a metamodule (e.g. meta-overlayfs). If not, a full-screen warning appears with "Check again" and a "Skip" button at the bottom. The install dialog warns again and a notification is shown. Without root this check cannot be done.
- **Background encoding**: a foreground service with a progress notification keeps the encode running while you use other apps, and an "Animation ready" notification appears if the app is not on screen.
- **Edit card** (Create tab): brightness, contrast and a text overlay (top / middle / bottom, 3 sizes). Everything shows in the live preview and goes into the final .qmg.
- **One Edit card**: Trim, Look (fit / fade) and the editing tools (crop, brightness, contrast, text) are now together in a single Edit card.
- **Whole-clip fast preview**: the live preview now covers the WHOLE chosen clip (up to 60 evenly spaced frames, plays at its real length), loaded coarse-to-fine with parallel workers so it starts almost at once and gets smoother. The phone preview always stays inside the screen with the phone's shape.
- **Free crop** (Edit card): crop box of any size and shape (width and height set separately, or one tap for Phone shape / Square / 16:9 / 9:16 / 4:3, or type any shape such as 400 : 100), move it anywhere in the video, then Fill / Fit / Stretch / Glow fits it to the phone's screen. Shows in the live preview and in the final file.
- New permissions: FOREGROUND_SERVICE, FOREGROUND_SERVICE_DATA_SYNC, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED.
- The in-app updater now checks the SHA-256 checksum that GitHub publishes for the release APK and rejects a download that does not match.
- The build can sign with a private key stored in GitHub Secrets (see `SECURITY.md`). Without the secrets it still uses the key in the repo, so updates keep working.
- Module .zip now saves instantly into /storage/emulated/0/QmgStudio (same folder as the backup: direct write, then root, Download only as a last resort). STORED zip entries, no re-compression.
- Every card now has a flowing wave animation. Tapping the "Your animations" card saves the module .zip at once.
- In-app update: every time the app opens it checks the latest GitHub Release. If a newer version exists, an "Update available" dialog shows with release notes, a download progress bar and an "Update now" button that opens the Android installer.
- About card: new "Check for updates" button.
- New permissions: INTERNET (check/download) and REQUEST_INSTALL_PACKAGES (install the update).

## 1.0 (version numbers restart here)
- Version numbers restart at 1.0 and now grow automatically: 1.0 → 1.1 … 1.9 → 2.0 (see `.github/workflows/build.yml`).
- The one-tap "Install now (root)" button is removed. The tab is now "Save".
- Save buttons write the file at once into Download/QmgStudio (module .zip, bootsamsung.qmg, shutdown.qmg).
- Every build is checked with apksigner before it is published; all releases use the same signing key.


## Earlier builds (old numbering)

## 1.7
- New home: https://github.com/xBooM-SL/Qmg-studio
- About card shows the real installed version and has buttons for GitHub, Releases and Issues.

## 1.6
- The app asks for root when it opens (KernelSU / Magisk pop-up) and shows a **Root ✓ / ✗** chip; tap it to ask again.
- Header chips are read from the phone the app runs on: model, Android version, animation size, decoder, root.
- Without root: clear message, one-tap install/uninstall/reboot are disabled, saving the module (.zip) and the .qmg files still works.
- A notice appears on non-Samsung phones (QMG animations only run on Samsung).
- With root, the stock `bootsamsung.qmg` is read on first launch to learn the exact animation size of the phone.

## 1.5
- Fix: "Preview error (-1)" after "Verify with system decoder" or GIF export. The preview player, GIF export,
  single-frame preview and the verifier now use independent decoder instances.
- Faster live player (no bitmap allocation per frame).
- Faster encoder: O(1) best-delta lookup + early abort when planning blocks (output stays bit-identical to the
  slower search for the same settings, verified against Samsung's decoder).
- Release workflow: builds a signed APK on every push, and publishes a GitHub Release when you push a tag like `v1.5`.

## 1.4
- Fixed signing key and auto-incrementing versionCode, so a new build installs as an *update* over the old one.
- Parallel frame extraction, encoder cache, "Glow" fit mode, live clip preview.

## 1.2 / 1.3
- Live preview with the real encoder, boot + shutdown at once, any screen size (multiple of 4), GIF in/out.

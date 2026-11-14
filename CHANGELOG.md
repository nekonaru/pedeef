# Changelog

All notable changes to Pedeef are recorded here. The format follows [Keep a Changelog](https://keepachangelog.com).

## v1.3.0

Big update: audit fixes, viewer thumbnail strip, reusable signatures and better export controls.

### New features

- **Page thumbnails in the viewer.** Tap the grid icon in the viewer to jump to any page at a glance; the current page is highlighted.

- **Reusable signature.** Draw once in Fill & Sign; the signature is saved and offered on every page you sign.

- **Export quality.** PDF to Images now lets you pick image size (1600 or 2400 px) and JPEG quality (75, 90 or 100).

### Improvements

- **Audit fixes.** File listing caps search results; sign-image storage moved from cache to internal files so Fill & Sign projects survive cleanup; viewer page tracking uses a snapshot-derived state so it no longer recomposes on every scroll frame.
- **Version bump for tests.** Unit tests now cover the update checker and file-name sanitizer.

### Bug fixes

- **Fill & Sign images could break.** Signature and picture references lived in the volatile cache; they now live in internal app files.

## v1.2.0

A full visual refresh: consistent design tokens, tidier layouts and smooth, cheap animations.

### Improvements

- **Visual refresh.** One spacing scale, unified radii and elevation across every screen: Home, Files, Settings, tool screens, dialogs and onboarding.
- **Cleaner Home.** App icon next to the wordmark, scanner hero with a subtle gradient and chevron, pinned tools with animated pin state, consistent tool cards.
- **Cleaner Settings.** Header card with the app icon, grouped sections, full-width segmented controls. Fixes the broken "Mode" and "Bahasa" label wrapping.
- **Cleaner Files.** Consistent list rows with tinted circular icons, centered empty state, better storage permission gate.
- **Smooth tab transitions.** Crossfade between Home, Files and Settings, fast and lightweight.

### Bug fixes

- **Settings label overflow.** Mode and Language labels no longer wrap vertically in cramped rows.
- **Scanner hero contrast.** Text on the scanner card keeps full contrast in every theme.

## v1.1.0

Files now live in the public Download/Pedeef folder, plus update checks and a settings redesign.

### New features

- **Public storage.** Everything Pedeef saves (scans, tool outputs) goes to `Download/Pedeef`, visible in any file manager. Grant "All files access" or keep using in-app storage.
- **Check for updates.** Tap the version row in Settings to check GitHub for a newer release.
- **Sponsor.** Support the developer via GitHub Sponsors, Saweria, SociaBuzz or BagiBagi.
- **GitHub profile.** The Settings About section links to the developer's GitHub.

### Improvements

- **Files in Download/Pedeef.** New permission gate on the Files tab and one-time migration of files saved by earlier versions.
- **Cleaner Settings.** Redesigned layout: header card, grouped sections, consistent spacing.
- **Tab bar hidden on detail screens.** The bottom bar no longer covers the viewer, tool or scanner screens.
- **PDF to Images saves to your phone.** Exported JPEG pages land in Download/Pedeef like everything else.
- **Adaptive icon.** Transparent foreground inside the safe zone; monochrome variant kept for themed launchers.
- **Lint gate.** The build now runs Android lint with zero errors, locally and in CI.

### Bug fixes

- **Portrait lock removed.** Pedeef no longer forces portrait orientation.

## v1.0.0

The first public release. Pedeef rebuilds the PDF toolkit idea as a fully offline, bilingual Android app.

### New features

- **Offline PDF toolkit.** 26 tools: merge, alternate and mix, split (every page, ranges, text, bookmarks, half, size), organize, rotate, flip, multiple pages per sheet, crop and resize, watermark, page numbers, redact, fill and sign, metadata editor, compress, OCR, set and remove password, PDF to ZIP, workflow builder, and batch mode.
- **New tools.** Images to PDF, PDF to Images, Extract Text.
- **Camera scanner.** Edge detection and deskew on-device (ML Kit Document Scanner), gallery import fallback, optional OCR text layer.
- **Built-in viewer.** Scrolling, zoom, search with match navigation, copy page text. Works from Android 7.0 up.
- **Files tree.** Save, folders, search, rename, move, share, delete, export copies. Everything stays in the app's private storage.
- **Bilingual UI.** English and Bahasa Indonesia, switchable in Settings.
- **Three themes.** Ember, Black Cherry, Arctic Blue, plus dynamic color on Android 12+.
- **Privacy by design.** No network permission, no broad storage permission, allowBackup off.

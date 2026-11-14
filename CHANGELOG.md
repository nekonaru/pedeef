# Changelog

All notable changes to Pedeef are recorded here. The format follows [Keep a Changelog](https://keepachangelog.com).

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

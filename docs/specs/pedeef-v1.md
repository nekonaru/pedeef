# Pedeef v1.0.0 Spec

## Goal

Rebuild the MIT-licensed PDF Tools Android app (source study only, in
`pdf-tools-master/`, never committed) as an original project named Pedeef,
with an enhanced feature set, a bilingual UI, a new logo and a privacy-first
permission model. Android 7.0 (API 24) and newer.

## Done criteria

1. `./gradlew :app:assembleDebug` succeeds on a machine with the Android SDK.
2. `./gradlew :app:testDebugUnitTest` passes.
3. CI (GitHub Actions) runs build and unit tests on push and on pull requests.
4. Every tool from the original catalogue exists, plus Images to PDF, PDF to
   Images and Extract Text.
5. UI ships in English and Bahasa Indonesia, switchable in Settings.
6. No `MANAGE_EXTERNAL_STORAGE`, no `INTERNET` permission. All PDF work runs
   on the device.
7. README.md and README.id.md are in sync. CHANGELOG.md and CHANGELOG.id.md
   record v1.0.0.

## Enhancements over the original

- In-app viewer built on Android `PdfRenderer` and Compose. Works on every
  supported Android version, not only Android 12+.
- Scanner uses the ML Kit Document Scanner (auto edge detection, ML-based
  cleanup) with a gallery-import fallback. No bundled OpenCV or ONNX model.
- No broad storage permission. Files live in app storage; imports and exports
  go through the system file picker (Storage Access Framework).
- Bilingual UI (English, Bahasa Indonesia).
- Dynamic color on Android 12+, plus three brand schemes.
- Three new tools: Images to PDF, PDF to Images, Extract Text.
- Batch mode: single-file tools can run on a selection of PDFs at once.
- No Google Drive sync and no network permission at all. Rationale: keep the
  offline promise absolute and keep the app small.
- Tagline promises change from "no sign-up" to "no account, no network".

## Feature inventory (v1.0.0)

Capture: Scan (ML Kit scanner or gallery import, filters, rotate, save as PDF,
optional OCR text layer via ML Kit Text Recognition).

Combine and split: Merge, Alternate and Mix, Split (every page or custom
ranges), Split by Text, Split by Bookmarks, Split in Half, Split by Size,
PDF to ZIP.

Arrange pages: Organize (reorder, rotate, delete), Rotate, Flip, Multiple
Pages per Sheet, Crop and Resize.

Fill and mark up: Fill and Sign (text and saved signature), Watermark, Page
Numbers, Redact, Edit PDF Details.

Protect and shrink: Compress (by level or to a target size), OCR
(searchable), Set Password, Remove Password.

Automate: Workflow Builder (chain tools, run on one or many PDFs).

New: Images to PDF, PDF to Images, Extract Text.

## Architecture

- Kotlin, Jetpack Compose, Material 3, Navigation Compose.
- `engine/`: pure PDF operations on Apache PDFBox (pdfbox-android). No Android
  UI dependencies. Unit-testable.
- `ui/`: screens (home, tool, files, viewer, settings, onboarding).
- `data/`: file storage, drafts, pinned tools, settings (JSON on disk).
- `ui/theme/`: three brand schemes plus dynamic color.
- `i18n/`: EN and ID string tables, `LocalPedeefStrings`.

## Out of scope for v1.0.0

- Google Drive sync (removed, documented as an intentional simplification).
- Play Store release process (APKs are attached to GitHub Releases instead).
- Connected (on-device) instrumented tests; CI runs JVM unit tests only.

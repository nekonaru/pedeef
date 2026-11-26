<div align="center">
  <img src="assets/pedeef-icon-512.png" alt="Pedeef logo" width="120" />

  <h1>Pedeef: Offline PDF Toolkit for Android</h1>

  <p><b>All your PDF tools in one free app.<br/>Everything runs on your phone, your files stay in Download/Pedeef.</b></p>

  <a href="https://github.com/nekonaru/pedeef/releases/latest">
    <img alt="Release" src="https://img.shields.io/github/v/release/nekonaru/pedeef?style=flat-square&color=C8501E" />
  </a>
  <a href="https://github.com/nekonaru/pedeef/actions/workflows/ci.yml">
    <img alt="CI" src="https://img.shields.io/github/actions/workflow/status/nekonaru/pedeef/ci.yml?branch=main&style=flat-square" />
  </a>
  <img alt="Platform: Android" src="https://img.shields.io/badge/platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img alt="Min SDK: Android 7.0" src="https://img.shields.io/badge/minSdk-24-C8501E?style=flat-square" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-C8501E?style=flat-square" />
</div>

<br />

**English** | [Bahasa Indonesia](README.id.md)

---

## Table of Contents

- [About](#about)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Continuous Integration](#continuous-integration)
- [FAQ](#faq)
- [License](#license)

---

## About

Pedeef is a free PDF toolkit for Android. It merges, splits, compresses, scans, OCRs, redacts, signs and protects PDF files, and it does all of that on the device, with no account and no network access.

It is an original reimplementation of the open-source PDF Tools app (MIT, (c) 2026 SammyZi), with a rebuilt architecture, a bilingual UI and new tools of its own.

> _"Your documents never leave your phone unless you send them yourself."_

---

## Features

### Scan

| Feature | Description |
|---------|-------------|
| **Camera scan** | On-device document scanning with automatic edge detection, corner cleanup and deskew. |
| **Gallery import** | Images from your gallery become the same scans, one page per image. |
| **Searchable scans** | Optional OCR layer (invisible text) baked into the saved PDF, done on-device. |

### Combine and split

| Feature | Description |
|---------|-------------|
| **Merge PDFs** | Combine many PDFs into one; reorder files before merging. |
| **Alternate & Mix** | Interleave pages from two or more PDFs, one page from each in turn. |
| **Split** | Every page as its own file, or extract custom ranges like `1-3, 7, 9-`. |
| **Split by Text** | Start a new file at every page containing a phrase you choose. |
| **Split by Bookmarks** | Turn each bookmarked section into its own PDF. |
| **Split in Half** | Two files down the middle, or slice every page vertically or horizontally for book scans. |
| **Split by Size** | Break a large PDF into parts that each stay under a size limit. |
| **PDF to ZIP** | Bundle PDFs into a single ZIP archive. |

### Arrange pages

| Feature | Description |
|---------|-------------|
| **Organize Pages** | Reorder, rotate and delete pages, with page thumbnails. |
| **Rotate** | 90, 180 or 270 degrees, all pages or a selection. |
| **Flip** | Mirror pages horizontally, vertically or both. |
| **Multiple Pages per Sheet** | 2, 4, 6, 9 or 16 pages per sheet when printing. |
| **Crop & Resize** | Drag a crop frame per page, or fit every page to A4, A5, Letter or Legal. |

### Fill and mark up

| Feature | Description |
|---------|-------------|
| **Fill & Sign** | Type onto forms, place a drawn signature or a picture anywhere on any page. |
| **Watermark** | Diagonal CONFIDENTIAL-style text across every page, adjustable size and opacity. |
| **Page Numbers** | Bottom left, center or right; `1, 2, 3` or `Page 1 of N`. |
| **Redact** | Black out areas so their content is actually removed, not just covered. |
| **Edit PDF Details** | Title, author, subject and keywords. |

### Protect and shrink

| Feature | Description |
|---------|-------------|
| **Compress** | Recompress embedded photos to a level, or squeeze until the file fits a size target. |
| **OCR** | Add an invisible text layer to scans so find, select and copy work. |
| **Set Password** | AES-256 encryption; the PDF asks for a password before it opens. |
| **Remove Password** | Save a copy that opens without a password, restrictions removed too. |
| **Remove Metadata** | Strip title, author, subject, keywords, creator, producer and XMP from a PDF. |

### Automate

| Feature | Description |
|---------|-------------|
| **Workflow Builder** | Chain any tools in order and run them over one or many PDFs. |
| **Batch mode** | Single-file tools run on every selected PDF at once. |
| **Pins** | Pin the tools you use most to the top of Home. |

### New in Pedeef

| Feature | Description |
|---------|-------------|
| **Images to PDF** | Turn JPG and PNG images into one PDF, one page per image. |
| **PDF to Images** | Export pages as JPEG (a ZIP for multi-page documents). |
| **Extract Text** | Copy all text from a PDF into a plain `.txt` file. |
| **Extract Images** | Save every embedded image in a PDF as a PNG, zipped when there are several. |
| **Bilingual UI** | English and Bahasa Indonesia, switchable in Settings. |
| **Download/Pedeef** | Every file you save lands in the public Download/Pedeef folder, visible in any file manager. The only network call is the optional update check. |
| **Built-in viewer** | Smooth scrolling, zoom presets (100/125/150%), bookmark navigation, in-page search highlights, page thumbnails, copy page text, on every supported Android version. |
| **Check for updates** | On-demand update check against GitHub releases, in Settings. |
| **Sponsor** | Support the developer via GitHub Sponsors, Saweria, SociaBuzz or BagiBagi. |
| **PDF handler** | Open PDFs sent from other apps, view them in Pedeef and optionally save them to Download/Pedeef. |
| **Print to printer** | Print any PDF straight from the system print dialog. |
| **Sort files** | Sort your files by name, date, size or extension in the Files tab. |
| **Rotate all** | Rotate every page in Organize Pages with one tap. |
| **Night mode** | Invert page colors in the viewer for comfortable reading in the dark. Toggle in the top bar or in Settings. |
| **Resume reading** | Reopen a document exactly where you left off; the viewer remembers the last page per file. |
| **Launcher shortcuts** | Long-press the app icon for Scan, Merge PDFs, Fill & sign and Organize pages. |
| **Reopen recent PDFs** | The launcher shortcut also lists your most recently modified PDFs (up to 4); one tap reopens one. |
| **New app icon** | A fresh terracotta mark with an adaptive icon and a themed (monochrome) icon for Android 13+. |
| **Blank PDF** | Create an empty PDF at A4, A5, Letter or Legal, up to 200 pages, no source file required. |
| **Compare PDFs** | Compare two PDFs page by page and get a plain-text report: identical pages, changed pages and the exact lines that differ. |
| **Document report** | A plain-text report of a PDF's pages, metadata, embedded images and form fields. |
| **PDF Lint** | Lint a PDF for internal issues: fonts that are not embedded, odd versions, encryption, producer. |
| **Find Duplicates** | Report visually identical pages in a PDF (e.g. duplicate scans) as a plain-text file. |
| **Remove Blank Pages** | Delete blank pages from a scan and get a text report of the removed page numbers. |
| **Reverse Pages** | Reverse the page order of a PDF, e.g. after scanning a document back to front. |
| **Build Bookmarks** | Add bookmarks to a PDF from lines like "1=Introduction", then use bookmark navigation in the viewer. |
| **Owner password** | Optional separate owner password when locking a PDF (defaults to the open password). |
| **Text to PDF** | Turn typed or pasted text into a PDF with automatic wrapping and page breaks. |
| **Resize Pages** | Fit every page onto A4, A5, Letter or Legal while keeping aspect and orientation. |
| **Grayscale PDF** | Render pages in grayscale for e-ink or print; optional standard/high resolution. |
| **Invert Pages** | Invert page colors for dark-background reading or negative printing. |
| **Remove Annotations** | Strip comments, highlights, links and form fields before sharing a PDF. |
| **Extract Attachments** | Save files embedded inside a PDF to the app's file tree. |
| **Flatten Pages** | Freeze every page's appearance into an image: no editable text, links, forms or annotations. |
| **Read Aloud** | Listen to the current page from the viewer using on-device text-to-speech. |
| **Search in PDFs** | Search one or more PDFs for a phrase; get a text report of matching page numbers. |
| **Header & Footer** | Stamp a short header and/or footer on every page of a PDF. |
| **Split Every N Pages** | Split a PDF into consecutive parts with a fixed number of pages each. |
| **Trim Margins** | Auto-crop uniform white margins around scanned page content. |
| **Archive Copy** | Write a PDF/A-1b identification marker and set version 1.7 (not full conformance validation). |
| **Page Size Report** | List distinct page sizes in points, millimetres and inches. |
| **Split at Blank Pages** | Split a scan into parts at blank separator pages and drop them. |
| **Keep Odd/Even Pages** | Keep only odd- or even-numbered pages from duplex scans. |

---

## Requirements

| Dependency | Minimum Version | Notes |
|------------|-----------------|-------|
| **Android** | **7.0 (API 24)** | Built-in viewer and all tools work from Android 7 up. |
| **Google Play services** | recent | Only for the camera scanner. Every other feature works without it; the gallery import works everywhere. |
| **JDK** | **17** | To build from source. |
| **Android SDK** | compileSdk **36** | To build from source. |

> [!TIP]
> You do not need to build anything: grab the APK from the latest release instead.

### Never used a terminal? Start here

<details>
<summary><b>How to run the build commands on Windows, macOS and Linux</b></summary>

1. **Windows**: press `Win + R`, type `powershell`, press Enter.
2. **macOS**: open `Applications > Utilities > Terminal`.
3. **Linux**: open your terminal app (often `Ctrl + Alt + T`).
4. Move into the project folder with `cd` and run the commands from this README as written. On Windows, use `.\gradlew` instead of `./gradlew`.

</details>

---

## Installation

### Option A: Download the APK (easiest)

1. Open the [latest release](https://github.com/nekonaru/pedeef/releases/latest).
2. Download `pedeef-1.23.0.apk` (signed with the app's built-in debug key).
3. Tap the file to install. If Android warns about unknown sources, allow installing from your browser or file manager.
4. Open Pedeef. On first use of the Files tab, allow the file access it asks for so your files land in Download/Pedeef.

> [!IMPORTANT]
> These APKs are signed with the app's built-in debug key, not a store upload key yet. If you use Google Play later, you may need to uninstall this build first.

### Option B: Build from source (for development)

#### 1. Get the code

<details>
<summary><b>I have Git installed</b></summary>

```bash
git clone https://github.com/nekonaru/pedeef.git
cd pedeef
```

</details>

<details>
<summary><b>I don't have Git</b></summary>

1. Open the [repository](https://github.com/nekonaru/pedeef) on GitHub.
2. Click **Code**, then **Download ZIP**.
3. Unzip it and open a terminal inside the `pedeef` folder.

</details>

#### 2. Install dependencies and verify

You need JDK 17 and the Android SDK (platform 36). Android Studio bundles both.

```bash
./gradlew :app:testDebugUnitTest
```

> [!IMPORTANT]
> The analyzer and all unit tests must pass. Do not continue with the next step until they are green.

#### 3. Build

```bash
./gradlew :app:assembleDebug   # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug    # install on a connected phone
```

---

## Usage

### First launch

1. Pedeef opens with a welcome screen. Pick **English** or **Bahasa Indonesia**.
2. Tap **Get started**.
3. Pin the tools you use most: tap the pin icon on any tool card.

### Everyday use

- **Run a tool**: tap it, pick your PDFs, set the options, tap the action button. The result can be previewed, shared, opened in the built-in viewer, or saved to Download/Pedeef on your phone.
- **Scan a document**: tap the scan card on Home. Pages are detected and cleaned automatically; scans are searchable if you leave OCR on.
- **Find your files**: the Files tab lists everything in Download/Pedeef, with folders, search, rename, move, share and delete. Allow file access when asked, everything works through it.
- **Locked PDF?**: password-protected files ask for their password when you pick them; the password is never stored.

---

## Configuration

### Display Language

1. Open **Settings**.
2. Under **Language**, choose **English**, **Bahasa Indonesia** or **System**.

### Appearance

1. Open **Settings**.
2. Choose **System**, **Light** or **Dark**, then one of the three color schemes: **Ember**, **Black Cherry** or **Arctic Blue**.
3. On Android 12+, turn on **Dynamic color** to follow your wallpaper.

---

## Project Structure

```
pedeef/
  app/                      Android app module
    src/main/java/com/nekonaru/pedeef/
      MainActivity.kt       entry point
      engine/               PDF engine (PdfBox), pure ops + android ops
      tools/                tool catalog (51 tools, EN + ID)
      ui/                   screens: home, tool, files, viewer, settings, scan
      ui/theme/             themes (Ember, Cherry, Arctic) + typography
      i18n/                 English and Bahasa Indonesia strings
      data/                 files store, settings, pinned tools
    src/test/               JVM unit tests
  assets/                   logo and icon artwork
  docs/specs/               specs
  .github/workflows/        CI (build, tests, secret scan)
  README.md                 English documentation (source of truth)
  README.id.md              Bahasa Indonesia documentation
  CHANGELOG.md              release history (English)
  CHANGELOG.id.md           release history (Bahasa Indonesia)
```

---

## Development

### Adding a tool

1. Implement the operation in `engine/PdfEngine.kt` (bytes in, bytes out, no Android UI).
2. Add a bilingual entry in `tools/Tools.kt` (`TOOLS` list) with EN and ID title, description and CTA.
3. If it needs a custom editor, add one under `ui/tool/` and dispatch it from `ToolScreen.kt`.
4. Add a unit test in `app/src/test/` (pure logic) or note why it is device-only.
5. Add the tool to the Home catalog icons (`ui/home/HomeScreen.kt`).
6. Update `README.md` and `README.id.md` together.

> [!WARNING]
> Do not edit the generated Gradle wrapper JAR. Update it with the Gradle wrapper task instead.

### Adding a user-facing string

1. Add it to `Strings.kt` in both `En` and `Id`.
2. Use `LocalPedeefStrings.current` in the composable. No hard-coded UI strings.

---

## Testing

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

| Check | Requirement |
|-------|-------------|
| Kotlin compile | Zero errors |
| Unit tests | All pass (48 tests) |
| Debug APK | Builds successfully |
| Lint | Zero errors |
| Secret scan | gitleaks reports zero findings |

---

## Continuous Integration

| Workflow | Purpose |
|----------|---------|
| `ci.yml` | Unit tests, lint, debug APK build, gitleaks secret scan on every push, PR and tag `v*` |

---

## FAQ

<details>
<summary><b>Does Pedeef work without internet?</b></summary>

Yes. All tools, the scanner and OCR run on your phone. The app declares the INTERNET permission only for the optional update check, which runs when you tap it; the check sends one HTTPS GET to GitHub and nothing else.
</details>

<details>
<summary><b>Where are my files?</b></summary>

In Download/Pedeef, visible to any file manager. If you deny storage access, they stay inside the app and the Files tab tells you how to grant it.
</details>

<details>
<summary><b>Why is there no Google Drive sync?</b></summary>

The app is deliberately network-free. Back up by exporting copies wherever you back up everything else.
</details>

<details>
<summary><b>A tool says my PDF is password-protected. What now?</b></summary>

Pick it again and type the password in the prompt. The password is used once and never saved.
</details>

<details>
<summary><b>The camera scanner does nothing on my phone.</b></summary>

The scanner needs Google Play services. On phones without it, use "Import from gallery" on the scan screen instead.
</details>

<details>
<summary><b>How is Pedeef licensed?</b></summary>

MIT. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
</details>

---

## Author

<div align="center">

| [![Nicolas Dwi Dharma](https://github.com/nekonaru.png?size=100)](https://github.com/nekonaru) |
|:---:|
| **Nicolas Dwi Dharma** |
| [github.com/nekonaru](https://github.com/nekonaru) |

</div>

---

## License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.

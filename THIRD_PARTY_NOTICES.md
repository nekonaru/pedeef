# Third-Party Notices

Pedeef is MIT licensed. It stands on these open source projects, libraries and assets.

## Libraries

| Project | Licence | Used for |
|---------|---------|----------|
| [Apache PDFBox](https://github.com/apache/pdfbox) (via [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)) | Apache 2.0 | PDF engine: structure, text, images, encryption |
| [Jetpack Compose](https://developer.android.com/jetpack/compose), [Compose Material 3](https://m3.material.io) | Apache 2.0 | UI toolkit and components |
| [androidx Activity](https://developer.android.com/jetpack/androidx/releases/activity), [Navigation](https://developer.android.com/jetpack/androidx/releases/navigation), [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | Apache 2.0 | App components and navigation |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Apache 2.0 | Asynchronous work |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | Apache 2.0 | JSON (settings, tool options, drafts) |
| [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition) (on-device, Latin) | Google ML Kit terms | OCR: searchable scans |
| [ML Kit Document Scanner](https://developers.google.com/ml-kit/vision/doc-scanner) (Google Play services) | Google Play services terms | Camera scanning with edge detection |

## Fonts and icons

| Asset | Licence | Notes |
|-------|---------|-------|
| [Poppins](https://github.com/google/fonts/tree/main/ofl/poppins) (Indian Type Foundry) | SIL Open Font License 1.1 | UI typeface; regular, medium, semibold |
| [Caveat](https://github.com/googlefonts/caveat) (Impallari Type) | SIL Open Font License 1.1 | Display face on the welcome screen |
| App icon (Pedeef mark) | Same as the app (MIT) | Original artwork, see `assets/` |

Full text of the SIL Open Font License 1.1: https://openfontlicense.org

Full text of the Apache License 2.0: https://www.apache.org/licenses/LICENSE-2.0

## Original work Pedeef learned from

Pedeef was designed after studying the MIT licensed **PDF Tools** Android app by SammyZi
(https://github.com/sammyZi/pdf-tools, Copyright (c) 2026 SammyZi). No source code was copied. The engine
and UI were written from scratch in a new package, with a different architecture (ML Kit Document Scanner instead
of CameraX/OpenCV, custom PdfRenderer viewer instead of Jetpack PDF, no Drive sync, bilingual UI).

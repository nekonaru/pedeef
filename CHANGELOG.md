# Changelog

All notable changes to Pedeef are recorded here. The format follows [Keep a Changelog](https://keepachangelog.com).

## v2.0.0

Major release: 24 new PDF tools, presets and batch processing, a richer workspace, and a two-page viewer.

### New features

- **24 new tools.** Image stamp, highlight phrase, replace pages, overlay PDFs, booklet, poster tiles, insert blank pages, duplicate pages, page border, Bates numbering, text stamp, black and white, blank page report, extract comments, remove attachments, remove JavaScript, detailed protection, owner password unlock, PDF to long image, images to long image, text with page markers, checksum manifest, integrity check and sanitize everything.
- **Presets.** Save a tool configuration with a name and load it later from the same tool screen.
- **Batch runner.** Choose a preset and run it on up to 100 PDFs in the file tree. Results go to a timestamped folder with a text report.
- **Files workspace.** Multi-select files for bulk delete, star important files, and choose all.
- **Search history.** Recent phrases appear as chips on Home and can be cleared.
- **Quick actions.** Batch runner and the four most used pinned tools appear above the tool grid.
- **Viewer Pro basics.** Two-page book mode and a full thumbnail grid, alongside the existing strip and strip zoom.
- **Input.Any.** Tools can now accept mixed PDF and image files in one picker.

### Improvements

- **Tool catalog:** 75 tools, catalog test and README EN/ID updated; unit suite is now 54 tests.
- **Engine reuse:** page placement, form embedding and cleanup operations share the same internal helpers as the older tools.
- **Spec and review:** every new tool follows the same English first, Indonesian translation pattern.

### Bug fixes

- **No known regression at release.** New code passed 54 unit tests and lint before CI.

## v1.23.0

Split at Blank Pages and Keep Odd/Even Pages, plus a fifteenth audit round.

### New features

- **Split at Blank Pages.** Detects fully blank separator pages in a scan and splits the PDF into parts, dropping the separators. Capped at 500 analyzed pages.
- **Keep Odd/Even Pages.** Keeps only odd- or even-numbered pages, for the front or back of duplex scans.

### Improvements

- **Tool catalog:** 51 tools; catalog test and README EN/ID updated.

### Bug fixes

- **Page numbers and markers on 180-degree pages (High).** `unrotate(180)` did not flip y, so page numbers, header/footer and crop placement landed on the wrong edge. Corrected, with the unit-test expectation updated.
- **Archive XMP packet (Medium).** Uses a spec-conformant empty `begin` attribute and declares PDF/A-2b (matching PDF 1.7) instead of the contradictory PDF/A-1b + 1.7 combination.
- **Read Aloud double-start (Medium).** The speak effect no longer writes one of its own effect keys, preventing a second flush/restart of the same page.
- **Locked PDF on the UI thread (Medium).** Empty-password unlock attempts and password-dialog retries now run on a background dispatcher.
- **Recent file tap (Low).** Reading a recent PDF now happens off the main thread.
- **Scanner spinner (Low).** `finish()` owns the busy state; caller `finally` blocks no longer cancel it early.
- **Page size report (Low).** Rounds instead of truncating and skips invalid page dimensions.

## v1.22.0

Archive Copy and Page Size Report, plus a fourteenth audit round with High fixes for Trim Margins and text-page requests.

### New features

- **Archive Copy.** Writes a minimal PDF/A-1b XMP identification marker and sets PDF version 1.7. Full conformance is not validated (no font check), and the tool says so.
- **Page Size Report.** Lists every distinct page size in points, millimetres and inches.

### Improvements

- **Tool catalog:** 49 tools; catalog test and README EN/ID updated.

### Bug fixes

- **Trim Margins vertical crop inverted (High).** Pixel bounds are top-down while PDF form space is bottom-up; the crop window is now converted to bottom-up coordinates before placing the page form.
- **Trim Margins ignored page rotation (High).** The trimmed form is now placed with the rotation-aware helper; displayed crop bounds apply to the displayed page.
- **Copy/Read Aloud no-op on first tap (High).** Tokens are kept until text for the requested page is ready, instead of being consumed when `pageText` is temporarily null.
- **Stale page text on incoming PDF (Medium).** `pageText` and `pageTextIndex` are now keyed by payload version.
- **Header/footer on rotated pages (Medium).** Uses the same rotation-aware placement as page numbers.
- **Temp sweep.** Added `trim-` prefix so a force-stop during Trim Margins no longer leaves a plaintext PDF copy in cache.
- **Split Every N Pages** now names single-page parts `page-N` consistently.

## v1.21.0

Two page-layout tools (Split Every N Pages, Trim Margins), plus a thirteenth audit round with a High header/footer placement fix.

### New features

- **Split Every N Pages.** Split a PDF into consecutive parts with a fixed number of pages (1-500) each.
- **Trim Margins.** Detect non-white content bounds on each scanned page and crop uniform white margins automatically.

### Improvements

- **Tool catalog:** 47 tools; catalog test and README EN/ID updated.

### Bug fixes

- **Footer-only stamping invisible (High).** Footer placement used a relative offset from a non-existent header text matrix, putting it off-page. Both header and footer now use absolute text matrices, correct for non-zero mediaBox origins.
- **Footer x-position (Medium).** A shadowed variable made the footer inherit the header's x; each line is now centered independently, and text is truncated by measured width.
- **Clipboard/speak token traps (Medium).** Tokens are consumed even when text extraction yields null, so a later page change cannot silently copy or speak.
- **Stale document state (Medium).** Search results, highlights and page text are now keyed by payload version, so a PDF arriving while the viewer is open does not inherit the previous file's results.
- **TTS generation guard (Medium).** Late errors from a stopped/superseded speech run no longer end a new one.
- **Scanner save crash (Medium).** A failed save now shows a generic error instead of throwing in an unguarded coroutine.

## v1.20.0

Search in PDFs and Header & Footer, plus a twelfth audit round that fixes the remaining Read Aloud edge cases.

### New features

- **Search in PDFs.** New tool: search one or more PDFs for a phrase and get a plain-text report of matching page numbers per file.
- **Header & Footer.** New tool: stamp a short header and/or footer line on every page, centered, with standard font and text normalization.

### Improvements

- **Tool catalog:** 45 tools; catalog test and README EN/ID updated.

### Bug fixes

- **Read Aloud clipboard leak (Medium).** The copy effect kept its token and re-fired when read-aloud changed `pageText`, overwriting the clipboard. The token is now consumed after one copy.
- **Read Aloud spoke the previous page (Medium).** `pageText` is now cleared when the requested page changes, so old text is never spoken or copied.
- **Save button hidden for shared PDFs (Medium).** `persisted` was remembered without a version key; incoming non-persisted PDFs shown in an already-open viewer now correctly offer Save to files.
- **TTS queue failure (Medium).** An error on any chunk now ends speech instead of leaving the UI stuck in "Stop reading"; utterance IDs include a generation counter so late callbacks from a stopped run cannot end a new one.
- **Rename errors (Low).** Failed renames now show an error in the dialog instead of silently closing it.

## v1.19.0

Eleventh audit round: stabilizes Read Aloud and fixes incoming-payload delivery.

### Improvements

- **Status bar and accessibility** unchanged; 43 tools and 48 unit tests remain green.

### Bug fixes

- **Read Aloud truncated long pages (High).** Text-to-speech chunks were queued with `QUEUE_ADD` and the last chunk used `QUEUE_FLUSH`, clearing the earlier chunks. Now the first chunk flushes and the rest are added; completion fires only on the final utterance and is posted to the main thread for Compose state.
- **Read Aloud did not restart (High).** Tapping speak, stopping, then tapping speak again on the same page did not re-trigger speech. A request counter is now an effect key.
- **TTS engine discovery.** Added the `TTS_SERVICE` query to the manifest so Android 11+ devices can find the device's speech engine.
- **Pending TTS text truncated.** While the synthesizer initialized, only the first 3500 characters were saved; the whole page is now replayed through the chunking helper.
- **Read Aloud clobbered the clipboard.** The speaker path no longer writes to the clipboard or shows the "copied" toast; only the Copy button does.
- **Incoming payload delivery (Medium).** `ViewerPayload.version` and `ShortcutRoute.version` are now Compose-backed state, so a PDF or shortcut delivered while on `shell` navigates immediately, and the viewer re-reads the payload when a new document arrives while it is open.
- **TTS shutdown** now resets state and clears pending utterances.

## v1.18.0

Flatten Pages, read-aloud in the viewer, and a tenth audit round fixing attachment extraction and privacy cleanup.

### New features

- **Flatten Pages.** New tool: renders each page to an image so its appearance is frozen. Editable text, links, forms and annotations are no longer present; text is no longer selectable. Can also be used as a workflow step.
- **Read aloud.** The viewer has a speaker button that reads the current page with the device's text-to-speech engine; tap again to stop. Uses the same text extraction as the copy-page-text action.

### Improvements

- **Tool catalog:** 43 tools; catalog test and README EN/ID updated.

### Bug fixes

- **Attachments with Unicode names (Medium).** Extraction now falls back from `/EF /UF` to `/EF /F`, and uses the attachment's own displayed filename (not the numeric name-tree key) with the correct extension. A 128 MB total cap prevents runaway memory use on hostile files.
- **Signature metadata left behind (Medium).** Remove Annotations now also clears the catalog `/Perms` entry so certification signature data does not survive a privacy cleanup.
- **Audit cleanup.** Dead annotation counter removed; error handling on malformed name trees retained.

## v1.17.0

Two privacy tools (Remove Annotations, Extract Attachments) plus a ninth audit round.

### New features

- **Remove Annotations.** Strip every comment, highlight, link and form field from all pages before sharing. Also drops the AcroForm dictionary.
- **Extract Attachments.** Save files embedded in the PDF name tree to the app's file tree, with sanitized names.

### Improvements

- **Tool catalog:** 42 tools; catalog test and README EN/ID updated.
- **README privacy FAQ corrected.** It no longer claims there is no network permission: `INTERNET` is used only for the on-demand update check.

### Bug fixes

- **Viewer back during decrypt (Medium).** `BackHandler` was registered after early returns, so system back during the empty-password decrypt spinner bypassed it and re-opened the viewer with stale payload. Moved before the early returns; disabled only while the password dialog is shown.
- **Redundant alpha in filtered PDFs.** Grayscale/invert output pages no longer carry an unnecessary all-opaque soft mask.
- **Changelog accuracy.** Corrected the description of non-Latin text handling in Text to PDF (characters are dropped, not replaced with `?`).

## v1.16.0

Grayscale and inverted page filters, plus an eighth audit round that fixes a critical blank-page bug in Text to PDF.

### New features

- **Grayscale PDF.** New tool: renders every page in grayscale at standard or high resolution, for e-ink readers and printing. Text becomes part of the page image.
- **Invert Pages.** New tool: inverts page colors for dark-background reading or negative printing. Text becomes part of the page image.

### Improvements

- **Tool catalog:** 40 tools; catalog test and README EN/ID updated; unit test count updated to 48.

### Bug fixes

- **Text to PDF was blank (Critical).** `beginText()` leaves the PDF text matrix at identity, but the code never positioned the first line, so every generated page was empty. First line is now positioned absolutely at the left/top margin; later lines use relative offsets. Added a unit test that extracts text back from the generated PDF.
- **Viewer system-back trap (High).** The hardware/system back key bypassed the viewer's `onBack`, leaving plaintext bytes in `ViewerPayload` and bouncing straight back into the viewer. Added a `BackHandler` in the viewer.
- **Non-Latin text crashed Text to PDF (Medium).** Input is now normalized with `winAnsi()` (as in watermark/fillSign/OCR), so curly quotes and CJK are dropped instead of crashing the tool.
- **Overlong words clipped (Medium).** Words wider than the page are hard-split into chunks; empty input now fails with "Enter some text." instead of producing a blank PDF.
- **Warm-start temp sweep.** Added `filter-` prefix so a force-stop during grayscale/invert no longer leaves plaintext PDF copies in cache.
- **ID/EN sync:** Indonesian README test count corrected.

## v1.15.0

Text to PDF and Resize Pages, plus a seventh audit round that fixes the owner-password role swap.

### New features

- **Text to PDF.** New tool: write typed or pasted text into a new PDF with automatic line wrapping and page breaks; choose A4/A5/Letter/Legal and small/normal/large font size.
- **Resize Pages.** New tool: fit every page onto a standard paper size while preserving aspect ratio and orientation, using native PDF form XObjects (no rasterization).

### Improvements

- **Tool catalog:** 38 tools; catalog test and README EN/ID updated.
- **Indonesian option hints:** text option placeholders now have localized versions; pin button accessibility labels localized.

### Bug fixes

- **Owner password role swap (Medium).** PDFBox's `StandardProtectionPolicy` takes (owner, user) in that order; the previous code passed the open password as owner and vice versa. Corrected, and a distinct owner password now applies sensible restrictions (no modify, annotations, form fill, assembly or extraction for the opening password).
- **Startup cache sweep.** Added prefixes for `blank-`, `dup-`, `lint-`, `report-`, `compare-` temp PDFs, so force-stop during those tools no longer leaves plaintext copies in cache.
- **Text tool placeholder** is localized for Bahasa Indonesia.

## v1.14.0

Build Bookmarks, a separate owner password, and a sixth audit round that fixes a blank-page detection data-loss bug.

### New features

- **Build Bookmarks.** New tool: add an outline/bookmarks to a PDF from lines like `1=Introduction, 5=Chapter One`; page numbers are 1-based and invalid lines are skipped. The viewer's bookmark navigation picks them up.
- **Owner password.** The Set Password tool now has an optional owner password, separate from the open password; it defaults to the open password when left blank.

### Improvements

- **Localized UI leftovers.** "Reset", "Color" and page accessibility labels are now localized in Organize, Fill & Sign, Crop, Redact and the viewer.
- **Unit test count** in README updated to match the suite (46 tests).

### Bug fixes

- **Blank-page detection (High).** The all-white check compared the whole 24-bit RGB integer, so a red or yellow mark could be counted as white and the page silently deleted. Now checks each channel separately.
- **Blank rendering.** Pages were rendered without clearing the bitmap to white first, so vector/empty blank pages reported as not blank and duplicates of white-vs-transparent pages were missed. Added `eraseColor(WHITE)` in both blank-page and duplicate detection.
- **Bitmap size bound.** Rendered page height is capped at 3200 px in both tools, preventing huge allocations on narrow/tall pages.
- **Incoming PDF delivery.** Sharing a PDF into the app while a tool/viewer screen is open now opens it on return to the shell (route added to the effect keys).
- **Print page adoption.** PrintOps now uses the shared `adopt` helper instead of a local copy.

## v1.13.0

Three page-cleanup tools for scanned and merged documents, plus audit hardening.

### New features

- **Remove Blank Pages.** Detects fully blank pages by low-res rendering and removes them; writes the cleaned PDF plus a text report of the removed page numbers. Capped at 400 analyzed pages.
- **Reverse Pages.** Reverses the page order of a PDF (e.g. a document scanned back to front), with a unit test that checks the new order and page sizes.
- **Find Duplicates** (v1.12.0) and **Remove Blank Pages** complement each other after scan + merge workflows.

### Improvements

- **Tool catalog:** 35 tools; catalog test and README EN/ID updated. Blank-page removal is excluded from workflow steps (it produces two outputs).

### Bug fixes

- **Page adoption shared.** The internal `adopt` helper is now shared between page-engine operations so reversed/rebuilt documents keep page resources.

## v1.12.0

Find Duplicates: a PDF tool that reports visually identical pages (e.g. repeated scan pages), plus audit hardening.

### New features

- **Find Duplicates.** New tool: renders each page at low res and hashes the pixels; groups pages that are visually identical and writes a plain-text report of the page numbers. Capped at 400 pages with a bounded bitmap size, so a large scan job does not blow up memory or time.

### Improvements

- **Tool catalog:** 33 tools, catalog test updated; `cariDuplikat` excluded from workflow steps (it produces a report, not a PDF).

### Bug fixes

- **Audit hardening:** no new defects found; Strings alignment enforced by regression tests.

## v1.11.0

New tools: PDF Lint and extension sorting. Fifth audit round across the app.

### New features

- **PDF Lint.** New tool: a plain-text report of a PDF's internals - version, fonts, encryption, producer, and a list of issues found (fonts not embedded, reportable oddities). One tap produces the `.txt`.
- **Sort by extension.** New option in the Files tab sort menu: sorts by file extension, then by name; folders still come first. Extensionless files sort by name.
- **Files tab UI polish.** Sort placeholder and plural strings are localized; moving a file to a folder that already has a same-named file shows an error instead of silently dropping the move; stale comments updated.

### Improvements

- **Files tab**: extension sort option added to the sort dropdown.
- **PDF Lint** fills `ToolCatalogTest` expectations (32 tools) and gets a dedicated JVM-based smoke test using the embedded Poppins font, so fonts are actually exercised on the JVM unit test run.

### Bug fixes

- **Files tab (fifth audit)**: stale sort comment updated; `moveError` surfaces `genericError` when the target folder already has a same-named file.
- **ComparePdf load ordering**: on an unreadable second PDF, the first document is now closed properly.
- **Compare excerpt marking**: truncated excerpts are now terminated with a `(excerpt truncated)` marker instead of looking like a complete diff; splitting is code-point-safe.
- **Compare unreadable pages**: pages whose text could not be read are now reported in the summary instead of being counted as identical.
- **extractImages + temporary file naming**: process-wide unique temp file names avoid collisions between parallel renders.
- **Dead strings**: `nothingYet`, `batchHint`, `imagesToPdfNote`, `jumpToPage`, `noTextOnPage`, `saveAs`, `savedCopies` deleted (unused); localization test extended to assert all new strings at both position and value level.

## v1.10.0

Two new tools, an Indonesian localization fix, and a fifth deep audit across the app.

### New features

- **Document report.** New tool: a plain-text report of a PDF's pages, first-page size, metadata (title, author, subject, keywords, creator, producer, dates), embedded image count and AcroForm field values, capped for large forms.
- **Remove Metadata.** New tool: strips the document information dictionary and XMP metadata in one tap, for sharing files without personal details. Can also be used as a step in Workflow Builder.

### Improvements

- **Indonesian strings realigned.** A positional shift had put sort labels, the rotate-all button, the decrypt notice and the whole update/sponsor section on the wrong fields in Bahasa Indonesia. Fixed, with a regression test.
- **Files tab work off the main thread.** Move-to folder listing is now computed on a background dispatcher and capped at 500 folders; delete confirmations and the one-time storage migration no longer block the UI.
- **Home recent files off the main thread.** The Home screen's recent-file query now runs on Dispatchers.IO.
- **Startup and cache clearing off the main thread.** The temp-file sweep at launch and the Clear cache row no longer run on the main thread.
- **Localized thumbnail label.** The viewer's page-thumbnail button now has an Indonesian content description.
- **Compare reports unreadable pages.** A page whose text could not be extracted is reported instead of silently counted as identical. Truncated excerpts are marked explicitly.

### Bug fixes

- **Compare resource leak.** A failure while opening the second PDF no longer leaves the first document open.
- **Range parsing rejects text.** Typo'd ranges such as `abc` or `x-3` now show an error instead of silently selecting page 1.
- **Temp file names unique.** A process-wide counter prevents two parallel renders from colliding on a timestamp-based path.
- **Skipped large images reported.** When Extract Images skips images above 60 megapixels, a `skipped-images.txt` note is produced alongside the results.
- **Compare tool needs two files.** Its CTA stays disabled until two PDFs are chosen instead of failing after the tap.

## v1.9.0

Compare PDFs, and a third audit pass across the app.

### New features

- **Compare PDFs.** New tool: compare two PDFs page by page and get a plain-text report showing which pages are identical, which changed and the exact lines that differ (capped and truncated per page). Useful for reviewing revisions of contracts and specs.

### Improvements

- **Recent files walk bounded.** Reopening recent PDFs no longer scans the whole Download/Pedeef tree on the main thread; the walk is capped and runs in the background.
- **Extract Images memory guard.** Images over 60 megapixels are skipped instead of risking an out-of-memory kill on scan-heavy PDFs, and extracted images are written into the ZIP one entry at a time instead of all being held before zipping.
- **Reading positions off the main thread.** The page-position save now runs on a background coroutine.
- **Shortcuts deliver during onboarding.** An incoming PDF shared while onboarding is not finished is opened as soon as the app reaches the main screen, instead of being dropped.

### Bug fixes

- **Print page ranges clamped.** An unclamped end-of-range no longer explodes the boxed index list; an empty selection reports an error instead of producing an empty job.
- **Shortcut ids collision-free.** Recent-file shortcuts use the full path as their id, not a 32-bit hash, so two paths can never collapse into one shortcut.
- **Blank PDF error message.** Invalid page counts show the intended message instead of a generic error.

## v1.8.0

A new app icon, reopen-recent PDFs from the launcher, Blank PDF, and a second audit pass of v1.7.0 with fixes.

### New features

- **New app icon.** The official Pedeef mark (a cream document over a peach page on a terracotta gradient tile), shipped as an adaptive icon with a monochrome themed layer for Android 13+, plus round and legacy icons. The same mark replaces the in-repo artwork.
- **Reopen recent PDFs.** Long-press the app icon and your most recently modified PDFs appear as shortcuts (up to 4); one tap reopens one straight into the viewer.
- **Blank PDF.** New tool: create an empty PDF at A4, A5, Letter or Legal, up to 200 pages, no source file required.

### Improvements

- **Resume reading race fixed.** The 600ms page-position save no longer overwrites the restored position before the resume scroll lands, so large or encrypted PDFs resume reliably.
- **Shortcut delivery outside the shell fixed.** A shortcut tapped while another screen is open (or the app onboarding is showing) now opens the target screen when the app returns to the shell, instead of being silently dropped.
- **Extract Images slimmer and capped.** JPEG-backed images are re-encoded as JPEG instead of ballooning into PNG, and total extracted data is capped with a clear error message instead of memory pressure.
- **Reading positions write atomically.** A crash mid-save can no longer wipe the stored reading positions.
- **Files tab loading state.** The Files tab shows a spinner on first render instead of briefly claiming "no files".
- **Decrypt notice.** When a password-protected PDF was opened without a password, the viewer shows a one-line note that saved and shared copies are not password-protected.

### Bug fixes

- **Print path streamlined.** Full-document print jobs no longer parse the PDF at all; partial ranges validate against the page count before rewriting.
- **Folder counts display.** Folders with more than 10,000 files now show "10,000+" instead of a bare number.
- **Version badge color.** README badges use the new brand terracotta.

## v1.7.0

Night mode, resume reading, launcher shortcuts, image extraction, and a second deep audit with fixes.

### New features

- **Night mode.** The viewer can invert page colors for comfortable reading in the dark. Toggle it from the viewer top bar or in Settings under Appearance.
- **Resume reading.** Pedeef remembers the last page you viewed per document and reopens it there.
- **Launcher shortcuts.** Long-press the app icon for Scan, Merge PDFs, Fill & sign and Organize pages.
- **Extract Images.** New tool: saves every embedded image in a PDF as a PNG, zipped when there are several. Icons smaller than 48px are skipped.

### Improvements

- **Decrypt off the main thread.** Opening an encrypted PDF no longer blocks the UI; a spinner shows while the empty-password attempt runs in the background.
- **Print fixed up.** Preparing a print job now happens on a background thread, and the temporary plaintext copy is deleted when the print service finishes without writing (for example when the dialog is dismissed).
- **Clear cache actually clears.** The Clear cache row now removes the whole cache directory, including shared copies and print leftovers, instead of a partial prefix list.
- **File walks off the main thread.** The Files tab lists and counts off the composition thread; sorting by date or size keeps folders first.
- **Crash-proof search and copy.** Corrupted PDFs no longer crash the viewer when searching or copying page text; a generic error message is shown instead.
- **Background saves.** Saving outputs from tools and saving from the viewer now run on a background thread with error handling instead of firing on the main thread.
- **Single version source.** The version shown in Settings and used for update checks now comes from the build configuration.
- **Tag comparison hardened.** Pre-release suffixes like `-beta` no longer compare as equal to the base release.

### Bug fixes

- **Garbled counter string.** The "files saved" message shown after saving tool outputs no longer contains garbled characters.
- **Copy button disabled on empty PDFs.** Copy page text is disabled when a document has no pages instead of passing a negative page index to the text stripper.
- **Folder item counts on a background thread.** Folder file counts no longer recompute synchronously during list rendering.
- **Incoming PDFs no longer stall launch.** A PDF opened from another app is read on a background thread.

## v1.6.0

Print from the viewer, save to file from the viewer, and audit hardening across the board.

### New features

- **Print.** A print icon in the viewer sends the current PDF to the system print dialog; pages can be printed as-is without wrapping.
- **Save to files from the viewer.** Open a PDF from another app or Files, inspect it, then tap Save to write it into Download/Pedeef (or app storage) only when you want it kept.
- **Sort files.** Sort your files by name, date or size in the Files tab with the new menu.
- **Rotate all.** In Organize Pages, one tap rotates every page by 90 degrees; individual pages can still be rotated as usual.

### Improvements

- **Rename safety.** Rename now refuses to overwrite an existing file instead of silently destroying it.
- **Stale payloads cleared.** Viewer state (password, saved flag, zoom) no longer leaks between PDFs; going back closes the document cleanly.
- **Rotated-page highlights fixed.** Search highlight boxes on pages with /Rotate are flipped on the vertical axis before mapping, so boxes land where the text actually is.
- **Viewer layout pinned.** Bookmarks and the page indicator no longer scroll sideways with the pages when zoomed in.
- **Settings walks off the main thread.** File counts and storage sizes are computed on a background thread with a 10k-capped size walk; UI input stays responsive.
- **Connection leaks fixed.** Update check disconnects the HTTP connection on every path.
- **Locked-file batch unlock.** Multiple password-protected PDFs picked in sequence are all unlocked before the dialog closes.
- **Migration .part files cleaned.** Leftover .part files from crashed copies are removed at the start of migration.

### Bug fixes

- **Cache leak sweeps.** Share and export temporary files are removed at startup and by the clear-cache action.

## v1.5.0

Opens PDFs from other apps, bookmark navigation in the viewer, deeper security fixes and the full audit pass.

### New features

- **PDF handler.** Open a PDF from another app or Files; Pedeef opens it in the viewer and only saves it to Download/Pedeef after you tap Save in the viewer's top bar.
- **Bookmark navigation.** The viewer shows an outline of bookmarks with one-tap jumps to each section.
- **Zoom presets.** Cycle between 100%, 125% and 150% zoom directly from the viewer's top bar.

### Improvements

- **Safer redaction.** The document-level cleanup list now covers PageLabels, Dests, associated files, collections, open actions and piece info; page-level AF entries are also removed.
- **Safer migration.** Public storage migration verifies every copied file by size, renames atomically, never overwrites your existing public files, writes the .migrated marker only when everything succeeds, and retries any partial run on next launch.
- **Safer sharing.** Share and export are disabled for folders; folder grants never leak a whole directory tree to another app.
- **Compression fidelity.** CMYK, YCCK and ICCBased JPEGs are no longer re-encoded; color shifts in compressed PDFs are avoided.
- **Locked-file handling.** Owner-password-only PDFs now open without a password prompt (empty password is tried first).
- **Rotated-page highlight.** Highlight boxes work on rotated pages instead of silently skipping them.
- **Reusable signature fix.** The "My signature" button works again: the saved signature is accepted as an app-generated image.

### Bug fixes

- **Plain cache leaks.** Temp share/export images and PDFs are swept on startup and the clear-cache row covers them.
- **Manifest hygiene.** The legacy external-storage flag is gone; read permission caps at API 29.

## v1.4.0

Search highlights, recent files and cache control: the audit goes deeper.

### New features

- **Search highlights in the viewer.** Find a phrase and every page with a hit is highlighted on the page itself; tap the grid icon for page thumbnails.
- **Recent files on Home.** The five most recently changed PDFs, one tap to open.

### Improvements

- **Bounded walks.** File size and count scans cap out at 10,000 entries, so empty states and huge folders stay fast.
- **Clear cache row.** One tap clears temporary files and their size is shown in Settings.

### Bug fixes

- **List stability.** The Files list now keys rows by absolute path, so jumping between folders does not leak state into rows.

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

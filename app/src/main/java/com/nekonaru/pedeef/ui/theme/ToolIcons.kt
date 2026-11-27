package com.nekonaru.pedeef.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.ui.graphics.vector.ImageVector

/** One icon per tool id. Every id in TOOLS must have an entry here. */
val TOOL_ICONS: Map<String, ImageVector> = mapOf(
  "workflow" to Icons.Default.AccountTree,
  "merge" to Icons.Default.MergeType,
  "alternate" to Icons.Default.Shuffle,
  "compress" to Icons.Default.Compress,
  "split" to Icons.Default.CallSplit,
  "splitText" to Icons.Default.Subject,
  "splitBookmarks" to Icons.Default.Bookmarks,
  "splitHalf" to Icons.Default.VerticalSplit,
  "splitSize" to Icons.Default.Storage,
  "zip" to Icons.Default.FolderZip,
  "organize" to Icons.Default.Reorder,
  "rotate" to Icons.Default.Rotate90DegreesCw,
  "flip" to Icons.Default.FlipToBack,
  "nup" to Icons.Default.GridView,
  "crop" to Icons.Default.Crop,
  "fillSign" to Icons.Default.Draw,
  "watermark" to Icons.Default.FormatColorText,
  "pageNumbers" to Icons.Default.FormatListNumbered,
  "redact" to Icons.Default.FormatColorFill,
  "metadata" to Icons.Default.Description,
  "ocr" to Icons.Default.TextFields,
  "unlock" to Icons.Default.LockOpen,
  "stripMetadata" to Icons.Default.CleaningServices,
  "password" to Icons.Default.Lock,
  "imagesToPdf" to Icons.Default.Image,
  "pdfToImages" to Icons.Default.PhotoLibrary,
  "extractText" to Icons.Default.ContentCopy,
  "extractImages" to Icons.Default.Collections,
  "reportPdf" to Icons.Default.Assessment,
  "comparePdf" to Icons.Default.Compare,
  "bookmarks" to Icons.Default.Bookmark,
  "reversePages" to Icons.Default.SwapHoriz,
  "hapusKosong" to Icons.Default.DeleteSweep,
  "splitBlanks" to Icons.Default.CallSplit,
  "keepOddEven" to Icons.Default.FilterAlt,
  "archiveCopy" to Icons.Default.Archive,
  "pageSizeReport" to Icons.Default.Straighten,
  "splitCount" to Icons.Default.CallSplit,
  "autoTrim" to Icons.Default.Crop,
  "multiSearch" to Icons.Default.Search,
  "headerFooter" to Icons.Default.FormatAlignCenter,
  "flattenPdf" to Icons.Default.Layers,
  "removeAnnotations" to Icons.Default.CleaningServices,
  "extractAttachments" to Icons.Default.Attachment,
  "grayscalePdf" to Icons.Default.FormatColorFill,
  "invertPdf" to Icons.Default.InvertColors,
  "textToPdf" to Icons.Default.TextFields,
  "resizePdf" to Icons.Default.AspectRatio,
  "cariDuplikat" to Icons.Default.ContentCopy,
  "lintPdf" to Icons.Default.CheckCircle,
  "blankPdf" to Icons.Default.NoteAdd,
  "imageStamp" to Icons.Default.Image,
  "highlightPhrase" to Icons.Default.Highlight,
  "replacePages" to Icons.Default.SwapHoriz,
  "overlayPdf" to Icons.Default.Layers,
  "booklet" to Icons.Default.Book,
  "poster" to Icons.Default.GridOn,
  "insertBlank" to Icons.Default.Add,
  "duplicatePages" to Icons.Default.ContentCopy,
  "borderPages" to Icons.Default.Rectangle,
  "batesNumber" to Icons.Default.Numbers,
  "textStamp" to Icons.Default.TextFields,
  "blackWhite" to Icons.Default.Contrast,
  "blankPageReport" to Icons.Default.Assessment,
  "extractComments" to Icons.Default.Comment,
  "removeEmbedded" to Icons.Default.CleaningServices,
  "removeJavaScript" to Icons.Default.Code,
  "protectDetailed" to Icons.Default.Lock,
  "unlockPassword" to Icons.Default.LockOpen,
  "pdfToLongImage" to Icons.Default.ViewStream,
  "imagesToLongImage" to Icons.Default.ViewStream,
  "extractTextPages" to Icons.Default.ContentCopy,
  "checksumManifest" to Icons.Default.Fingerprint,
  "integrityCheck" to Icons.Default.Verified,
  "sanitizeAll" to Icons.Default.CleaningServices,
  "pdfToTextZip" to Icons.Default.FolderZip,
  "pdfToTextFile" to Icons.Default.Description,
  "pdfToHtml" to Icons.Default.Code,
  "pdfToMarkdown" to Icons.Default.Subject,
  "appendImages" to Icons.Default.Add,
  "imagesToPdfFit" to Icons.Default.Image,
  "mergePageRanges" to Icons.Default.MergeType,
  "sortPages" to Icons.Default.Reorder,
  "rotateRange" to Icons.Default.Rotate90DegreesCw,
  "swapPages" to Icons.Default.SwapHoriz,
  "splitByOrientation" to Icons.Default.CallSplit,
  "pagesToZip" to Icons.Default.FolderZip,
  "tableOfContents" to Icons.Default.Bookmarks,
  "exportOutline" to Icons.Default.Bookmark,
  "outlineFromText" to Icons.Default.TextFields,
  "addLink" to Icons.Default.Code,
  "stickyNote" to Icons.Default.Comment,
  "dateStamp" to Icons.Default.Numbers,
  "coverPage" to Icons.Default.Book,
  "verifySignature" to Icons.Default.Verified,
  "removeSignatures" to Icons.Default.CleaningServices,
  "permissionPreset" to Icons.Default.Lock,
  "verifyChecksum" to Icons.Default.Fingerprint,
  "duplicateFilesReport" to Icons.Default.ContentCopy,
)

fun toolIcon(id: String): ImageVector = TOOL_ICONS[id] ?: Icons.Default.Description

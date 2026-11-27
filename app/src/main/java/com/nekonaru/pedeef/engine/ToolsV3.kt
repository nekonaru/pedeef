package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionURI
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationText
import java.io.File
import java.security.MessageDigest

/** Text extraction and export helpers for the v3 tool set. */
fun pdfToTextZip(name: String, bytes: ByteArray): ByteArray =
  zipFiles(listOf(Named("$name.txt", extractTextByPage(bytes))))

fun pdfToHtml(name: String, bytes: ByteArray): ByteArray {
  val body = extractText(bytes)
    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
  val html = "<!doctype html><html><head><meta charset=\"utf-8\"><title>$name</title></head><body><pre>$body</pre></body></html>"
  return html.toByteArray(Charsets.UTF_8)
}

fun pdfToMarkdown(name: String, bytes: ByteArray): ByteArray {
  val body = extractText(bytes)
  return ("# $name\n\n$body").toByteArray(Charsets.UTF_8)
}

fun appendImages(base: ByteArray, images: List<Named>): ByteArray {
  val decoded = images.map { it.name to decodeScaled(it.bytes) }
  val pages = imagesToPdf(decoded)
  return merge(listOf(Named("document", base), Named("images", pages)))
}

fun mergePageRanges(files: List<Named>, ranges: String): ByteArray {
  if (files.isEmpty()) fail("Pick a PDF first.")
  val parts = files.flatMap { split(it.bytes, "ranges", ranges) }
  if (parts.isEmpty()) fail("No pages matched those ranges.")
  return merge(parts)
}

fun sortPages(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val order = (0 until doc.numberOfPages)
    .sortedBy { doc.getPage(it).cropBox.width }
    .sortedBy { doc.getPage(it).cropBox.height }
    .map { PageOrder(it) }
  organize(bytes, order)
}

fun swapPages(bytes: ByteArray, a: Int, b: Int): ByteArray = loadPdf(bytes).use { doc ->
  val n = doc.numberOfPages
  if (a < 1 || b < 1 || a > n || b > n) fail("Page number is outside this document.")
  val order = (0 until n).map { PageOrder(it) }.toMutableList()
  val i = a - 1
  val j = b - 1
  val tmp = order[i]
  order[i] = order[j]
  order[j] = tmp
  organize(bytes, order)
}

fun splitByOrientation(bytes: ByteArray): List<Output> = loadPdf(bytes).use { src ->
  val portrait = PDDocument()
  val landscape = PDDocument()
  try {
    for (i in 0 until src.numberOfPages) {
      val page = src.getPage(i)
      val box = page.cropBox
      val target = if (box.height >= box.width) portrait else landscape
      target.importPage(page)
    }
    listOf(
      Output("portrait", portrait.bytes()),
      Output("landscape", landscape.bytes()),
    )
  } finally {
    portrait.close()
    landscape.close()
  }
}

fun pagesToZip(name: String, bytes: ByteArray, cacheDir: File): ByteArray = zipFiles(pdfToJpegs(bytes, cacheDir).map { Named(it.name + "." + it.ext, it.bytes) })

fun exportOutline(name: String, bytes: ByteArray): ByteArray {
  val outline = readOutline(bytes)
  if (outline.isEmpty()) fail("This PDF has no bookmarks.")
  val text = outline.joinToString("\n") { "${it.second + 1}=${it.first}" }
  return textToPdf(text, "A4", 11)
}

fun outlineFromText(bytes: ByteArray): ByteArray {
  val lines = extractText(bytes).lines()
    .map { it.trim() }
    .filter { Regex("^\\d{1,2}(\\.\\d{1,2})*\\s+\\S").matches(it) }
    .take(80)
  if (lines.isEmpty()) fail("No numbered headings were found in the text layer.")
  // Approximate page position by spreading headings over the document.
  val count = loadPdf(bytes).use { it.numberOfPages }
  val spec = lines.mapIndexed { i, line ->
    val page = 1 + (i.toLong() * count / lines.size).toInt()
    "$page=${line.take(80)}"
  }.joinToString("\n")
  return setBookmarks(bytes, spec)
}

fun addLink(bytes: ByteArray, pageNumber: Int, url: String): ByteArray = loadPdf(bytes).use { doc ->
  if (pageNumber < 1 || pageNumber > doc.numberOfPages) fail("Page number is outside this document.")
  if (!url.startsWith("http://") && !url.startsWith("https://")) fail("Use an http or https link.")
  val page = doc.getPage(pageNumber - 1)
  val box = page.cropBox
  val link = PDAnnotationLink().apply {
    setRectangle(com.tom_roush.pdfbox.pdmodel.common.PDRectangle(
      box.width * 0.1f, box.height * 0.1f, box.width * 0.9f, box.height * 0.3f,
    ))
    setAction(PDActionURI().apply { setURI(url) })
  }
  page.setAnnotations((page.annotations?.toMutableList() ?: mutableListOf()).apply { add(link) })
  doc.bytes()
}

fun stickyNote(bytes: ByteArray, pageNumber: Int, text: String): ByteArray = loadPdf(bytes).use { doc ->
  if (pageNumber < 1 || pageNumber > doc.numberOfPages) fail("Page number is outside this document.")
  if (text.isBlank()) fail("Write a note first.")
  val page = doc.getPage(pageNumber - 1)
  val note = PDAnnotationText().apply {
    setContents(text.take(1000))
    setRectangle(com.tom_roush.pdfbox.pdmodel.common.PDRectangle(20f, 20f, 44f, 44f))
  }
  page.setAnnotations((page.annotations?.toMutableList() ?: mutableListOf()).apply { add(note) })
  doc.bytes()
}

fun dateStamp(bytes: ByteArray, prefix: String, x: Float, y: Float): ByteArray {
  val stamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
  val text = if (prefix.isBlank()) stamp else "$prefix $stamp"
  return stampText(bytes, text, x, y)
}

fun coverPage(bytes: ByteArray, title: String): ByteArray = loadPdf(bytes).use { doc ->
  val cover = textToPdf(title, "A4", 24)
  loadPdf(cover).use { coverDoc ->
    val out = PDDocument()
    try {
      for (i in 0 until coverDoc.numberOfPages) out.addPage(PDPage(coverDoc.getPage(i).cropBox))
      for (i in 0 until doc.numberOfPages) out.addPage(PDPage(doc.getPage(i).cropBox))
      out.bytes()
    } finally {
      out.close()
    }
  }
}

fun verifySignature(bytes: ByteArray): ByteArray {
  val raw = String(bytes.copyOfRange(0, minOf(bytes.size, 2_000_000)), Charsets.ISO_8859_1)
  val found = raw.contains("/Sig") || raw.contains("/ByteRange") || raw.contains("/AcroForm")
  val text = if (found) {
    "A signature field or signature structure was found. Pedeef cannot verify the certificate chain offline."
  } else {
    "No signature field was found in this PDF."
  }
  return textToPdf(text, "A4", 12)
}

fun removeSignatures(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  var removed = 0
  for (i in 0 until doc.numberOfPages) {
    val page = doc.getPage(i)
    val annotations = page.annotations?.toList().orEmpty()
    val kept = annotations.filter { annotation ->
      val subtype = annotation.subtype ?: ""
      val skip = subtype.contains("Sig", ignoreCase = true)
      if (skip) removed++
      !skip
    }
    if (kept.size != annotations.size) page.setAnnotations(kept)
  }
  if (removed == 0) fail("No signature field was found.")
  doc.bytes()
}

fun permissionPreset(bytes: ByteArray, password: String, preset: String): ByteArray? {
  val readOnly = preset == "read"
  val printOnly = preset == "print"
  return protectWithPermissions(
    bytes = bytes,
    openPassword = password,
    ownerPassword = password,
    print = true,
    copy = !readOnly && !printOnly,
    modify = !readOnly && !printOnly,
    annotate = !readOnly,
    fillForms = !readOnly,
    assemble = !readOnly,
  )
}

fun sha256Hex(bytes: ByteArray): String =
  MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

fun verifyChecksum(files: List<Named>, manifest: ByteArray): ByteArray {
  val expected = HashMap<String, String>()
  String(manifest, Charsets.UTF_8).lines().forEach { line ->
    val parts = line.trim().split(Regex("\\s+"), limit = 2)
    if (parts.size == 2 && parts[0].matches(Regex("[0-9a-fA-F]{64}"))) expected[parts[1]] = parts[0].lowercase()
  }
  if (expected.isEmpty()) fail("The manifest has no checksum rows.")
  val rows = files.map { f ->
    val actual = sha256Hex(f.bytes)
    val want = expected[f.name]
    val status = when {
      want == null -> "MISSING"
      want == actual -> "OK"
      else -> "MISMATCH"
    }
    "$status  ${f.name}"
  }
  return textToPdf(rows.joinToString("\n"), "A4", 10)
}

fun duplicateFilesReport(files: List<Named>): ByteArray {
  val groups = files.groupBy { sha256Hex(it.bytes) }.filter { it.value.size > 1 }
  if (groups.isEmpty()) fail("No duplicate files were found in this set.")
  val rows = groups.values.flatMap { group -> group.map { it.name } }
  return textToPdf(rows.joinToString("\n"), "A4", 10)
}

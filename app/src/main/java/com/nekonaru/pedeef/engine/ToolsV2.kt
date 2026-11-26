package com.nekonaru.pedeef.engine

import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

/** Places an image on every page. Input must contain one PDF and one image. */
fun stampImage(files: List<Named>, x: Float, y: Float, size: Float, opacity: Float): ByteArray {
  val pdf = files.firstOrNull { it.name.endsWith(".pdf", true) } ?: fail("Pick one PDF and one image.")
  val img = files.firstOrNull { !it.name.endsWith(".pdf", true) } ?: fail("Pick one PDF and one image.")
  val bitmap = runCatching { BitmapFactory.decodeByteArray(img.bytes, 0, img.bytes.size) }.getOrNull() ?: fail("Could not decode the image.")
  return loadPdf(pdf.bytes).use { doc ->
    val image = LosslessFactory.createFromImage(doc, bitmap)
    val ratio = bitmap.height.toFloat() / bitmap.width
    bitmap.recycle()
    for (page in doc.pages) {
      val cb = page.cropBox
      val wFrac = size.coerceIn(0.02f, 0.95f)
      val hFrac = (wFrac * cb.width * ratio / cb.height).coerceAtMost(0.95f)
      val at = placeOnPage(listOf(x, y, wFrac, hFrac), ((page.rotation % 360) + 360) % 360, cb.width, cb.height)
      doc.draw(page) {
        alpha(opacity.coerceIn(0.05f, 1f))
        drawImage(image, cb.lowerLeftX + at.x, cb.lowerLeftY + at.y, at.w, at.h)
      }
    }
    doc.bytes()
  }
}

private class PhraseBoxes(val needle: String) : PDFTextStripper() {
  val boxes = ArrayList<android.graphics.RectF>()
  init { sortByPosition = true }
  override fun writeString(text: String?, positions: MutableList<TextPosition>?) {
    if (text.isNullOrEmpty() || positions.isNullOrEmpty()) return
    if (!text.lowercase().replace(Regex("\\s+"), "").contains(needle)) return
    val l = positions.minOf { it.xDirAdj }
    val r = positions.maxOf { it.xDirAdj + it.widthDirAdj }
    val t = positions.minOf { it.yDirAdj }
    val b = positions.maxOf { it.yDirAdj + it.heightDir }
    if (r > l && b > t) boxes.add(android.graphics.RectF(l, t, r, b))
  }
}

/** Draws translucent highlight rectangles over every occurrence of a phrase. */
fun highlightPhrase(bytes: ByteArray, phrase: String): ByteArray {
  val needle = phrase.trim().lowercase().replace(Regex("\\s+"), "")
  if (needle.isEmpty()) fail("Enter text to highlight.")
  return loadPdf(bytes).use { doc ->
    for (i in 0 until doc.numberOfPages) {
      val collector = PhraseBoxes(needle)
      collector.startPage = i + 1
      collector.endPage = i + 1
      runCatching { collector.getText(doc) }
      if (collector.boxes.isEmpty()) continue
      val page = doc.getPage(i)
      val cb = page.cropBox
      doc.draw(page) {
        setNonStrokingColor(1f, 0.84f, 0.24f)
        alpha(0.45f)
        for (b in collector.boxes) {
          addRect(cb.lowerLeftX + b.left, cb.lowerLeftY + cb.height - b.bottom, b.width(), b.height())
          fill()
        }
      }
    }
    doc.bytes()
  }
}

/** Replaces selected pages of [bytes] with pages from [replacement]. */
fun replacePages(bytes: ByteArray, replacement: ByteArray, pages: String): ByteArray = loadPdf(bytes).use { base ->
  loadPdf(replacement).use { rep ->
    val indices = if (pages.isBlank()) (0 until base.numberOfPages).toSet() else parseRanges(pages, base.numberOfPages).flatten().toSet()
    PDDocument().use { out ->
      for (i in 0 until base.numberOfPages) {
        out.adopt(if (i in indices && rep.numberOfPages > 0) rep.getPage(i % rep.numberOfPages) else base.getPage(i))
      }
      out.bytes()
    }
  }
}

/** Overlays the second PDF on top of the first, page for page. */
fun overlayPdfs(baseBytes: ByteArray, overlayBytes: ByteArray): ByteArray = loadPdf(baseBytes).use { base ->
  loadPdf(overlayBytes).use { overlay ->
    PDDocument().use { out ->
      for (i in 0 until base.numberOfPages) {
        val cb = base.getPage(i).cropBox
        val page = PDPage(PDRectangle(cb.width, cb.height)).also { out.addPage(it) }
        val a = out.embedShown(base.getPage(i))
        val b = out.embedShown(overlay.getPage(i % overlay.numberOfPages))
        out.draw(page) { drawShown(a, 0f, 0f, 1f); drawShown(b, 0f, 0f, 1f) }
        page.rotation = base.getPage(i).rotation
      }
      out.bytes()
    }
  }
}

/** Imposes pages two per sheet for a folded booklet. */
fun makeBooklet(bytes: ByteArray): ByteArray = loadPdf(bytes).use { src ->
  PDDocument().use { out ->
    var i = 0
    while (i < src.numberOfPages) {
      val cb = src.getPage(i).cropBox
      val sheet = PDPage(PDRectangle(cb.width * 2, cb.height)).also { out.addPage(it) }
      val a = out.embedShown(src.getPage(i))
      out.draw(sheet) { drawShown(a, 0f, 0f, 1f) }
      if (i + 1 < src.numberOfPages) {
        val b = out.embedShown(src.getPage(i + 1))
        out.draw(sheet) { drawShown(b, cb.width, 0f, 1f) }
      }
      i += 2
    }
    out.bytes()
  }
}

/** Splits each page into a grid of tiles, like a poster printed across sheets. */
fun makePoster(bytes: ByteArray, cols: Int, rows: Int, overlapPt: Float = 12f): ByteArray {
  if (cols !in 1..8 || rows !in 1..8) fail("Choose 1 to 8 columns and rows.")
  return loadPdf(bytes).use { src ->
    PDDocument().use { out ->
      for (p in src.pages) {
        val cb = p.cropBox
        val e = out.embedShown(p)
        val pw = e.shownW / cols
        val ph = e.shownH / rows
        for (row in 0 until rows) for (col in 0 until cols) {
          val w = pw + overlapPt * 2
          val h = ph + overlapPt * 2
          val tile = PDPage(PDRectangle(w, h)).also { out.addPage(it) }
          val x = col * pw - overlapPt
          val y = row * ph - overlapPt
          out.draw(tile) { drawShown(e, -x, -(e.shownH - y - h), 1f) }
        }
      }
      out.bytes()
    }
  }
}

/** Rebuilds a PDF with blank pages inserted after the given 1-based page. */
fun insertBlankPages(bytes: ByteArray, after: Int, count: Int): ByteArray {
  if (count !in 1..50) fail("Choose between 1 and 50 blank pages.")
  return loadPdf(bytes).use { src ->
    PDDocument().use { out ->
      if (after <= 0) repeat(count) { out.addPage(PDPage(PDRectangle(src.getPage(0).cropBox.width, src.getPage(0).cropBox.height))) }
      for (i in 0 until src.numberOfPages) {
        out.adopt(src.getPage(i))
        if (i + 1 == after) repeat(count) { out.addPage(PDPage(PDRectangle(src.getPage(i).cropBox.width, src.getPage(i).cropBox.height))) }
      }
      out.bytes()
    }
  }
}

/** Rebuilds a PDF with selected pages repeated according to their multiplicity. */
fun duplicatePages(bytes: ByteArray, pages: String): ByteArray = loadPdf(bytes).use { src ->
  val counts = HashMap<Int, Int>()
  parseRanges(pages, src.numberOfPages).flatten().forEach { counts[it] = (counts[it] ?: 0) + 1 }
  if (counts.isEmpty()) fail("Choose pages to duplicate.")
  PDDocument().use { out ->
    for (i in 0 until src.numberOfPages) {
      repeat(1 + (counts[i] ?: 0)) { out.adopt(src.getPage(i)) }
    }
    out.bytes()
  }
}

/** Draws a border on every page. */
fun borderPages(bytes: ByteArray, widthPt: Float): ByteArray = loadPdf(bytes).use { doc ->
  for (page in doc.pages) {
    val cb = page.cropBox
    doc.draw(page) {
      setLineWidth(widthPt.coerceIn(0.5f, 24f))
      setNonStrokingColor(0.9f, 0.3f, 0.1f)
      setStrokingColor(0.9f, 0.3f, 0.1f)
      addRect(cb.lowerLeftX + 6, cb.lowerLeftY + 6, cb.width - 12, cb.height - 12)
      stroke()
    }
  }
  doc.bytes()
}

/** Bates-style numbering with a prefix and a starting number. */
fun batesNumber(bytes: ByteArray, prefix: String, start: Int): ByteArray = loadPdf(bytes).use { doc ->
  val font = PDType1Font.HELVETICA
  doc.pages.forEachIndexed { i, p ->
    val label = (prefix.trim().ifEmpty { "" }) + (start + i).toString()
    val cb = p.cropBox
    val at = placeOnPage(listOf(0.95f, 0.96f, 0f, 0.02f), ((p.rotation % 360) + 360) % 360, cb.width, cb.height)
    doc.draw(p) { putText(font, 10f, label, cb.lowerLeftX + at.x - font.getStringWidth(label) / 1000f * 10f, cb.lowerLeftY + at.y, ((p.rotation % 360) + 360) % 360f) }
  }
  doc.bytes()
}

/** Stamps the same short text at a displayed page position on every page. */
fun stampText(bytes: ByteArray, text: String, x: Float, y: Float): ByteArray {
  val value = winAnsi(text.trim())
  if (value.isEmpty()) fail("Enter text to stamp.")
  return loadPdf(bytes).use { doc ->
    val font = PDType1Font.HELVETICA
    for (page in doc.pages) {
      val cb = page.cropBox
      val rot = ((page.rotation % 360) + 360) % 360
      val at = placeOnPage(listOf(x, y, 0f, 0.03f), rot, cb.width, cb.height)
      doc.draw(page) { putText(font, 12f, value, cb.lowerLeftX + at.x, cb.lowerLeftY + at.y, rot.toFloat()) }
    }
    doc.bytes()
  }
}

/** Extracts annotation/comment text into a plain report. */
fun extractComments(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val sb = StringBuilder("Pedeef comments\n")
  var n = 0
  doc.pages.forEachIndexed { i, page ->
    for (a in page.annotations) {
      val text = runCatching { a.contents.orEmpty() }.getOrDefault("").trim()
      if (text.isEmpty()) continue
      n++
      sb.append("Page ${i + 1} (${a.subtype ?: "annotation"}): $text\n")
    }
  }
  if (n == 0) sb.append("No comments found.")
  Output("comments", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

/** Removes the embedded-files name tree. */
fun removeEmbeddedFiles(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val names = runCatching { doc.documentCatalog.names }.getOrNull()
  names?.setEmbeddedFiles(null)
  doc.bytes()
}

/** Removes JavaScript, launch actions and document-level extra actions. */
fun removeJavaScript(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val cat = doc.documentCatalog.cosObject
  cat.removeItem(COSName.getPDFName("JavaScript"))
  cat.removeItem(COSName.getPDFName("OpenAction"))
  cat.removeItem(COSName.getPDFName("AA"))
  runCatching { doc.documentCatalog.names }.getOrNull()?.cosObject?.removeItem(COSName.getPDFName("JavaScript"))
  for (page in doc.pages) for (a in page.annotations) {
    a.cosObject.removeItem(COSName.getPDFName("A"))
    a.cosObject.removeItem(COSName.getPDFName("AA"))
  }
  doc.bytes()
}

/** Extracts text with explicit page markers. */
fun extractTextByPage(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val sb = StringBuilder()
  val stripper = PDFTextStripper()
  for (i in 1..doc.numberOfPages) {
    stripper.startPage = i
    stripper.endPage = i
    sb.append("--- Page $i ---\n")
    sb.append(runCatching { stripper.getText(doc) }.getOrDefault("[unreadable]").trim())
    sb.append("\n\n")
  }
  Output("text-by-page", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

/** SHA-256 of every supplied file, as a text manifest. */
fun checksumManifest(files: List<Named>): ByteArray {
  val sb = StringBuilder("Pedeef checksums\n")
  for (f in files) {
    val digest = MessageDigest.getInstance("SHA-256").digest(f.bytes).joinToString("") { "%02x".format(it.toInt() and 0xFF) }
    sb.append("$digest  ${f.name}\n")
  }
  return Output("sha256", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

/** Runs a lightweight integrity pass over pages and reports failures. */
fun integrityCheck(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val sb = StringBuilder("Pedeef integrity check\nPages: ${doc.numberOfPages}\n")
  var bad = 0
  for (i in 0 until doc.numberOfPages) {
    val ok = runCatching {
      val p = doc.getPage(i)
      p.contents.use { it.readBytes() }
      val s = PDFTextStripper().apply { startPage = i + 1; endPage = i + 1 }
      s.getText(doc)
      p.annotations.size
      true
    }.getOrDefault(false)
    if (!ok) { bad++; sb.append("Page ${i + 1}: unreadable\n") }
  }
  sb.append(if (bad == 0) "All pages readable." else "Unreadable pages: $bad")
  Output("integrity", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

/** Combines all one-tap cleanup operations. */
fun sanitizeAll(bytes: ByteArray): ByteArray {
  var out = stripMetadata(bytes)
  out = removeAnnotations(out)
  out = removeEmbeddedFiles(out)
  out = removeJavaScript(out)
  return out
}

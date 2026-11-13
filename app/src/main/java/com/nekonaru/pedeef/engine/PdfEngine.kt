package com.nekonaru.pedeef.engine

import android.graphics.BitmapFactory
import com.tom_roush.harmony.awt.geom.AffineTransform
import com.tom_roush.pdfbox.cos.COSArray
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.cos.COSStream
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.common.PDStream
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.util.Matrix
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Every operation runs on-device with PdfBox: bytes in, bytes out.

fun loadPdf(bytes: ByteArray): PDDocument = try {
  PDDocument.load(bytes)
} catch (_: InvalidPasswordException) {
  fail("This PDF is password-protected. Unlock it first.")
} catch (_: Exception) {
  fail("Could not read this PDF. The file may be damaged.")
}

fun PDDocument.bytes(): ByteArray = ByteArrayOutputStream().use { save(it); it.toByteArray() }

private val PDPage.rot: Int get() = ((rotation % 360) + 360) % 360

/** Copies a page into this document, keeping the resources it relies on. */
private fun PDDocument.adopt(p: PDPage): PDPage {
  val n = importPage(p)
  if (!n.cosObject.containsKey(COSName.RESOURCES)) n.resources = p.resources ?: PDResources()
  return n
}

private fun pick(src: PDDocument, indices: List<Int>): ByteArray = PDDocument().use { out ->
  for (i in indices) out.adopt(src.getPage(i))
  out.bytes()
}

fun merge(files: List<Named>): ByteArray {
  val sources = files.map { loadPdf(it.bytes) }
  try {
    return PDDocument().use { out ->
      for (src in sources) for (p in src.pages) out.adopt(p)
      out.bytes()
    }
  } finally {
    sources.forEach { it.close() }
  }
}

fun alternate(files: List<Named>, reverse: Boolean): ByteArray {
  val sources = files.map { loadPdf(it.bytes) }
  try {
    val stacks = sources.mapIndexed { i, src -> src.pages.toList().let { if (reverse && i > 0) it.reversed() else it } }
    return PDDocument().use { out ->
      for (i in 0 until (stacks.maxOfOrNull { it.size } ?: 0)) for (s in stacks) s.getOrNull(i)?.let { out.adopt(it) }
      out.bytes()
    }
  } finally {
    sources.forEach { it.close() }
  }
}

fun split(bytes: ByteArray, mode: String, ranges: String): List<Named> = loadPdf(bytes).use { src ->
  val groups = if (mode == "ranges") parseRanges(ranges, src.numberOfPages) else (0 until src.numberOfPages).map { listOf(it) }
  groups.map { g -> Named(if (g.size > 1) "pages-${g.first() + 1}-${g.last() + 1}" else "page-${g.first() + 1}", pick(src, g)) }
}

private fun norm(s: String) = s.lowercase().replace(Regex("\\s+"), "")

fun splitByText(bytes: ByteArray, phrase: String): List<Named> {
  val needle = norm(phrase)
  if (needle.isEmpty()) fail("Enter the phrase to split at.")
  return loadPdf(bytes).use { src ->
    val stripper = PDFTextStripper()
    val groups = ArrayList<MutableList<Int>>()
    var hits = 0
    for (i in 0 until src.numberOfPages) {
      stripper.startPage = i + 1
      stripper.endPage = i + 1
      val hit = norm(runCatching { stripper.getText(src) }.getOrDefault("")).contains(needle)
      if (hit) hits++
      if (groups.isEmpty() || (hit && groups.last().isNotEmpty())) groups.add(mutableListOf())
      groups.last().add(i)
    }
    if (hits == 0) fail("\"$phrase\" was not found on any page. If this is a scan, run OCR Scanned PDF on it first.")
    groups.mapIndexed { k, g -> Named("part-${k + 1}", pick(src, g)) }
  }
}

fun splitByBookmarks(bytes: ByteArray): List<Named> = loadPdf(bytes).use { src ->
  val n = src.numberOfPages
  val marks = ArrayList<Pair<String, Int>>()
  var item = src.documentCatalog.documentOutline?.firstChild
  var guard = 0
  while (item != null && guard++ < 10000) {
    val page = runCatching { item.findDestinationPage(src) }.getOrNull()
    val i = page?.let { src.pages.indexOf(it) } ?: -1
    if (i >= 0) marks.add((item.title?.takeIf { it.isNotBlank() } ?: "Section ${marks.size + 1}") to i)
    item = item.nextSibling
  }
  if (marks.isEmpty()) fail("This PDF has no bookmarks to split by.")
  marks.sortBy { it.second }
  if (marks[0].second > 0) marks.add(0, "Front matter" to 0)
  val uniq = marks.filterIndexed { i, m -> i == 0 || m.second != marks[i - 1].second }
  uniq.mapIndexed { i, (title, page) -> Named(title.take(80), pick(src, (page until (uniq.getOrNull(i + 1)?.second ?: n)).toList())) }
}

fun splitHalf(bytes: ByteArray, mode: String): List<Named> = loadPdf(bytes).use { src ->
  when (mode) {
    "file" -> {
      val n = src.numberOfPages
      val mid = (n + 1) / 2
      listOfNotNull(
        Named("part-1", pick(src, (0 until mid).toList())),
        if (mid < n) Named("part-2", pick(src, (mid until n).toList())) else null,
      )
    }
    else -> {
      val out = PDDocument()
      out.use {
        for (p in src.pages) {
          val cb = p.cropBox
          val halves = if (mode == "vertical") listOf(listOf(0f, 0f, 0.5f, 1f), listOf(0.5f, 0f, 0.5f, 1f))
          else listOf(listOf(0f, 0f, 1f, 0.5f), listOf(0f, 0.5f, 1f, 0.5f))
          for (half in halves) {
            val q = out.adopt(p)
            val (x, y, w, h) = unrotate(half, p.rot)
            val box = PDRectangle(cb.lowerLeftX + x * cb.width, cb.lowerLeftY + y * cb.height, w * cb.width, h * cb.height)
            q.mediaBox = box
            q.cropBox = box
          }
        }
        listOf(Named("sliced", out.bytes()))
      }
    }
  }
}

/** Longest side each photo is shrunk to, by compression level. */
internal val MAX_SIDE = mapOf(80 to 0, 60 to 2400, 45 to 2000, 35 to 1600, 25 to 1200, 20 to 900)

/** Compresses embedded JPEGs (and drops XMP metadata). Never returns a bigger file. */
fun compress(bytes: ByteArray, quality: Int, maxSide: Int = MAX_SIDE[quality] ?: 0): ByteArray =
  loadPdf(bytes).use { doc ->
    for (obj in doc.document.objects) {
      val st = obj.getObject() as? COSStream ?: continue
      if (st.getCOSName(COSName.SUBTYPE) != COSName.IMAGE) continue
      val filter = st.getDictionaryObject(COSName.FILTER)
      val jpeg = filter == COSName.DCT_DECODE ||
        (filter is COSArray && filter.size() == 1 && filter.getObject(0) == COSName.DCT_DECODE)
      if (!jpeg) continue
      try {
        val raw = st.createRawInputStream().use { it.readBytes() }
        val out = recompressJpeg(raw, quality, maxSide) ?: continue
        val (w, h) = jpegSize(out) ?: continue
        if (out.size >= raw.size) continue
        st.createRawOutputStream().use { it.write(out) }
        st.setItem(COSName.FILTER, COSName.DCT_DECODE)
        st.setItem(COSName.COLORSPACE, COSName.DEVICERGB)
        st.setInt(COSName.BITS_PER_COMPONENT, 8)
        st.setInt(COSName.WIDTH, w)
        st.setInt(COSName.HEIGHT, h)
        st.removeItem(COSName.DECODE)
        st.removeItem(COSName.DECODE_PARMS) // a ColorTransform meant for the old JPEG would skew the new one's colors
      } catch (_: Exception) {
        // unusual JPEG flavor: keep the original image
      }
    }
    doc.documentCatalog.cosObject.removeItem(COSName.METADATA)
    val out = doc.bytes()
    if (out.size < bytes.size) out else bytes
  }

/** Compresses just enough to fit under `targetMB`: the gentlest step that fits. */
fun compressToSize(bytes: ByteArray, targetMB: String): ByteArray {
  val target = ((targetMB.toFloatOrNull() ?: 0f) * 1024 * 1024).toLong()
  if (target <= 0) fail("Enter the size to fit under, in MB.")
  if (bytes.size <= target) return compress(bytes, 80) // already fits: only the lossless tidy-up
  var best = bytes
  for ((q, side) in listOf(80 to 0, 60 to 2400, 45 to 2000, 35 to 1600, 25 to 1200, 20 to 900)) {
    val out = compress(bytes, q, side)
    if (out.size < best.size) best = out
    if (out.size <= target) return out
  }
  return best
}

/** A copy with no password or restrictions. Locked files are opened with their password before any tool sees them. */
fun removePassword(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  doc.isAllSecurityToBeRemoved = true
  doc.bytes()
}

/** AES-256. Null when the PDF is already encrypted. */
fun setPdfPassword(bytes: ByteArray, password: String): ByteArray? {
  val doc = try {
    PDDocument.load(bytes)
  } catch (_: InvalidPasswordException) {
    return null
  }
  doc.use {
    if (it.isEncrypted) return null
    it.protect(StandardProtectionPolicy(password, password, AccessPermission()).apply { encryptionKeyLength = 256 })
    return it.bytes()
  }
}

/** Title, author, subject and keywords, as most PDF apps and file managers show them. */
fun readDetails(bytes: ByteArray): Map<String, String> = loadPdf(bytes).use { doc ->
  val info = doc.documentInformation
  mapOf(
    "title" to info.title,
    "author" to info.author,
    "subject" to info.subject,
    "keywords" to info.keywords,
  ).mapValues { it.value.orEmpty() }
}

/** Sets the details; an empty field removes it. */
fun writeDetails(bytes: ByteArray, title: String, author: String, subject: String, keywords: String): ByteArray = loadPdf(bytes).use { doc ->
  val info = doc.documentInformation
  info.title = title.trim().ifEmpty { null }
  info.author = author.trim().ifEmpty { null }
  info.subject = subject.trim().ifEmpty { null }
  info.keywords = keywords.trim().ifEmpty { null }
  doc.documentInformation = info
  doc.documentCatalog.cosObject.removeItem(COSName.METADATA)
  doc.bytes()
}

fun splitBySize(bytes: ByteArray, maxMB: String): List<Named> {
  val limit = (maxMB.toFloatOrNull() ?: 0f) * 1024 * 1024
  if (limit <= 0f) fail("Enter a size limit in MB.")
  return loadPdf(bytes).use { src ->
    val parts = ArrayList<ByteArray>()
    var cur = mutableListOf<Int>()
    var last: ByteArray? = null
    for (i in 0 until src.numberOfPages) {
      val b = pick(src, cur + i)
      if (b.size > limit && cur.isNotEmpty() && last != null) {
        parts.add(last)
        cur = mutableListOf(i)
        last = pick(src, cur)
      } else {
        cur.add(i)
        last = b
      }
    }
    last?.let { parts.add(it) }
    parts.mapIndexed { k, b -> Named("part-${k + 1}", b) }
  }
}

fun rotate(bytes: ByteArray, angle: Int, pages: String): ByteArray = loadPdf(bytes).use { doc ->
  val sel = if (pages.isNotBlank()) parseRanges(pages, doc.numberOfPages).flatten().toSet() else (0 until doc.numberOfPages).toSet()
  for (i in sel) doc.getPage(i).let { it.rotation = (it.rotation + angle) % 360 }
  doc.bytes()
}

/** A form XObject showing what a viewer shows of the page, w x h, its origin at the lower-left. */
private class Shown(val form: PDFormXObject, val w: Float, val h: Float, val rot: Int) {
  val shownW get() = if (rot % 180 != 0) h else w
  val shownH get() = if (rot % 180 != 0) w else h
}

private fun PDDocument.embedShown(p: PDPage): Shown {
  val cb = p.cropBox
  val form = PDFormXObject(PDStream(this, p.contents, COSName.FLATE_DECODE))
  form.resources = p.resources ?: PDResources()
  form.bBox = PDRectangle(cb.lowerLeftX, cb.lowerLeftY, cb.width, cb.height)
  form.setMatrix(AffineTransform.getTranslateInstance(-cb.lowerLeftX.toDouble(), -cb.lowerLeftY.toDouble()))
  return Shown(form, cb.width, cb.height, p.rot)
}

private fun PDPageContentStream.place(form: PDFormXObject, x: Float, y: Float, sx: Float, sy: Float = sx, deg: Float = 0f) {
  saveGraphicsState()
  transform(Matrix.getTranslateInstance(x, y))
  if (deg != 0f) transform(Matrix.getRotateInstance(Math.toRadians(deg.toDouble()), 0f, 0f))
  transform(Matrix.getScaleInstance(sx, sy))
  drawForm(form)
  restoreGraphicsState()
}

private fun PDPageContentStream.drawShown(e: Shown, x: Float, y: Float, s: Float) {
  val w = e.w * s
  val h = e.h * s
  val (ox, oy) = when (e.rot) {
    90 -> x to y + w
    180 -> x + w to y + h
    270 -> x + h to y
    else -> x to y
  }
  place(e.form, ox, oy, s, s, -e.rot.toFloat())
}

private fun PDDocument.newPage(w: Float, h: Float) = PDPage(PDRectangle(w, h)).also { addPage(it) }
private fun PDDocument.draw(p: PDPage, block: PDPageContentStream.() -> Unit) =
  PDPageContentStream(this, p, PDPageContentStream.AppendMode.APPEND, true, true).use(block)

fun flip(bytes: ByteArray, axis: String): ByteArray = loadPdf(bytes).use { src ->
  PDDocument().use { out ->
    for (p in src.pages) {
      val e = out.embedShown(p)
      // The page keeps its /Rotate, so mirroring it as displayed means the other axis of a page turned a quarter.
      val fx = if (e.rot % 180 != 0) axis != "h" else axis != "v"
      val fy = if (e.rot % 180 != 0) axis != "v" else axis != "h"
      val page = out.newPage(e.w, e.h)
      out.draw(page) { place(e.form, if (fx) e.w else 0f, if (fy) e.h else 0f, if (fx) -1f else 1f, if (fy) -1f else 1f) }
      page.rotation = e.rot
    }
    out.bytes()
  }
}

private val GRID = mapOf("2" to (2 to 1), "4" to (2 to 2), "6" to (2 to 3), "9" to (3 to 3), "16" to (4 to 4))

fun nUp(bytes: ByteArray, perSheet: String, lines: Boolean): ByteArray {
  val (c0, r0) = GRID[perSheet] ?: fail("Choose 2, 4, 6, 9 or 16 pages per sheet.")
  return loadPdf(bytes).use { src ->
    PDDocument().use { out ->
      val shown = src.pages.map { out.embedShown(it) }
      if (shown.isEmpty()) fail("This PDF has no pages.")
      val m = 18f
      val a4w = SIZES.getValue("A4").first
      val a4h = SIZES.getValue("A4").second
      val portrait = a4w to a4h
      val landscape = a4h to a4w
      val w0 = shown.first().shownW
      val h0 = shown.first().shownH
      data class L(val cols: Int, val rows: Int, val w: Float, val h: Float)
      val a = if (c0 > r0) L(c0, r0, landscape.first, landscape.second) else L(c0, r0, portrait.first, portrait.second)
      val b = if (c0 > r0) L(r0, c0, portrait.first, portrait.second) else L(r0, c0, landscape.first, landscape.second)
      fun fit(l: L) = min((l.w - m * 2) / l.cols / w0, (l.h - m * 2) / l.rows / h0)
      val (cols, rows, W, H) = if (fit(b) > fit(a)) b else a
      val cw = (W - m * 2) / cols
      val ch = (H - m * 2) / rows
      var cs: PDPageContentStream? = null
      try {
        shown.forEachIndexed { i, e ->
          val k = i % (cols * rows)
          if (k == 0) {
            cs?.close()
            cs = PDPageContentStream(out, out.newPage(W, H)).also { s ->
              if (lines) {
                s.setStrokingColor(0.6f, 0.6f, 0.6f)
                s.setLineWidth(0.5f)
                for (col in 1 until cols) { s.moveTo(m + col * cw, m); s.lineTo(m + col * cw, H - m) }
                for (row in 1 until rows) { s.moveTo(m, H - m - row * ch); s.lineTo(W - m, H - m - row * ch) }
                s.stroke()
              }
            }
          }
          val s = min((cw - 8) / e.shownW, (ch - 8) / e.shownH)
          val col = k % cols
          val row = k / cols
          cs!!.drawShown(e, m + col * cw + (cw - e.shownW * s) / 2, H - m - (row + 1) * ch + (ch - e.shownH * s) / 2, s)
        }
      } finally {
        cs?.close()
      }
      out.bytes()
    }
  }
}

/** What to keep on a page, as fractions of the page as displayed, y from the top. Keyed by page index. */
fun cropResize(bytes: ByteArray, mode: String, size: String, boxes: Map<String, Box>): ByteArray = loadPdf(bytes).use { src ->
  if (mode == "crop") {
    if (boxes.isEmpty()) fail("Pull a page's corners in to choose what to keep.")
    src.pages.forEachIndexed { i, p ->
      val box = boxes[i.toString()] ?: return@forEachIndexed
      val cb = p.cropBox
      val (x, y, w, h) = unrotate(box, p.rot)
      if (w * cb.width < 1 || h * cb.height < 1) fail("The crop on page ${i + 1} is too small.")
      p.cropBox = PDRectangle(cb.lowerLeftX + x * cb.width, cb.lowerLeftY + y * cb.height, w * cb.width, h * cb.height)
    }
    return@use src.bytes()
  }
  val (a, b) = SIZES[size] ?: SIZES.getValue("A4")
  PDDocument().use { out ->
    for (p in src.pages) {
      val e = out.embedShown(p)
      val (W, H) = if (e.shownW > e.shownH) b to a else a to b
      val s = min(W / e.shownW, H / e.shownH)
      val page = out.newPage(W, H)
      out.draw(page) { drawShown(e, (W - e.shownW * s) / 2, (H - e.shownH * s) / 2, s) }
    }
    out.bytes()
  }
}

class WatermarkLayout(val angle: Float, val size: Float, val textWidth: Float)

/** Diagonal watermark geometry in page points; the preview uses it too so both match. */
fun watermarkLayout(text: String, width: Float, height: Float, scale: Float): WatermarkLayout {
  val w1 = PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000f
  val k = if (scale > 0) scale else 1f
  val size = min(110 * k, hypot(width, height) * 0.65f * k / w1)
  return WatermarkLayout(atan2(height, width), size, w1 * size)
}

private fun PDPageContentStream.alpha(a: Float) {
  if (a >= 1f) return
  setGraphicsStateParameters(PDExtendedGraphicsState().apply { nonStrokingAlphaConstant = a; strokingAlphaConstant = a })
}

private fun PDPageContentStream.putText(font: PDType1Font, size: Float, text: String, x: Float, y: Float, deg: Float) {
  beginText()
  setFont(font, size)
  setTextMatrix(Matrix.getRotateInstance(Math.toRadians(deg.toDouble()), x, y))
  showText(text)
  endText()
}

fun watermark(bytes: ByteArray, typed: String, opacity: Float, scale: Float): ByteArray {
  if (typed.isBlank()) fail("Enter watermark text.")
  val text = winAnsi(typed).trim()
  if (text.isEmpty()) fail("Watermarks can only use Latin letters, numbers and common symbols.")
  return loadPdf(bytes).use { doc ->
    val font = PDType1Font.HELVETICA_BOLD
    for (p in doc.pages) {
      val cb = p.cropBox
      val width = cb.width
      val height = cb.height
      val l = watermarkLayout(text, width, height, scale)
      val csAngle = cos(l.angle)
      val sn = sin(l.angle)
      val up = l.size * 0.35f
      doc.draw(p) {
        alpha(opacity)
        setNonStrokingColor(0.82f, 0.27f, 0.17f)
        putText(
          font, l.size, text, cb.lowerLeftX + width / 2 - csAngle * l.textWidth / 2 + sn * up,
          cb.lowerLeftY + height / 2 - sn * l.textWidth / 2 - csAngle * up, (l.angle * 180 / PI).toFloat(),
        )
      }
    }
    doc.bytes()
  }
}

private const val DESCENT = 0.79f // Helvetica sits about a fifth of its size below the baseline

class Placement(val x: Float, val y: Float, val w: Float, val h: Float)

/** Where to draw an item placed on the page as displayed, relative to the page's stored frame. */
fun placeOnPage(rect: Box, rot: Int, W: Float, H: Float): Placement {
  val (fx, fy, fw, fh) = unrotate(rect, rot)
  val X = fx * W; val Y = fy * H; val RW = fw * W; val RH = fh * H
  return when (rot) {
    90 -> Placement(X + RW, Y, RH, RW)
    180 -> Placement(X + RW, Y + RH, RW, RH)
    270 -> Placement(X, Y + RH, RH, RW)
    else -> Placement(X, Y, RW, RH)
  }
}

fun pageNumbers(bytes: ByteArray, position: String, format: String): ByteArray = loadPdf(bytes).use { doc ->
  val font = PDType1Font.HELVETICA
  val n = doc.numberOfPages
  doc.pages.forEachIndexed { i, p ->
    val label = if (format == "of") "Page ${i + 1} of $n" else "${i + 1}"
    val tw = font.getStringWidth(label) / 1000f * 10
    val cb = p.cropBox
    val rot = p.rot
    val (dw, dh) = if (rot % 180 != 0) cb.height to cb.width else cb.width to cb.height
    val lx = when (position) {
      "left" -> 28f
      "right" -> dw - 28 - tw
      else -> (dw - tw) / 2
    }
    val at = placeOnPage(listOf(lx / dw, (dh - 20 - 10 * DESCENT) / dh, 0f, 10 * DESCENT / dh), rot, cb.width, cb.height)
    doc.draw(p) {
      setNonStrokingColor(0.2f, 0.2f, 0.2f)
      putText(font, 10f, label, cb.lowerLeftX + at.x, cb.lowerLeftY + at.y, rot.toFloat())
    }
  }
  doc.bytes()
}

fun organize(bytes: ByteArray, all: List<PageOrder>): ByteArray {
  val order = all.filter { !it.del }
  if (order.isEmpty()) fail("Keep at least one page.")
  return loadPdf(bytes).use { src ->
    PDDocument().use { out ->
      for (o in order) out.adopt(src.getPage(o.i)).let { it.rotation = (it.rotation + o.rot) % 360 }
      out.bytes()
    }
  }
}

class Turned(val x: Float, val y: Float, val rotate: Float)

/**
 * Where to draw an item that sits, as displayed, with its top-left at fractions (x, y) of the page, is w x h points,
 * and is turned `angle` degrees clockwise about its centre. `oy` lifts the drawing origin above the item's bottom edge.
 */
fun placeTurned(x: Float, y: Float, w: Float, h: Float, oy: Float, angle: Float, rot: Int, W: Float, H: Float): Turned {
  val (dw, dh) = if (rot % 180 != 0) H to W else W to H
  val cx = x * dw + w / 2
  val cy = dh - y * dh - h / 2
  val t = -angle * PI.toFloat() / 180
  val lx = -w / 2
  val ly = oy - h / 2
  val X = cx + lx * cos(t) - ly * sin(t)
  val Y = cy + lx * sin(t) + ly * cos(t)
  val (u, v) = when (rot) {
    90 -> (W - Y) to X
    180 -> (W - X) to (H - Y)
    270 -> Y to (H - X)
    else -> X to Y
  }
  return Turned(u, v, rot - angle)
}

private fun hexRgb(hex: String?): Triple<Float, Float, Float> {
  val def = "#0F172A"
  val m = Regex("^#?([0-9a-fA-F]{6})$").find(hex ?: def) ?: Regex("^#?([0-9a-fA-F]{6})$").find(def)!!
  val n = m.groupValues[1].toInt(16)
  return Triple(((n shr 16) and 255) / 255f, ((n shr 8) and 255) / 255f, (n and 255) / 255f)
}

fun fillSign(bytes: ByteArray, items: List<SignItem>, images: Map<String, ByteArray>): ByteArray =
  loadPdf(bytes).use { doc ->
    val embedded = HashMap<String, PDImageXObject>()
    val font = PDType1Font.HELVETICA
    for (item in items) {
      if (item.page !in 0 until doc.numberOfPages) continue
      val p = doc.getPage(item.page)
      val rot = p.rot
      val cb = p.cropBox
      val W = cb.width
      val H = cb.height
      val (dw, dh) = if (rot % 180 != 0) H to W else W to H
      val op = item.opacity.coerceIn(0.05f, 1f)
      if (item.kind == "image") {
        val img = embedded[item.uri] ?: run {
          val b = images[item.uri] ?: return@run null
          when {
            isPng(b) -> BitmapFactory.decodeByteArray(b, 0, b.size)?.let { LosslessFactory.createFromImage(doc, it) }
            isJpeg(b) -> JPEGFactory.createFromByteArray(doc, b)
            else -> fail("The picture on page ${item.page + 1} is not a JPEG or PNG.")
          }?.also { embedded[item.uri] = it }
        } ?: continue
        val w = item.w * dw
        val h = item.h * dh
        val at = placeTurned(item.x, item.y, w, h, 0f, item.angle, rot, W, H)
        doc.draw(p) {
          alpha(op)
          transform(Matrix.getTranslateInstance(cb.lowerLeftX + at.x, cb.lowerLeftY + at.y))
          transform(Matrix.getRotateInstance(Math.toRadians(at.rotate.toDouble()), 0f, 0f))
          drawImage(img, 0f, 0f, w, h)
        }
        continue
      }
      val text = winAnsi(item.text)
      if (text.isEmpty()) continue
      val size = item.size * dh
      val at = placeTurned(item.x, item.y, font.getStringWidth(text) / 1000f * size, size, size * (1 - DESCENT), item.angle, rot, W, H)
      val (r, g, b) = hexRgb(item.color)
      doc.draw(p) {
        alpha(op)
        setNonStrokingColor(r, g, b)
        putText(font, size, text, cb.lowerLeftX + at.x, cb.lowerLeftY + at.y, at.rotate)
      }
    }
    doc.bytes()
  }

fun zipFiles(files: List<Named>): ByteArray = ByteArrayOutputStream().use { bos ->
  ZipOutputStream(bos).use { zip ->
    val seen = HashSet<String>()
    files.forEachIndexed { i, f ->
      val name = if (f.name in seen) "${i + 1}-${f.name}" else f.name
      seen.add(name)
      zip.putNextEntry(ZipEntry(name))
      zip.write(f.bytes)
      zip.closeEntry()
    }
  }
  bos.toByteArray()
}

fun extractText(bytes: ByteArray): String = loadPdf(bytes).use { doc -> PDFTextStripper().getText(doc) }

/** Decodes an image, sampling down so the longest side is near `maxSide`. */
internal fun decodeScaled(bytes: ByteArray, maxSide: Int = 2200): android.graphics.Bitmap {
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
  val long = maxOf(bounds.outWidth, bounds.outHeight)
  var sample = 1
  while (maxSide > 0 && long / (sample * 2) >= maxSide) sample *= 2
  return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    ?: throw UserError("Could not decode an image.")
}

/** Builds a PDF with one page per image, each page sized to the image's pixel aspect ratio. */
fun imagesToPdf(images: List<Pair<String, android.graphics.Bitmap>>): ByteArray {
  if (images.isEmpty()) fail("Pick at least one image.")
  PDDocument().use { doc ->
    val maxSide = 2200f // keep the embedded picture well under format limits
    for ((_, bitmap) in images) {
      val k = min(1f, maxSide / max(bitmap.width, bitmap.height).toFloat())
      val w = max(1f, bitmap.width * k)
      val h = max(1f, bitmap.height * k)
      val page = PDPage(PDRectangle(w, h))
      doc.addPage(page)
      val xobject = LosslessFactory.createFromImage(doc, bitmap)
      PDPageContentStream(doc, page).use { cs -> cs.drawImage(xobject, 0f, 0f, w, h) }
    }
    return doc.bytes()
  }
}

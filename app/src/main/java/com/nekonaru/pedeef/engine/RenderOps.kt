package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.util.Matrix
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max

// File- and render-based operations: need android.graphics or PdfRenderer.

/** Runs `block` with the bytes written to a temp file (PdfRenderer needs a file descriptor). */
fun <T> withTempPdf(bytes: ByteArray, cacheDir: File, tag: String, block: (File) -> T): T {
  val tmp = File(cacheDir, "$tag-${System.currentTimeMillis()}.pdf").apply { writeBytes(bytes) }
  try {
    return block(tmp)
  } finally {
    tmp.delete()
  }
}

/** True when the bytes are an encrypted PDF, decided by scanning for /Encrypt. Cheap, no full parse. */
fun mentionsEncrypt(bytes: ByteArray): Boolean {
  val head = bytes.copyOfRange(0, minOf(bytes.size, 16384))
  val tail = if (bytes.size > 65536) bytes.copyOfRange(bytes.size - 65536, bytes.size) else ByteArray(0)
  return String(head, Charsets.ISO_8859_1).contains("/Encrypt") || String(tail, Charsets.ISO_8859_1).contains("/Encrypt")
}

/** A decrypted copy of a locked PDF, or null when the password is wrong or it was not encrypted. */
fun unlockBytes(bytes: ByteArray, password: String): ByteArray? {
  if (!mentionsEncrypt(bytes)) return bytes
  return try {
    PDDocument.load(bytes, password).use { doc ->
      doc.isAllSecurityToBeRemoved = true
      ByteArrayOutputStream().use { out -> doc.save(out); out.toByteArray() }
    }
  } catch (_: InvalidPasswordException) {
    null
  } catch (_: Exception) {
    null
  }
}

/** Page sizes in pixels at `renderWidth` (aspect only, the exact width is the render target). */
fun pagePixelSizes(path: String, renderWidth: Int = 1000): List<Pair<Int, Int>> {
  val pfd = android.os.ParcelFileDescriptor.open(File(path), android.os.ParcelFileDescriptor.MODE_READ_ONLY)
  pfd.use { fd ->
    PdfRenderer(fd).use { r ->
      return (0 until r.pageCount).map { i ->
        r.openPage(i).use { p ->
          val k = renderWidth.toFloat() / p.width
          renderWidth to maxOf(1, (p.height * k).toInt())
        }
      }
    }
  }
}

private fun renderBitmap(path: String, index: Int, maxWidth: Int): Bitmap {
  val pfd = android.os.ParcelFileDescriptor.open(File(path), android.os.ParcelFileDescriptor.MODE_READ_ONLY)
  pfd.use { fd ->
    PdfRenderer(fd).use { r ->
      r.openPage(index).use { p ->
        val k = minOf(maxWidth.toFloat() / p.width, 3f)
        // Cap the longest side too: a page with extreme geometry must not ask for a gigapixel bitmap.
        val w0 = maxOf(1, (p.width * k).toInt())
        val h0 = maxOf(1, (p.height * k).toInt())
        val shrink = minOf(1f, 3200f / max(w0, h0).toFloat())
        val bmp = Bitmap.createBitmap(maxOf(1, (w0 * shrink).toInt()), maxOf(1, (h0 * shrink).toInt()), Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bmp
      }
    }
  }
}

/** Renders one page to a JPEG, capped at `maxWidth` px. */
fun renderPageJpeg(path: String, index: Int, maxWidth: Int, quality: Int = 85): ByteArray {
  val bmp = renderBitmap(path, index, maxWidth)
  val out = ByteArrayOutputStream()
  bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
  bmp.recycle()
  return out.toByteArray()
}

/** Every page as a JPEG at 144 dpi (at most 2400 px wide). One page: a .jpg. More: a .zip of page-XXX.jpg. */
fun pdfToJpegs(bytes: ByteArray, cacheDir: File): List<Output> = withTempPdf(bytes, cacheDir, "export") { tmp ->
  val pfd = android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
  pfd.use { fd ->
    PdfRenderer(fd).use { r ->
      val n = r.pageCount
      if (n == 1) {
        return@withTempPdf listOf(Output("page-1", renderPageJpeg(tmp.path, 0, 2400, 90), ext = "jpg"))
      }
      val out = ByteArrayOutputStream()
      ZipOutputStream(BufferedOutputStream(out)).use { zip ->
        zip.setLevel(Deflater.NO_COMPRESSION) // JPEGs do not shrink any further
        for (i in 0 until n) {
          zip.putNextEntry(ZipEntry("page-%03d.jpg".format(i + 1)))
          zip.write(renderPageJpeg(tmp.path, i, 2400, 90))
          zip.closeEntry()
        }
      }
      listOf(Output("pages", out.toByteArray(), ext = "zip"))
    }
  }
}

/**
 * Redaction that removes, not covers. Every page with boxes is rendered at ~216 dpi with the boxes painted black,
 * and that picture becomes the page's only content. Annotation, form and tag trees on those pages are dropped too,
 * since they can hold copies of the redacted text.
 */
fun redactPdf(bytes: ByteArray, boxes: Map<String, List<Box>>, cacheDir: File): ByteArray {
  if (boxes.isEmpty()) fail("Drag across at least one area to black it out.")
  return withTempPdf(bytes, cacheDir, "redact") { tmp ->
    PDDocument.load(tmp).use { doc ->
      val redacted = mutableSetOf<Int>()
      val pfd = android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
      pfd.use { fd ->
        PdfRenderer(fd).use { r ->
          for ((key, list) in boxes) {
            val i = key.toIntOrNull() ?: continue
            if (list.isEmpty() || i !in 0 until r.pageCount) continue
            r.openPage(i).use { p ->
              val bw = minOf(p.width * 3, 3000) // ~216 dpi: enough to paint out text
              // Cap the longest side: extreme page geometry must not ask for a gigapixel bitmap.
              val bh = minOf((p.height * bw.toFloat() / p.width).toInt().coerceAtLeast(1), 3200)
              val bitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
              bitmap.eraseColor(Color.WHITE)
              p.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
              val canvas = Canvas(bitmap)
              val black = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
              for (b in list) {
                val x = b[0] * bw
                val y = b[1] * bh
                canvas.drawRect(x, y, x + b[2] * bw, y + b[3] * bh, black)
              }
              bitmap.setHasAlpha(false) // no soft mask in the PDF
              val img = JPEGFactory.createFromImage(doc, bitmap, 0.9f)
              bitmap.recycle()
              val page = doc.getPage(i)
              for (name in listOf("Annots", "Thumb", "PieceInfo", "Metadata", "StructParents", "B", "AA", "Tabs", "ArtBox", "BleedBox", "TrimBox", "UserUnit", "Group", "VP", "SeparationInfo", "ID", "PZ")) {
                page.cosObject.removeItem(COSName.getPDFName(name))
              }
              val box = PDRectangle(p.width.toFloat(), p.height.toFloat()) // the picture is already the visible area
              page.mediaBox = box
              page.cropBox = box
              page.rotation = 0
              page.resources = com.tom_roush.pdfbox.pdmodel.PDResources()
              PDPageContentStream(doc, page, PDPageContentStream.AppendMode.OVERWRITE, true).use { it.drawImage(img, 0f, 0f, p.width.toFloat(), p.height.toFloat()) }
              redacted += i
            }
          }
        }
      }
      if (redacted.isNotEmpty()) {
        val catalog = doc.documentCatalog.cosObject
        // The document's title, author, subject, keywords, bookmarks and embedded files (and the XMP copy) can
        // repeat what was redacted.
        listOf("AcroForm", "StructTreeRoot", "MarkInfo", "Metadata", "Outlines", "Names").forEach { catalog.removeItem(COSName.getPDFName(it)) }
        doc.documentInformation = com.tom_roush.pdfbox.pdmodel.PDDocumentInformation()
        // Annotations elsewhere can still point at the removed ones, which would keep them in the file.
        for ((i, page) in doc.pages.withIndex()) {
          if (i in redacted) continue
          for (annot in page.annotations) {
            annot.cosObject.removeItem(COSName.getPDFName("Parent"))
            annot.cosObject.removeItem(COSName.getPDFName("IRT"))
          }
        }
      }
      ByteArrayOutputStream().use { out -> doc.save(out); out.toByteArray() }
    }
  }
}

private fun realChars(s: String): Int = s.count { !it.isWhitespace() }

private fun hasScanImage(res: PDResources?, depth: Int = 0): Boolean {
  if (res == null || depth > 2) return false
  return res.xObjectNames.any { name ->
    when (val x = runCatching { res.getXObject(name) }.getOrNull()) {
      is com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject ->
        x.width >= 600 && x.height >= 600 && x.width.toLong() * x.height >= 300_000L
      is com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject -> depth < 2 && hasScanImage(x.resources, depth + 1)
      else -> false
    }
  }
}

/**
 * Makes a scanned PDF searchable: every page that has no real text is rendered, read by ML Kit, and given an
 * invisible text layer over the words. Fails with a message saying why when nothing was added.
 */
fun ocrPdf(bytes: ByteArray, cacheDir: File): ByteArray {
  return withTempPdf(bytes, cacheDir, "ocr") { tmp ->
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
      PDDocument.load(tmp).use { doc ->
        val stripper = PDFTextStripper()
        var added = 0
        var searchable = 0
        var count = 0
        val pfd = android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
        pfd.use { fd ->
          PdfRenderer(fd).use { r ->
            count = minOf(doc.numberOfPages, r.pageCount)
            for (i in 0 until count) {              stripper.startPage = i + 1
              stripper.endPage = i + 1
              val chars = realChars(runCatching { stripper.getText(doc) }.getOrDefault(""))
              val scanned = hasScanImage(doc.getPage(i).resources, 0)
              if (chars >= (if (scanned) 300 else 50)) { searchable++; continue }
              val lines = r.openPage(i).use { p ->
                val bw = minOf(p.width * 3, 3000)
                val bh = minOf((p.height * bw.toFloat() / p.width).toInt().coerceAtLeast(1), 3200)
                val bitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                p.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                try {
                  com.google.android.gms.tasks.Tasks.await(
                    recognizer.process(InputImage.fromBitmap(bitmap, 0)), 120, java.util.concurrent.TimeUnit.SECONDS,
                  ).textBlocks.flatMap { it.lines }.mapNotNull { line ->
                    val box = line.boundingBox ?: return@mapNotNull null
                    val text = winAnsi(line.text)
                    val width = try { PDType1Font.HELVETICA.getStringWidth(text) } catch (_: Exception) { 0f }
                    if (text.isEmpty() || width <= 0f || box.width() <= 0 || box.height() <= 0) null
                    else Triple(text, android.graphics.RectF(box.left / bw.toFloat(), box.top / bh.toFloat(), box.right / bw.toFloat(), box.bottom / bh.toFloat()), width)
                  }
                } catch (_: Exception) {
                  emptyList()
                } finally {
                  bitmap.recycle()
                }
              }
              if (lines.isEmpty()) continue
              val page = doc.getPage(i)
              val rot = ((page.rotation % 360) + 360) % 360
              val box = page.cropBox
              val pw = if (rot % 180 == 0) box.width else box.height  // the page as displayed
              val ph = if (rot % 180 == 0) box.height else box.width
              PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                cs.beginText()
                cs.setRenderingMode(RenderingMode.NEITHER) // there for search and selection only, never drawn
                for ((text, rect, width) in lines) {
                  val w = (rect.right - rect.left) * pw
                  val h = (rect.bottom - rect.top) * ph
                  val size = h.coerceAtLeast(1f)
                  // Same placement math as Fill & Sign: correct for pages stored with /Rotate.
                  val at = placeTurned(rect.left, rect.top, w, h, size * 0.21f, 0f, rot, box.width, box.height)
                  cs.setFont(PDType1Font.HELVETICA, size)
                  // Stretch the line to the width of the words underneath, so a selection lands on them.
                  cs.setHorizontalScaling(w / (width / 1000f * size) * 100f)
                  cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(at.rotate.toDouble()), box.lowerLeftX + at.x, box.lowerLeftY + at.y))
                  cs.showText(text)
                  added++
                }
                cs.endText()
              }
            }
          }
        }
        if (added > 0) return@withTempPdf ByteArrayOutputStream().use { out -> doc.save(out); out.toByteArray() }
        if (count > 0 && searchable == count) fail("This PDF already has text on every page.")
        fail("No text could be read on the pages. They may be blank, handwritten, or too blurry or small to read.")
      }
    } finally {
      recognizer.close()
    }
  }
}

/** Re-encodes one JPEG at `quality`, its long side at most `maxSide` px (0 keeps it). Null if Android cannot decode it. */
fun recompressJpeg(bytes: ByteArray, quality: Int, maxSide: Int): ByteArray? {
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
  val long = maxOf(bounds.outWidth, bounds.outHeight)
  if (long <= 0) return null
  // Never decode more than 4096 px on the long side, whatever the caller asked for.
  val cap = if (maxSide > 0) minOf(maxSide, 4096) else 4096
  var sample = 1 // decode near the target size rather than whole: a 6000 px scan is ~140 MB as pixels
  while (long / (sample * 2) >= cap) sample *= 2
  val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
  val k = minOf(1f, cap.toFloat() / maxOf(decoded.width, decoded.height))
  val scaled = if (k < 1f) Bitmap.createScaledBitmap(decoded, maxOf(1, (decoded.width * k).toInt()), maxOf(1, (decoded.height * k).toInt()), true) else decoded
  val out = ByteArrayOutputStream()
  scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
  if (scaled !== decoded) decoded.recycle()
  scaled.recycle()
  return out.toByteArray()
}

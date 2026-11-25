package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import java.io.ByteArrayOutputStream

/** Trims uniform white margins around the visible content of every page. */
fun autoTrim(bytes: ByteArray, cacheDir: java.io.File, threshold: Int = 245, padPt: Float = 12f): ByteArray = withTempPdf(bytes, cacheDir, "trim") { tmp ->
  val src = loadPdf(bytes)
  val out = PDDocument()
  try {
    android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
      PdfRenderer(fd).use { r ->
        for (i in 0 until r.pageCount) {
          r.openPage(i).use { rendered ->
            val page = src.getPage(i)
            val scale = 300f / maxOf(1, rendered.width)
            val w = maxOf(1, (rendered.width * scale).toInt())
            val h = maxOf(1, (rendered.height * scale).toInt())
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(android.graphics.Color.WHITE)
            rendered.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val box = contentBounds(bmp, threshold)
            bmp.recycle()
            val content = out.embedShown(page)
            val sx = content.shownW / w
            val sy = content.shownH / h
            val left = (box[0] * sx - padPt).coerceAtLeast(0f)
            val top = (box[1] * sy - padPt).coerceAtLeast(0f)
            val cw = ((box[2] - box[0]) * sx + 2 * padPt).coerceAtMost(content.shownW - left)
            val ch = ((box[3] - box[1]) * sy + 2 * padPt).coerceAtMost(content.shownH - top)
            val np = PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle(cw, ch)).also { out.addPage(it) }
            PDPageContentStream(out, np).use { cs ->
              cs.saveGraphicsState()
              cs.transform(com.tom_roush.pdfbox.util.Matrix.getTranslateInstance(-left, -top))
              cs.drawForm(content.form)
              cs.restoreGraphicsState()
            }
          }
        }
      }
    }
    ByteArrayOutputStream().use { out.save(it); it.toByteArray() }
  } finally {
    out.close()
    src.close()
  }
}

/** Returns (left, top, right, bottom) pixel bounds of non-white content. */
private fun contentBounds(bmp: Bitmap, threshold: Int): IntArray {
  val w = bmp.width
  val h = bmp.height
  val px = IntArray(w * h)
  bmp.getPixels(px, 0, w, 0, 0, w, h)
  var left = w; var top = h; var right = -1; var bottom = -1
  for (y in 0 until h) for (x in 0 until w) {
    val c = px[y * w + x]
    if (((c shr 16) and 0xFF) < threshold || ((c shr 8) and 0xFF) < threshold || (c and 0xFF) < threshold) {
      if (x < left) left = x
      if (x > right) right = x
      if (y < top) top = y
      if (y > bottom) bottom = y
    }
  }
  if (right < 0) return intArrayOf(0, 0, w, h)
  return intArrayOf(left, top, right + 1, bottom + 1)
}

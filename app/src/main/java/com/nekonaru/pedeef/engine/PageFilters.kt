package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.ByteArrayOutputStream

/**
 * Rasterizing page filter: renders each page at the chosen width, applies a bitmap color operation,
 * and rebuilds the PDF with one image per page. Text becomes pixels, so this is for print/visual use,
 * not for searching or copying text.
 */
enum class PageFilter { GRAYSCALE, INVERT }

fun filterPages(bytes: ByteArray, cacheDir: java.io.File, filter: PageFilter, maxWidth: Int = 1600): ByteArray = withTempPdf(bytes, cacheDir, "filter") { tmp ->
  val out = PDDocument()
  try {
    android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
      PdfRenderer(fd).use { r ->
        for (i in 0 until r.pageCount) {
          r.openPage(i).use { page ->
            val scale = minOf(maxWidth.toFloat() / maxOf(1, page.width), 3f)
            val w = maxOf(1, (page.width * scale).toInt())
            val h = maxOf(1, (page.height * scale).toInt())
            val shrink = minOf(1f, 3000f / maxOf(w, h).toFloat())
            val bw = maxOf(1, (w * shrink).toInt())
            val bh = maxOf(1, (h * shrink).toInt())
            val source = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            source.eraseColor(android.graphics.Color.WHITE)
            page.render(source, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val target = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            Canvas(target).drawBitmap(source, 0f, 0f, filterPaint(filter))
            source.recycle()
            val pdfPage = PDPage(PDRectangle(page.width.toFloat(), page.height.toFloat()))
            out.addPage(pdfPage)
            val img = LosslessFactory.createFromImage(out, target)
            PDPageContentStream(out, pdfPage).use { cs -> cs.drawImage(img, 0f, 0f, pdfPage.mediaBox.width, pdfPage.mediaBox.height) }
            target.recycle()
          }
        }
      }
    }
    ByteArrayOutputStream().use { out.save(it); it.toByteArray() }
  } finally {
    out.close()
  }
}

private fun filterPaint(filter: PageFilter): Paint = Paint().apply {
  colorFilter = ColorMatrixColorFilter(
    when (filter) {
      PageFilter.GRAYSCALE -> ColorMatrix().apply { setSaturation(0f) }
      PageFilter.INVERT -> ColorMatrix(
        floatArrayOf(
          -1f, 0f, 0f, 0f, 255f,
          0f, -1f, 0f, 0f, 255f,
          0f, 0f, -1f, 0f, 255f,
          0f, 0f, 0f, 1f, 0f,
        )
      )
    }
  )
}

package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument

/** Reverses the page order of a PDF. */
fun reversePages(bytes: ByteArray): ByteArray = loadPdf(bytes).use { src ->
  PDDocument().use { out ->
    for (i in src.pages.count - 1 downTo 0) out.adopt(src.getPage(i))
    out.bytes()
  }
}

/**
 * Removes fully blank pages (rendered as all-white at low resolution) and returns the cleaned PDF
 * plus a text report of the removed page numbers. Capped so a huge scan job stays bounded.
 */
fun removeBlankPages(bytes: ByteArray, cacheDir: java.io.File, maxPages: Int = 400): List<Output> = withTempPdf(bytes, cacheDir, "blank") { tmp ->
  val blanks = ArrayList<Int>()
  var total = 0
  android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
    PdfRenderer(fd).use { r ->
      total = r.pageCount
      for (i in 0 until minOf(total, maxPages)) {
        r.openPage(i).use { if (isBlankPage(it)) blanks.add(i + 1) }
      }
    }
  }
  if (blanks.isEmpty()) fail("No blank pages found.")

  val cleaned = loadPdf(bytes).use { src ->
    PDDocument().use { out ->
      for (i in 0 until src.pages.count) if ((i + 1) !in blanks.toSet()) out.adopt(src.getPage(i))
      out.bytes()
    }
  }
  val report = StringBuilder()
    .append("Pedeef blank page removal\n")
    .append("Pages before: $total\n")
    .append("Pages removed: ${blanks.size}\n")
    .append("Removed page numbers: ${blanks.joinToString(", ")}\n")
    .append("Pages after: ${total - blanks.size}\n")
    .toString().toByteArray(Charsets.UTF_8)
  listOf(Output("no-blank-pages", cleaned), Output("blank-pages-removed", report, ext = "txt"))
}

private const val BLANK_WIDTH = 150

internal fun isBlankPage(page: PdfRenderer.Page): Boolean {
  val scale = BLANK_WIDTH.toFloat() / maxOf(1, page.width)
  val bmp = Bitmap.createBitmap(BLANK_WIDTH, maxOf(1, minOf(3200, (page.height * scale).toInt())), Bitmap.Config.ARGB_8888)
  bmp.eraseColor(android.graphics.Color.WHITE)
  page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
  return try {
    val px = IntArray(bmp.width * bmp.height)
    bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
    px.all { c ->
      ((c shr 16) and 0xFF) >= 0xF8 && ((c shr 8) and 0xFF) >= 0xF8 && (c and 0xFF) >= 0xF8
    }
  } finally {
    bmp.recycle()
  }
}

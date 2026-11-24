package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import java.security.MessageDigest

/**
 * Renders pages at low res and hashes each bitmap; groups pages with visually identical pixels.
 * Bounded to [maxPages] pages so a huge document does not take minutes to scan.
 */
fun findDuplicatePages(name: String, bytes: ByteArray, cacheDir: java.io.File, maxPages: Int = 400): ByteArray = withTempPdf(bytes, cacheDir, "dup") { tmp ->
  android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
    PdfRenderer(fd).use { r ->
      val sb = StringBuilder()
      sb.append("Pedeef duplicate pages\n")
      sb.append("File: $name\n")
      val n = minOf(r.pageCount, maxPages)
      sb.append("Pages: ${r.pageCount}${if (n < r.pageCount) " (analyzed $n)" else ""}\n\n")
      val byHash = LinkedHashMap<String, MutableList<Int>>()
      for (i in 0 until n) {
        val hash = runCatching { r.openPage(i).use { hashPage(it) } }.getOrNull() ?: "render-failed-$i"
        byHash.getOrPut(hash) { mutableListOf() }.add(i + 1)
      }
      val groups = byHash.values.filter { it.size > 1 }
      if (groups.isEmpty()) sb.append("No duplicate pages found.")
      else {
        sb.append("Duplicate groups: ${groups.size}\n")
        groups.forEachIndexed { gi, g ->
          sb.append("Group ${gi + 1} (${g.size} pages): ${g.joinToString(", ")}\n")
        }
        sb.append("Pages affected: ${groups.sumOf { it.size }}")
      }
      sb.append("\n")
      Output("duplicates", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
    }
  }
}

private const val DUP_WIDTH = 150

/** MD5 hex of the bitmap's pixel bytes. */
private fun hashPage(page: PdfRenderer.Page): String {
  val scale = DUP_WIDTH.toFloat() / page.width
  val bmp = Bitmap.createBitmap(DUP_WIDTH, maxOf(1, (page.height * scale).toInt()), Bitmap.Config.ARGB_8888)
  page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
  try {
    val buf = java.nio.ByteBuffer.allocate(bmp.byteCount)
    bmp.copyPixelsToBuffer(buf)
    val md = MessageDigest.getInstance("MD5")
    return md.digest(buf.array()).joinToString("") { "%02x".format(it.toInt() and 0xFF) }
  } finally {
    bmp.recycle()
  }
}
package com.nekonaru.pedeef.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.ByteArrayOutputStream

/** Threshold-renders every page to black and white, rebuilding the PDF as images. */
fun blackWhitePages(bytes: ByteArray, cacheDir: java.io.File, threshold: Int = 160, maxWidth: Int = 1500): ByteArray = withTempPdf(bytes, cacheDir, "bw") { tmp ->
  val out = PDDocument()
  try {
    android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
      PdfRenderer(fd).use { r ->
        for (i in 0 until r.pageCount) {
          r.openPage(i).use { page ->
            val scale = minOf(maxWidth.toFloat() / maxOf(1, page.width), 3f)
            val w = maxOf(1, (page.width * scale).toInt())
            val h = maxOf(1, (page.height * scale).toInt())
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val px = IntArray(w * h)
            bmp.getPixels(px, 0, w, 0, 0, w, h)
            for (k in px.indices) {
              val c = px[k]
              val lum = (((c shr 16) and 0xFF) * 299 + ((c shr 8) and 0xFF) * 587 + (c and 0xFF) * 114) / 1000
              px[k] = if (lum >= threshold) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            }
            bmp.setPixels(px, 0, w, 0, 0, w, h)
            val pdfPage = PDPage(PDRectangle(page.width.toFloat(), page.height.toFloat())).also { out.addPage(it) }
            val img = LosslessFactory.createFromImage(out, bmp)
            PDPageContentStream(out, pdfPage).use { cs -> cs.drawImage(img, 0f, 0f, pdfPage.mediaBox.width, pdfPage.mediaBox.height) }
            bmp.recycle()
          }
        }
      }
    }
    ByteArrayOutputStream().use { out.save(it); it.toByteArray() }
  } finally {
    out.close()
  }
}

/** Reports which pages are fully blank. */
fun blankPageReport(bytes: ByteArray, cacheDir: java.io.File): ByteArray = withTempPdf(bytes, cacheDir, "blankreport") { tmp ->
  val sb = StringBuilder("Pedeef blank page report\n")
  var count = 0
  android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
    PdfRenderer(fd).use { r ->
      for (i in 0 until r.pageCount) {
        r.openPage(i).use {
          if (isBlankPage(it)) { count++; sb.append("Page ${i + 1} is blank\n") }
        }
      }
    }
  }
  sb.append(if (count == 0) "No blank pages found." else "Blank pages: $count")
  Output("blank-report", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

/** Stacks all PDF pages vertically into one PNG. */
fun pdfToLongImage(bytes: ByteArray, cacheDir: java.io.File, width: Int = 1000): ByteArray = withTempPdf(bytes, cacheDir, "longimg") { tmp ->
  val parts = ArrayList<Bitmap>()
  try {
    android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
      PdfRenderer(fd).use { r ->
        for (i in 0 until r.pageCount) {
          r.openPage(i).use { page ->
            val scale = minOf(width.toFloat() / maxOf(1, page.width), 3f)
            val w = maxOf(1, (page.width * scale).toInt())
            val h = maxOf(1, (page.height * scale).toInt())
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            bmp.eraseColor(Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            parts.add(bmp)
          }
        }
      }
    }
    val w = parts.maxOf { it.width }
    val h = parts.sumOf { it.height }
    if (h > 16000 || w > 16000) fail("Document is too tall or wide for one image. Lower the resolution or split it.")
    val result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
    val canvas = Canvas(result)
    var y = 0
    for (bmp in parts) {
      canvas.drawBitmap(bmp, 0f, y.toFloat(), null)
      y += bmp.height
    }
    ByteArrayOutputStream().use { result.compress(Bitmap.CompressFormat.PNG, 100, it); it.toByteArray() }.also { result.recycle() }
  } finally {
    parts.forEach { it.recycle() }
  }
}

/** Stacks several images vertically into one PNG. */
fun imagesToLongImage(images: List<Named>, width: Int = 1000): ByteArray {
  val parts = ArrayList<Bitmap>()
  try {
    for (f in images) {
      val raw = runCatching { BitmapFactory.decodeByteArray(f.bytes, 0, f.bytes.size) }.getOrNull() ?: continue
      val scale = minOf(width.toFloat() / raw.width, 3f)
      val bmp = Bitmap.createBitmap(maxOf(1, (raw.width * scale).toInt()), maxOf(1, (raw.height * scale).toInt()), Bitmap.Config.RGB_565)
      val c = Canvas(bmp)
      c.drawBitmap(raw, null, android.graphics.RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat()), null)
      raw.recycle()
      parts.add(bmp)
    }
    if (parts.isEmpty()) fail("No readable images found.")
    val w = parts.maxOf { it.width }
    val h = parts.sumOf { it.height }
    if (h > 16000 || w > 16000) fail("Images are too large for one canvas.")
    val result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
    val canvas = Canvas(result)
    var y = 0
    for (bmp in parts) {
      canvas.drawBitmap(bmp, 0f, y.toFloat(), null)
      y += bmp.height
    }
    return ByteArrayOutputStream().use { result.compress(Bitmap.CompressFormat.PNG, 100, it); it.toByteArray() }.also { result.recycle() }
  } finally {
    parts.forEach { it.recycle() }
  }
}

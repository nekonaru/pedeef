package com.nekonaru.pedeef.ui.kit

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.nekonaru.pedeef.engine.withTempPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Page sizes of a PDF held in memory, as [w, h] in points (no rendering). */
suspend fun pageSizesOf(bytes: ByteArray, cacheDir: File): List<Pair<Int, Int>> = withContext(Dispatchers.IO) {
  withTempPdf(bytes, cacheDir, "sizes") { tmp ->
    val pfd = android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
    pfd.use { fd ->
      android.graphics.pdf.PdfRenderer(fd).use { r ->
        (0 until r.pageCount).map { i -> r.openPage(i).use { it.width to it.height } }
      }
    }
  }
}

/** Renders page `index` of a PDF in memory at `widthPx`, or null if the page cannot be rendered. */
suspend fun renderPage(bytes: ByteArray, index: Int, widthPx: Int, cacheDir: File): Bitmap? = withContext(Dispatchers.IO) {
  runCatching {
    withTempPdf(bytes, cacheDir, "page") { tmp ->
      val pfd = android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
      pfd.use { fd ->
        android.graphics.pdf.PdfRenderer(fd).use { r ->
          r.openPage(index).use { p ->
            val k = minOf(widthPx.toFloat() / p.width, 3f)
            val w0 = maxOf(1, (p.width * k).toInt())
            val h0 = maxOf(1, (p.height * k).toInt())
            val shrink = minOf(1f, 3200f / maxOf(w0, h0))
            val bmp = Bitmap.createBitmap(maxOf(1, (w0 * shrink).toInt()), maxOf(1, (h0 * shrink).toInt()), Bitmap.Config.ARGB_8888)
            bmp.eraseColor(android.graphics.Color.WHITE)
            p.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bmp
          }
        }
      }
    }
  }.getOrNull()
}

@Composable
fun rememberPageSizes(bytes: ByteArray?, cacheDir: File): List<Pair<Int, Int>>? {
  val state = produceState<List<Pair<Int, Int>>?>(initialValue = null, bytes) {
    value = null
    val b = bytes ?: return@produceState
    value = runCatching { pageSizesOf(b, cacheDir) }.getOrNull()
  }
  return state.value
}

@Composable
fun rememberPageImage(bytes: ByteArray?, index: Int, widthPx: Int, cacheDir: File): ImageBitmap? {
  val state = produceState<ImageBitmap?>(initialValue = null, bytes, index, widthPx) {
    value = null
    val b = bytes ?: return@produceState
    if (index < 0) return@produceState
    value = renderPage(b, index, widthPx, cacheDir)?.asImageBitmap()
  }
  return state.value
}

@Composable
fun rememberPageCount(bytes: ByteArray?, cacheDir: File): Int? {
  val state = produceState<Int?>(initialValue = null, bytes) {
    value = null
    val b = bytes ?: return@produceState
    value = runCatching { pageSizesOf(b, cacheDir).size }.getOrNull()
  }
  return state.value
}

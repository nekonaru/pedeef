package com.nekonaru.pedeef.ui.kit

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private val sessionCounter = AtomicInteger(0)

/**
 * One temp file and one PdfRenderer per document. Pages and thumbnails share the renderer and a
 * bounded bitmap cache, so scrolling does not rewrite the whole PDF for every page.
 */
class PdfRenderSession(bytes: ByteArray, cacheDir: File, tag: String) : AutoCloseable {
  private val temp: File = File(cacheDir, "$tag-${System.currentTimeMillis()}-${sessionCounter.incrementAndGet()}.pdf")
  private val fd: ParcelFileDescriptor
  private val renderer: PdfRenderer
  private val lock = Mutex()

  init {
    var opened: ParcelFileDescriptor? = null
    try {
      temp.writeBytes(bytes)
      opened = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
      fd = opened
      renderer = PdfRenderer(fd)
    } catch (t: Throwable) {
      runCatching { opened?.close() }
      runCatching { temp.delete() }
      throw t
    }
  }

  private val cache = object : LruCache<String, Bitmap>(32 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
  }

  /** Page sizes in points, without rendering any page. */
  suspend fun pageSizes(): List<Pair<Int, Int>> = withContext(Dispatchers.IO) {
    lock.withLock {
      (0 until renderer.pageCount).map { i ->
        renderer.openPage(i).use { page -> page.width to page.height }
      }
    }
  }

  /**
   * Renders one page. Widths are bucketed so a small change in zoom reuses the cached bitmap.
   * Thumbnails use RGB_565 to halve memory; full pages keep ARGB_8888 for text clarity.
   */
  suspend fun render(index: Int, widthPx: Int): Bitmap? = withContext(Dispatchers.IO) {
    if (index < 0 || index >= renderer.pageCount) return@withContext null
    val bucket = RenderMath.widthBucket(widthPx)
    val key = "$index:$bucket"
    cache.get(key)?.let { return@withContext it }
    lock.withLock {
      cache.get(key)?.let { return@withLock it }
      renderer.openPage(index).use { page ->
        val k = minOf(bucket.toFloat() / page.width, 3f)
        val w0 = maxOf(1, (page.width * k).toInt())
        val h0 = maxOf(1, (page.height * k).toInt())
        val shrink = minOf(1f, 3200f / maxOf(w0, h0))
        val w = maxOf(1, (w0 * shrink).toInt())
        val h = maxOf(1, (h0 * shrink).toInt())
        val config = if (bucket == RenderMath.ThumbnailWidth) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(w, h, config)
        bmp.eraseColor(Color.WHITE)
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        cache.put(key, bmp)
        bmp
      }
    }
  }

  /** Blocks until any in-flight render finishes, then frees native resources. */
  override fun close() {
    runBlocking {
      lock.withLock {
        cache.evictAll()
        runCatching { renderer.close() }
        runCatching { fd.close() }
        runCatching { temp.delete() }
      }
    }
  }
}

/** Remembers one render session for the given bytes, built off the main thread and closed on dispose. */
@Composable
fun rememberPdfSession(bytes: ByteArray?, cacheDir: File, tag: String): PdfRenderSession? {
  var session by remember { mutableStateOf<PdfRenderSession?>(null) }
  val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
  DisposableEffect(bytes, tag) {
    val job = scope.launch {
      val created = bytes?.let { runCatching { PdfRenderSession(it, cacheDir, tag) }.getOrNull() }
      withContext(Dispatchers.Main) { session = created }
    }
    onDispose {
      job.cancel()
      val closing = session
      session = null
      if (closing != null) scope.launch { closing.close() }
    }
  }
  return session
}

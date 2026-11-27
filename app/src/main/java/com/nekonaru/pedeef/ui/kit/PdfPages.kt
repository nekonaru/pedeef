package com.nekonaru.pedeef.ui.kit

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Page sizes of a PDF held in memory, as [w, h] in points. */
suspend fun pageSizesOf(bytes: ByteArray, cacheDir: File): List<Pair<Int, Int>> = withContext(Dispatchers.IO) {
  PdfRenderSession(bytes, cacheDir, "sizes").use { it.pageSizes() }
}

/** Renders page `index`, or null when the page cannot be rendered. */
suspend fun renderPage(bytes: ByteArray, index: Int, widthPx: Int, cacheDir: File): Bitmap? = withContext(Dispatchers.IO) {
  runCatching { PdfRenderSession(bytes, cacheDir, "page").use { it.render(index, widthPx) } }.getOrNull()
}

@Composable
fun rememberPageSizes(bytes: ByteArray?, cacheDir: File): List<Pair<Int, Int>>? {
  val session = rememberPdfSession(bytes, cacheDir, "sizes")
  val state = produceState<List<Pair<Int, Int>>?>(initialValue = null, session) {
    value = null
    val s = session ?: return@produceState
    value = runCatching { s.pageSizes() }.getOrNull()
  }
  return state.value
}

@Composable
fun rememberPageImage(bytes: ByteArray?, index: Int, widthPx: Int, cacheDir: File): ImageBitmap? {
  val session = rememberPdfSession(bytes, cacheDir, "editor")
  val state = produceState<ImageBitmap?>(initialValue = null, session, index, widthPx) {
    value = null
    val s = session ?: return@produceState
    if (index < 0) return@produceState
    value = s.render(index, widthPx)?.asImageBitmap()
  }
  return state.value
}

@Composable
fun rememberPageCount(bytes: ByteArray?, cacheDir: File): Int? {
  val session = rememberPdfSession(bytes, cacheDir, "count")
  val state = produceState<Int?>(initialValue = null, session) {
    value = null
    val s = session ?: return@produceState
    value = runCatching { s.pageSizes().size }.getOrNull()
  }
  return state.value
}

/** Session based variants let one editor share a single temp file and renderer. */
@Composable
fun rememberSessionPageSizes(session: PdfRenderSession?): List<Pair<Int, Int>>? {
  val state = produceState<List<Pair<Int, Int>>?>(initialValue = null, session) {
    value = null
    val s = session ?: return@produceState
    value = runCatching { s.pageSizes() }.getOrNull()
  }
  return state.value
}

@Composable
fun rememberSessionPageCount(session: PdfRenderSession?): Int? {
  val state = produceState<Int?>(initialValue = null, session) {
    value = null
    val s = session ?: return@produceState
    value = runCatching { s.pageSizes().size }.getOrNull()
  }
  return state.value
}

@Composable
fun rememberSessionPageImage(session: PdfRenderSession?, index: Int, widthPx: Int): ImageBitmap? {
  val state = produceState<ImageBitmap?>(initialValue = null, session, index, widthPx) {
    value = null
    val s = session ?: return@produceState
    if (index < 0) return@produceState
    value = s.render(index, widthPx)?.asImageBitmap()
  }
  return state.value
}

package com.nekonaru.pedeef.engine

import android.graphics.RectF
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import java.io.File

/** Page indices containing the query plus highlight boxes for each, as fractions of the page. */
data class SearchResult(val pages: List<Int>, val boxes: Map<Int, List<RectF>>)

private class LineHighlight(val needle: String) : PDFTextStripper() {
  val boxes = ArrayList<RectF>()
  init { sortByPosition = true }

  private fun norm(s: String) = s.lowercase().replace(Regex("\\s+"), "")

  override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
    if (text == null || textPositions.isNullOrEmpty()) return
    if (!norm(text).contains(norm(needle))) return
    val x1 = textPositions.minOf { it.xDirAdj }
    val x2 = textPositions.maxOf { it.xDirAdj + it.widthDirAdj }
    val y1 = textPositions.minOf { it.yDirAdj }
    val y2 = textPositions.maxOf { it.yDirAdj + it.heightDir }
    if (x2 > x1 && y2 > y1) boxes.add(RectF(x1, y1, x2, y2))
  }
}

/** Searches every page for the needle and returns page indices plus line highlight boxes. */
fun searchPdf(bytes: ByteArray, query: String, cacheDir: File): SearchResult = withTempPdf(bytes, cacheDir, "searchhl") { tmp ->
  val needle = norm(query)
  if (needle.isEmpty()) return@withTempPdf SearchResult(emptyList(), emptyMap())
  PDDocument.load(tmp).use { doc ->
    val pages = ArrayList<Int>()
    val boxes = HashMap<Int, List<RectF>>()
    val stripper = PDFTextStripper()
    for (i in 0 until doc.numberOfPages) {
      stripper.startPage = i + 1
      stripper.endPage = i + 1
      val found = runCatching { norm(stripper.getText(doc)).contains(needle) }.getOrDefault(false)
      if (!found) continue
      pages.add(i)
      val page = doc.getPage(i)
      val rot = ((page.rotation % 360) + 360) % 360
      if (rot == 0) {
        val cb = page.cropBox
        val collector = LineHighlight(query)
        collector.startPage = i + 1
        collector.endPage = i + 1
        runCatching { collector.getText(doc) }
        boxes[i] = collector.boxes.map {
          RectF(it.left / cb.width, it.top / cb.height, it.right / cb.width, it.bottom / cb.height)
        }
      } else {
        // Highlight boxes from PDFBox are stored-space, y measured from page top. Flip y before mapping
        // back to the displayed page, or rotated pages get mirrored boxes.
        val cb = page.cropBox
        val collector = LineHighlight(query)
        collector.startPage = i + 1
        collector.endPage = i + 1
        runCatching { collector.getText(doc) }
        boxes[i] = collector.boxes.map { b ->
          val f = listOf(b.left / cb.width, b.top / cb.height, (b.right - b.left) / cb.width, (b.bottom - b.top) / cb.height)
          val f2 = listOf(f[0], 1 - f[1] - f[3], f[2], f[3])
          val u = unrotate(f2, rot)
          RectF(u[0], u[1], u[0] + u[2], u[1] + u[3])
        }
      }
    }
    SearchResult(pages, boxes)
  }
}

private fun norm(s: String) = s.lowercase().replace(Regex("\\s+"), "")

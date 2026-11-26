package com.nekonaru.pedeef.engine

/** Reports every distinct page size of a PDF in points, millimetres and inches. */
fun pageSizeReport(name: String, bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val seen = LinkedHashMap<Pair<Int, Int>, MutableList<Int>>()
  for (i in 0 until doc.numberOfPages) {
    val box = doc.getPage(i).cropBox
    val key = (box.width * 10).toInt() to (box.height * 10).toInt()
    seen.getOrPut(key) { mutableListOf() }.add(i + 1)
  }
  val sb = StringBuilder()
  sb.append("Pedeef page sizes: $name\n")
  sb.append("Pages: ${doc.numberOfPages}\n\n")
  seen.forEach { (tenth, pages) ->
    val wPt = tenth.first / 10f
    val hPt = tenth.second / 10f
    sb.append("Page ${pages.first()}${if (pages.size > 1) " and ${pages.size - 1} more" else ""}:\n")
    sb.append(String.format(java.util.Locale.US, "  %.1f x %.1f pt\n", wPt, hPt))
    sb.append(String.format(java.util.Locale.US, "  %.1f x %.1f mm\n", wPt * 25.4 / 72, hPt * 25.4 / 72))
    sb.append(String.format(java.util.Locale.US, "  %.2f x %.2f in\n", wPt / 72, hPt / 72))
  }
  Output("page-sizes", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

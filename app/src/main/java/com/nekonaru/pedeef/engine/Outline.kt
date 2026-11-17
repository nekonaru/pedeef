package com.nekonaru.pedeef.engine

/** PDF outline (bookmarks) as pairs of title and zero-based page index, sorted by page. */
fun readOutline(bytes: ByteArray): List<Pair<String, Int>> = loadPdf(bytes).use { src ->
  val marks = ArrayList<Pair<String, Int>>()
  var item = src.documentCatalog.documentOutline?.firstChild
  var guard = 0
  while (item != null && guard++ < 10000) {
    val page = runCatching { item.findDestinationPage(src) }.getOrNull()
    val i = page?.let { src.pages.indexOf(it) } ?: -1
    if (i >= 0) marks.add((item.title?.takeIf { it.isNotBlank() } ?: "Section") to i)
    item = item.nextSibling
  }
  marks.sortBy { it.second }
  marks
}

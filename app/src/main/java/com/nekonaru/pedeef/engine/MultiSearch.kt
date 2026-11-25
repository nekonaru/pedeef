package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.text.PDFTextStripper

/** Case-insensitive text search across several PDFs, returned as a plain-text match report. */
fun searchAcrossFiles(files: List<Named>, query: String): ByteArray {
  val q = query.trim()
  if (q.isEmpty()) fail("Enter text to search for.")
  val sb = StringBuilder()
  sb.append("Pedeef search: \"${q}\"\n\n")
  var total = 0
  for (file in files) {
    val hits = ArrayList<Int>()
    loadPdf(file.bytes).use { doc ->
      val stripper = PDFTextStripper()
      for (i in 1..doc.numberOfPages) {
        val text = runCatching {
          stripper.startPage = i; stripper.endPage = i; stripper.getText(doc)
        }.getOrNull() ?: continue
        if (text.contains(q, ignoreCase = true)) hits.add(i)
      }
    }
    if (hits.isNotEmpty()) {
      total += hits.size
      sb.append("${file.name}: ${hits.size} page(s) - ${hits.joinToString(", ")}\n")
    }
  }
  if (total == 0) sb.append("No matches found.")
  else sb.append("\nTotal pages with matches: $total")
  return Output("search", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

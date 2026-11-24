package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

/**
 * Adds a document outline (bookmarks) from lines like "1=Introduction, 5=Chapter One".
 * Page numbers are 1-based; invalid or out-of-range lines are skipped.
 */
fun setBookmarks(bytes: ByteArray, spec: String): ByteArray = loadPdf(bytes).use { doc ->
  val outline = PDDocumentOutline()
  var added = 0
  for (raw in spec.split(',', '\n', ';')) {
    val line = raw.trim()
    if (line.isEmpty()) continue
    val eq = line.indexOf('=')
    if (eq < 1) continue
    val page = line.substring(0, eq).trim().toIntOrNull() ?: continue
    val title = line.substring(eq + 1).trim()
    if (title.isEmpty() || page < 1 || page > doc.numberOfPages) continue
    val item = PDOutlineItem().apply {
      setTitle(title)
      setDestination(doc.getPage(page - 1))
    }
    outline.addLast(item)
    added++
  }
  if (added == 0) fail("No valid bookmarks. Use lines like: 1=Introduction, 5=Chapter One")
  doc.documentCatalog.setDocumentOutline(outline)
  doc.bytes()
}

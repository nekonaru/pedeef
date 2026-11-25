package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font

/**
 * Stamps a short header and/or footer line on every page using a standard PDF font.
 * Text is normalized with winAnsi and capped in length so it cannot run off the page.
 */
fun addHeaderFooter(bytes: ByteArray, header: String, footer: String, fontPt: Int = 10): ByteArray = loadPdf(bytes).use { doc ->
  val h = winAnsi(header.trim()).take(120)
  val f = winAnsi(footer.trim()).take(120)
  if (h.isEmpty() && f.isEmpty()) fail("Enter a header or footer.")
  val font = PDType1Font.HELVETICA
  for (page in doc.pages) {
    val w = page.mediaBox.width
    val height = page.mediaBox.height
    if (h.isEmpty() && f.isEmpty()) continue
    PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
      cs.beginText()
      cs.setFont(font, fontPt.toFloat())
      if (h.isNotEmpty()) {
        val x = (w - runCatching { font.getStringWidth(h) * fontPt / 1000f }.getOrDefault(h.length * fontPt * 0.5f)) / 2
        cs.newLineAtOffset(x.coerceAtLeast(12f), height - 24f)
        cs.showText(h)
      }
      if (f.isNotEmpty()) {
        val x = (w - runCatching { font.getStringWidth(f) * fontPt / 1000f }.getOrDefault(f.length * fontPt * 0.5f)) / 2
        cs.newLineAtOffset(x.coerceAtLeast(12f) - (if (h.isNotEmpty()) x.coerceAtLeast(12f) else 0f), -height + 36f)
        cs.showText(f)
      }
      cs.endText()
    }
  }
  doc.bytes()
}

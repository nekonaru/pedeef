package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font

/**
 * Stamps a short header and/or footer line on every page using a standard PDF font.
 * Text is normalized with winAnsi and each line is truncated to fit the page width.
 */
fun addHeaderFooter(bytes: ByteArray, header: String, footer: String, fontPt: Int = 10): ByteArray = loadPdf(bytes).use { doc ->
  val h = fitText(winAnsi(header.trim()), fontPt, 500f) // generous pre-cap; exact per-page fit below
  val f = fitText(winAnsi(footer.trim()), fontPt, 500f)
  if (h.isEmpty() && f.isEmpty()) fail("Enter a header or footer.")
  val font = PDType1Font.HELVETICA
  for (page in doc.pages) {
    val box = page.mediaBox
    val avail = box.width - 24f
    if (avail <= 10f) continue
    val hLine = fitText(h, fontPt, avail)
    val fLine = fitText(f, fontPt, avail)
    PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
      cs.beginText()
      cs.setFont(font, fontPt.toFloat())
      if (hLine.isNotEmpty()) {
        val x = box.lowerLeftX + (box.width - textWidth(hLine, fontPt)) / 2f
        // Absolute placement: no dependency on a previous Td/Tm operation.
        cs.setTextMatrix(com.tom_roush.pdfbox.util.Matrix.getTranslateInstance(x.coerceAtLeast(box.lowerLeftX + 12f), box.lowerLeftY + box.height - 24f))
        cs.showText(hLine)
      }
      if (fLine.isNotEmpty()) {
        val x = box.lowerLeftX + (box.width - textWidth(fLine, fontPt)) / 2f
        cs.setTextMatrix(com.tom_roush.pdfbox.util.Matrix.getTranslateInstance(x.coerceAtLeast(box.lowerLeftX + 12f), box.lowerLeftY + 14f))
        cs.showText(fLine)
      }
      cs.endText()
    }
  }
  doc.bytes()
}

private fun textWidth(s: String, fontPt: Int): Float =
  runCatching { PDType1Font.HELVETICA.getStringWidth(s) * fontPt / 1000f }.getOrDefault(s.length * fontPt * 0.5f)

/** Truncates [s] until its rendered width fits [maxWidth]. */
private fun fitText(s: String, fontPt: Int, maxWidth: Float): String {
  if (s.isEmpty()) return s
  var out = s.take(120)
  while (out.length > 1 && textWidth(out, fontPt) > maxWidth) out = out.dropLast(1)
  return out
}

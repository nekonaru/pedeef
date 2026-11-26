package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.font.PDType1Font

/**
 * Stamps a short header and/or footer line on every page using a standard PDF font.
 * Rotation-aware via the same placement helper used by page numbers.
 */
fun addHeaderFooter(bytes: ByteArray, header: String, footer: String, fontPt: Int = 10): ByteArray = loadPdf(bytes).use { doc ->
  val h = winAnsi(header.trim()).take(120)
  val f = winAnsi(footer.trim()).take(120)
  if (h.isEmpty() && f.isEmpty()) fail("Enter a header or footer.")
  val font = PDType1Font.HELVETICA
  for (page in doc.pages) {
    val box = page.cropBox
    val rot = page.rot
    val dw = if (rot % 180 != 0) box.height else box.width
    val dh = if (rot % 180 != 0) box.width else box.height
    val hLine = fitText(h, fontPt, dw - 24f)
    val fLine = fitText(f, fontPt, dw - 24f)
    if (hLine.isEmpty() && fLine.isEmpty()) continue
    doc.draw(page) {
      if (hLine.isNotEmpty()) {
        val x = (dw - textWidth(hLine, fontPt)) / 2f
        val at = placeOnPage(listOf(x / dw, (dh - 24f - fontPt) / dh, 0f, fontPt / dh), rot, box.width, box.height)
        putText(font, fontPt.toFloat(), hLine, box.lowerLeftX + at.x, box.lowerLeftY + at.y, rot.toFloat())
      }
      if (fLine.isNotEmpty()) {
        val x = (dw - textWidth(fLine, fontPt)) / 2f
        val at = placeOnPage(listOf(x / dw, (dh - 14f - fontPt) / dh, 0f, fontPt / dh), rot, box.width, box.height)
        putText(font, fontPt.toFloat(), fLine, box.lowerLeftX + at.x, box.lowerLeftY + at.y, rot.toFloat())
      }
    }
  }
  doc.bytes()
}

private fun textWidth(s: String, fontPt: Int): Float =
  runCatching { PDType1Font.HELVETICA.getStringWidth(s) * fontPt / 1000f }.getOrDefault(s.length * fontPt * 0.5f)

private fun fitText(s: String, fontPt: Int, maxWidth: Float): String {
  if (s.isEmpty()) return s
  var out = s
  while (out.length > 1 && textWidth(out, fontPt) > maxWidth) out = out.dropLast(1)
  return out
}

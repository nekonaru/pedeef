package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayOutputStream

/** Wraps plain text into a new PDF at the chosen page size and font size. */
fun textToPdf(text: String, size: String, fontPt: Int, marginPt: Float = 48f): ByteArray {
  if (text.isBlank()) fail("Enter some text.")
  val lineHeight = (fontPt * 1.45f).coerceAtLeast(8f)
  val lines = text.replace("\r", "").split('\n').flatMap { raw ->
    if (raw.isBlank()) return@flatMap listOf("")
    wrapLine(raw.trim(), size, fontPt, marginPt)
  }
  val doc = PDDocument()
  try {
    val (w, h) = SIZES[size] ?: fail("Unknown page size.")
    val font = PDType1Font.HELVETICA
    var page: PDPage? = null
    var cs: PDPageContentStream? = null
    var y = 0f
    var firstOnPage = true
    for (line in lines) {
      if (page == null || y < marginPt) {
        cs?.endText()
        cs?.close()
        page = PDPage(PDRectangle(w, h)).also { doc.addPage(it) }
        cs = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).also {
          it.beginText()
          it.setFont(font, fontPt.toFloat())
        }
        y = h - marginPt - lineHeight
        firstOnPage = true
      }
      // beginText() leaves the text matrix at identity; the first line must be positioned absolutely.
      if (firstOnPage) {
        cs!!.newLineAtOffset(marginPt, y)
        firstOnPage = false
      } else {
        cs!!.newLineAtOffset(0f, -lineHeight)
      }
      cs.showText(line)
      y -= lineHeight
    }
    cs?.endText()
    cs?.close()
    return ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
  } finally {
    runCatching { doc.close() }
  }
}

private fun wrapLine(line: String, size: String, fontPt: Int, marginPt: Float): List<String> {
  val (pw, _) = SIZES[size] ?: fail("Unknown page size.")
  val maxWidth = pw - 2 * marginPt
  val words = line.split(Regex("\\s+")).filter { it.isNotEmpty() }
  if (words.isEmpty()) return listOf("")
  val result = ArrayList<String>()
  var current = StringBuilder()
  // Hard-split words wider than the page, otherwise they get clipped past the right margin.
  val pieces = ArrayList<String>()
  for (word in words) {
    if (textWidth(word, fontPt) <= maxWidth) {
      pieces.add(word)
    } else {
      var buf = StringBuilder()
      for (ch in word) {
        if (textWidth(buf.toString() + ch, fontPt) > maxWidth && buf.isNotEmpty()) {
          pieces.add(buf.toString())
          buf = StringBuilder()
        }
        buf.append(ch)
      }
      if (buf.isNotEmpty()) pieces.add(buf.toString())
    }
  }
  for (word in pieces) {
    val trial = if (current.isEmpty()) word else "$current $word"
    if (textWidth(trial, fontPt) <= maxWidth || current.isEmpty()) {
      current = StringBuilder(trial)
    } else {
      result.add(current.toString())
      current = StringBuilder(word)
    }
  }
  if (current.isNotEmpty()) result.add(current.toString())
  return result
}

private fun textWidth(s: String, fontPt: Int): Float =
  runCatching { PDType1Font.HELVETICA.getStringWidth(s) * fontPt / 1000f }.getOrDefault(s.length * fontPt * 0.5f)

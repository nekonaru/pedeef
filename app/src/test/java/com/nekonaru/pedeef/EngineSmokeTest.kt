package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.reversePages
import com.nekonaru.pedeef.engine.extractText
import com.nekonaru.pedeef.engine.merge
import com.nekonaru.pedeef.engine.Named
import com.nekonaru.pedeef.engine.parseRanges
import com.nekonaru.pedeef.engine.split
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

private fun blankPdf(pages: Int): ByteArray {
  PDDocument().use { doc ->
    repeat(pages) { doc.addPage(PDPage(PDRectangle(200f, 300f))) }
    return ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
  }
}

class EngineSmokeTest {
  @Test
  fun reversePagesFlipsOrder() {
    val src = PDDocument().use { doc ->
      doc.addPage(PDPage(PDRectangle(200f, 300f)))
      doc.addPage(PDPage(PDRectangle(400f, 300f)))
      doc.addPage(PDPage(PDRectangle(200f, 500f)))
      ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
    }
    PDDocument.load(reversePages(src)).use { doc ->
      assertEquals(3, doc.numberOfPages)
      assertEquals(200f, doc.getPage(0).mediaBox.width)
      assertEquals(500f, doc.getPage(0).mediaBox.height)
      assertEquals(400f, doc.getPage(1).mediaBox.width)
    }
  }

  @Test
  fun mergeCombinesDocuments() {
    val a = blankPdf(2)
    val b = blankPdf(3)
    val out = merge(listOf(Named("a.pdf", a), Named("b.pdf", b)))
    PDDocument.load(out).use { assertEquals(5, it.numberOfPages) }
  }

  @Test
  fun splitEveryPageProducesOneFilePerPage() {
    val out = split(blankPdf(3), "every", "")
    assertEquals(3, out.size)
    PDDocument.load(out[0].bytes).use { assertEquals(1, it.numberOfPages) }
  }

  @Test
  fun splitRangesProducesOneFilePerRange() {
    val out = split(blankPdf(6), "ranges", "1-2, 4-")
    assertEquals(2, out.size)
    PDDocument.load(out[0].bytes).use { assertEquals(2, it.numberOfPages) }
    PDDocument.load(out[1].bytes).use { assertEquals(3, it.numberOfPages) }
  }

  @Test
  fun splitRangesMatchesParseRanges() {
    assertEquals(listOf(listOf(0), listOf(2, 3)), parseRanges("1, 3-4", 6))
  }
}

private fun textPdf(lines: List<String>): ByteArray {
  com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
    val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle(200f, 300f))
    doc.addPage(page)
    com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { cs ->
      cs.beginText()
      // PDType1Font needs the AFM bundle that ships only on-device; embed the repo's Poppins instead.
      val fontFile = java.io.File("src/main/res/font/poppins_regular.ttf").takeIf { it.exists() } ?: java.io.File(System.getProperty("user.dir"), "app/src/main/res/font/poppins_regular.ttf")
      val font = com.tom_roush.pdfbox.pdmodel.font.PDType0Font.load(doc, fontFile)
      cs.setFont(font, 12f)
      cs.newLineAtOffset(20f, 270f)
      var firstLine = true
      for (line in lines) {
        if (!firstLine) cs.newLineAtOffset(0f, -16f)
        cs.showText(line)
        firstLine = false
      }
      cs.endText()
    }
    return java.io.ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
  }
}

class CompareSmokeTest {
  @Test
  fun identicalPdfsReportIdentical() {
    val a = textPdf(listOf("hello", "world"))
    val b = textPdf(listOf("hello", "world"))
    val report = String(com.nekonaru.pedeef.engine.comparePdfs("a.pdf", a, "b.pdf", b), Charsets.UTF_8)
    org.junit.Assert.assertTrue(report, report.contains("Page 1: identical"))
    org.junit.Assert.assertTrue(report, report.contains("Changed pages: 0"))
  }

  @Test
  fun changedLineIsReported() {
    val a = textPdf(listOf("hello", "world"))
    val b = textPdf(listOf("hello", "mars"))
    val report = String(com.nekonaru.pedeef.engine.comparePdfs("a.pdf", a, "b.pdf", b), Charsets.UTF_8)
    org.junit.Assert.assertTrue(report, report.contains("Page 1: 1 line(s) added, 1 line(s) removed"))
    org.junit.Assert.assertTrue(report, report.contains("- world"))
    org.junit.Assert.assertTrue(report, report.contains("+ mars"))
    org.junit.Assert.assertTrue(report, report.contains("Changed pages: 1"))
  }

  @Test
  fun missingPagesAreReported() {
    val a = textPdf(listOf("hello"))
    val b = com.nekonaru.pedeef.engine.merge(listOf(Named("a.pdf", textPdf(listOf("hello"))), Named("b.pdf", textPdf(listOf("world")))))
    val report = String(com.nekonaru.pedeef.engine.comparePdfs("a.pdf", a, "b.pdf", b), Charsets.UTF_8)
    org.junit.Assert.assertTrue(report, report.contains("Page 2: only in B"))
    org.junit.Assert.assertTrue(report, report.contains("Only in B: 1"))
  }
}

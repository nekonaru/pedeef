package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.lintPdf
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class LintSmokeTest {
  private fun pdfWithFont(): ByteArray {
    PDDocument().use { doc ->
      val page = PDPage(PDRectangle(200f, 300f))
      doc.addPage(page)
      val fontFile = File("src/main/res/font/poppins_regular.ttf").takeIf { it.exists() } ?: File(System.getProperty("user.dir"), "app/src/main/res/font/poppins_regular.ttf")
      PDPageContentStream(doc, page).use { cs ->
        cs.beginText()
        cs.setFont(PDType0Font.load(doc, fontFile), 12f)
        cs.newLineAtOffset(20f, 270f)
        cs.showText("hello")
        cs.endText()
      }
      return ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
    }
  }

  @Test
  fun lintReportsPagesVersionAndFonts() {
    val report = String(lintPdf("sample.pdf", pdfWithFont()), Charsets.UTF_8)
    assertTrue(report, report.contains("File: sample.pdf"))
    assertTrue(report, report.contains("Pages: 1"))
    assertTrue(report, report.contains("Encrypted: no"))
    assertTrue(report, report.contains("Fonts ("))
    assertTrue(report, !report.contains("could not be loaded"))
  }

  @Test
  fun lintAcceptsAndReportsEmptyPdfs() {
    PDDocument().use { doc ->
      doc.addPage(PDPage(PDRectangle(200f, 300f)))
      val bytes = ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
      val report = String(lintPdf("empty.pdf", bytes), Charsets.UTF_8)
      assertTrue(report, report.contains("Pages: 1"))
      assertTrue(report, report.contains("(none)"))
    }
  }
}

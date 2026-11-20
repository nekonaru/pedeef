package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.reportPdf
import com.nekonaru.pedeef.engine.stripMetadata
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

class ReportSmokeTest {
  private fun pdfWithDetails(): ByteArray {
    PDDocument().use { doc ->
      val page = PDPage(PDRectangle(200f, 300f))
      doc.addPage(page)
      doc.documentInformation.title = "Quarterly report"
      doc.documentInformation.author = "Pedeef tests"
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
  fun reportListsPagesAndMetadata() {
    val report = String(reportPdf("sample.pdf", pdfWithDetails()), Charsets.UTF_8)
    assertTrue(report, report.contains("File: sample.pdf"))
    assertTrue(report, report.contains("Pages: 1"))
    assertTrue(report, report.contains("Title: Quarterly report"))
    assertTrue(report, report.contains("Author: Pedeef tests"))
    assertTrue(report, report.contains("Metadata"))
  }

  @Test
  fun stripMetadataRemovesDetailsButKeepsPages() {
    val cleaned = stripMetadata(pdfWithDetails())
    PDDocument.load(cleaned).use { doc ->
      assertEquals(1, doc.numberOfPages)
      assertTrue(doc.documentInformation.title.isNullOrBlank())
      assertTrue(doc.documentInformation.author.isNullOrBlank())
    }
    val report = String(reportPdf("cleaned.pdf", cleaned), Charsets.UTF_8)
    assertTrue(report, !report.contains("Title: Quarterly report"))
  }
}

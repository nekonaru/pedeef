package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.Named
import com.nekonaru.pedeef.engine.batesNumber
import com.nekonaru.pedeef.engine.borderPages
import com.nekonaru.pedeef.engine.checksumManifest
import com.nekonaru.pedeef.engine.duplicatePages
import com.nekonaru.pedeef.engine.extractComments
import com.nekonaru.pedeef.engine.extractTextByPage
import com.nekonaru.pedeef.engine.insertBlankPages
import com.nekonaru.pedeef.engine.integrityCheck
import com.nekonaru.pedeef.engine.removeEmbeddedFiles
import com.nekonaru.pedeef.engine.removeJavaScript
import com.nekonaru.pedeef.engine.replacePages
import com.nekonaru.pedeef.engine.sanitizeAll
import com.nekonaru.pedeef.engine.stampText
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

private fun threePages(): ByteArray {
  PDDocument().use { doc ->
    repeat(3) { doc.addPage(PDPage(PDRectangle(300f, 400f))) }
    return ByteArrayOutputStream().use { doc.save(it); it.toByteArray() }
  }
}

class ToolsV2SmokeTest {
  @Test
  fun insertBlankAddsPages() {
    PDDocument.load(insertBlankPages(threePages(), 1, 2)).use { assertEquals(5, it.numberOfPages) }
  }

  @Test
  fun duplicatePagesRepeatsSelection() {
    PDDocument.load(duplicatePages(threePages(), "2")).use { assertEquals(4, it.numberOfPages) }
  }

  @Test
  fun replacePagesUsesSecondDocument() {
    PDDocument.load(replacePages(threePages(), threePages(), "2")).use { assertEquals(3, it.numberOfPages) }
  }

  @Test
  fun stampsAndBordersKeepPageCount() {
    PDDocument.load(borderPages(threePages(), 2f)).use { assertEquals(3, it.numberOfPages) }
    PDDocument.load(batesNumber(threePages(), "DOC-", 10)).use { assertEquals(3, it.numberOfPages) }
    PDDocument.load(stampText(threePages(), "OK", 50f, 90f)).use { assertEquals(3, it.numberOfPages) }
  }

  @Test
  fun cleanupToolsKeepPages() {
    PDDocument.load(removeEmbeddedFiles(threePages())).use { assertEquals(3, it.numberOfPages) }
    PDDocument.load(removeJavaScript(threePages())).use { assertEquals(3, it.numberOfPages) }
    PDDocument.load(sanitizeAll(threePages())).use { assertEquals(3, it.numberOfPages) }
  }

  @Test
  fun reportsAreProduced() {
    assertTrue(String(extractComments(threePages()), Charsets.UTF_8).contains("comments"))
    assertTrue(String(extractTextByPage(threePages()), Charsets.UTF_8).contains("Page 1"))
    assertTrue(String(integrityCheck(threePages()), Charsets.UTF_8).contains("All pages readable"))
    val lines = String(checksumManifest(listOf(Named("a.pdf", threePages()))), Charsets.UTF_8).lines()
    assertTrue(lines.any { it.matches(Regex("[0-9a-f]{64}  a.pdf")) })
  }
}

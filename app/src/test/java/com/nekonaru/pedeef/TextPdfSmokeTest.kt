package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.textToPdf
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextPdfSmokeTest {
  @Test
  fun textAppearsOnThePage() {
    val bytes = textToPdf("hello pedeef", "A4", 12)
    PDDocument.load(bytes).use { doc ->
      assertEquals(1, doc.numberOfPages)
      val text = PDFTextStripper().getText(doc)
      assertTrue("text was not extractable: $text", text.contains("hello pedeef"))
    }
  }

  @Test(expected = com.nekonaru.pedeef.engine.UserError::class)
  fun emptyTextFails() {
    textToPdf("   ", "A4", 12)
  }
}

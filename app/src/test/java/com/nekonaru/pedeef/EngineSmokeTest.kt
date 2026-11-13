package com.nekonaru.pedeef

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

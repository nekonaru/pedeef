package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.UserError
import com.nekonaru.pedeef.tools.Input
import com.nekonaru.pedeef.tools.TOOLS
import com.nekonaru.pedeef.tools.bool
import com.nekonaru.pedeef.tools.defaults
import com.nekonaru.pedeef.tools.str
import com.nekonaru.pedeef.tools.stepTools
import com.nekonaru.pedeef.tools.toolById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolCatalogTest {
  @Test
  fun idsAreUnique() {
    val ids = TOOLS.map { it.id }
    assertEquals(ids.size, ids.toSet().size)
  }

  @Test
  fun sectionsAreInRange() {
    for (t in TOOLS) assertTrue("${t.id} section ${t.section}", t.section in 0..6)
  }

  @Test
  fun everyToolIsRunnable() {
    for (t in TOOLS) assertNotNull(t.run)
  }

  @Test
  fun catalogCoversTheOriginalsPlusNew() {
    assertEquals(26, TOOLS.size)
    for (id in listOf("merge", "alternate", "compress", "split", "splitText", "splitBookmarks", "splitHalf", "splitSize", "zip", "organize", "rotate", "flip", "nup", "crop", "fillSign", "watermark", "pageNumbers", "redact", "ocr", "unlock", "password", "metadata", "workflow", "imagesToPdf", "pdfToImages", "extractText", "scan")) {
      // scan is not a catalog tool; the rest must exist
      if (id == "scan") assertNull(toolById(id)) else assertNotNull(toolById(id))
    }
  }

  @Test
  fun stepToolsExcludesCustomEditorsAndNonPdfOutputs() {
    val stepIds = stepTools().map { it.id }.toSet()
    for (excluded in listOf("scan", "workflow", "organize", "redact", "fillSign", "zip", "password", "unlock", "metadata", "pdfToImages", "extractText")) {
      assertTrue(excluded, excluded !in stepIds)
    }
  }

  @Test
  fun defaultsProduceSegInitialValue() {
    val d = com.nekonaru.pedeef.tools.defaults(
      listOf(com.nekonaru.pedeef.tools.OptDef.Seg("mode", "Compress by", "Kompres berdasarkan", listOf(Triple("level", "Level", "Level"), Triple("size", "Fit under a size", "Sesuai uk")), "level")),
    )
    assertEquals("level", d.str("mode"))
  }

  @Test
  fun optHelpersReadDefaults() {
    val d = com.nekonaru.pedeef.tools.defaults(listOf(com.nekonaru.pedeef.tools.OptDef.Toggle("flag", "Flag", "Flag", true)))
    assertTrue(d.bool("flag"))
    assertEquals("", d.str("missing"))
    assertEquals(false, d.bool("missing"))
  }

  @Test
  fun userErrorIsShownAsIs() {
    val e = UserError("hello")
    assertEquals("hello", e.message)
  }
}

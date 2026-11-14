package com.nekonaru.pedeef

import com.nekonaru.pedeef.data.FilesStore
import org.junit.Assert.assertEquals
import org.junit.Test

class FilesStoreTest {
  @Test
  fun sanitizeName_stripsSeparatorsAndIllegalCharacters() {
    assertEquals(".._.._report", FilesStore.sanitizeName("../../report"))
    assertEquals("a_b_c", FilesStore.sanitizeName("a/b\\c"))
    assertEquals("a_b_c_", FilesStore.sanitizeName("a:b*c?"))
  }

  @Test
  fun sanitizeName_keepsOrdinaryNames() {
    assertEquals("report 2026.pdf", FilesStore.sanitizeName("report 2026.pdf"))
    assertEquals("Meeting Notes", FilesStore.sanitizeName("Meeting Notes"))
  }

  @Test
  fun sanitizeName_rejectsDotsOnly() {
    assertEquals("", FilesStore.sanitizeName(".."))
    assertEquals("", FilesStore.sanitizeName("..."))
  }

  @Test
  fun sanitizeName_truncatesLongNames() {
    val long = "a".repeat(500)
    assertEquals(120, FilesStore.sanitizeName(long).length)
  }

  @Test
  fun sanitizeName_stripsControlCharacters() {
    assertEquals("a_b_c_", FilesStore.sanitizeName("a\u0000b\u0001c\u001F"))
  }
}

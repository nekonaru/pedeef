package com.nekonaru.pedeef

import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Strings uses a positional constructor: these assertions guard against translated values drifting out of position. */
class StringsTest {
  @Test
  fun idTailStringsMatchTheirFields() {
    assertEquals("Cetak", Id.print)
    assertEquals("Nama", Id.sortName)
    assertEquals("Terbaru", Id.sortDate)
    assertEquals("Terbesar", Id.sortSize)
    assertEquals("Putar semua", Id.rotateAll)
    assertEquals("%1\$d file tersimpan", Id.savedNFiles)
    assertTrue(Id.decryptNotice.startsWith("Dibuka tanpa sandi"))
    assertEquals("Periksa pembaruan", Id.versionRow)
    assertEquals("Pengembang", Id.developer)
    assertEquals("Lihat di GitHub", Id.viewOnGithub)
    assertEquals("Thumbnail halaman", Id.pageThumbs)
  }

  @Test
  fun enTailStringsMatchTheirFields() {
    assertEquals("Print", En.print)
    assertEquals("Name", En.sortName)
    assertEquals("Developer", En.developer)
    assertEquals("Page thumbnails", En.pageThumbs)
  }
}

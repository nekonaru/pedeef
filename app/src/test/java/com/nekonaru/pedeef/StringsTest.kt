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
    assertEquals("Ekstensi", Id.sortExt)
    assertEquals("%1\$d file tersimpan", Id.savedNFiles)
    assertTrue(Id.decryptNotice.startsWith("Dibuka tanpa sandi"))
    assertEquals("Periksa pembaruan", Id.versionRow)
    assertEquals("Pengembang", Id.developer)
    assertEquals("Lihat di GitHub", Id.viewOnGithub)
    assertEquals("Thumbnail halaman", Id.pageThumbs)
    assertEquals("File", Id.fileOne)
    assertEquals("File", Id.fileMany)
    assertEquals("Cari di folder ini", Id.searchInFolder)
    assertEquals("Atur ulang", Id.reset)
    assertEquals("Warna", Id.color)
    assertEquals("Halaman", Id.pageName)
    assertEquals("Bacakan halaman", Id.speakPage)
    assertEquals("Simpan preset", Id.savePreset)
    assertEquals("Jalankan di folder", Id.batchRun)
    assertEquals("Dua halaman", Id.spread)
  }

  @Test
  fun enTailStringsMatchTheirFields() {
    assertEquals("Print", En.print)
    assertEquals("Name", En.sortName)
    assertEquals("Extension", En.sortExt)
    assertEquals("Developer", En.developer)
    assertEquals("Page thumbnails", En.pageThumbs)
    assertEquals("File", En.fileOne)
    assertEquals("Files", En.fileMany)
    assertEquals("Search in this folder", En.searchInFolder)
    assertEquals("Reset", En.reset)
    assertEquals("Color", En.color)
    assertEquals("Page", En.pageName)
    assertEquals("Read page aloud", En.speakPage)
    assertEquals("Save preset", En.savePreset)
    assertEquals("Run on folder", En.batchRun)
    assertEquals("Two pages", En.spread)
  }
}

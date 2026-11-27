package com.nekonaru.pedeef

import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.tools.TOOLS
import com.nekonaru.pedeef.ui.theme.Motion
import com.nekonaru.pedeef.ui.theme.MotionSpec
import com.nekonaru.pedeef.ui.theme.TOOL_ICONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Design tokens and translated copy are data, so they can be checked on the JVM. */
class DesignTokensTest {
  @Test
  fun everyToolHasAnIcon() {
    val missing = TOOLS.map { it.id }.filterNot { it in TOOL_ICONS }
    assertTrue("Missing tool icons: $missing", missing.isEmpty())
  }

  @Test
  fun motionSpecScalesDurations() {
    assertEquals(Motion.Medium, MotionSpec(1f).medium)
    assertEquals(Motion.Medium / 2, MotionSpec(0.5f).medium)
    assertEquals(0, MotionSpec(0f).medium)
    assertEquals(Motion.Long * 2, MotionSpec(2f).long)
  }

  @Test
  fun newStringsKeepLanguageAlignment() {
    assertEquals("More", En.more)
    assertEquals("Clear search", En.clearSearch)
    assertEquals("Offline, on device", En.offlineBadge)
    assertEquals("Lainnya", Id.more)
    assertEquals("Hapus pencarian", Id.clearSearch)
    assertEquals("Offline, di perangkat", Id.offlineBadge)
    assertEquals("Page %1\$d of %2\$d", En.progressPage)
    assertEquals("Halaman %1\$d dari %2\$d", Id.progressPage)
    assertEquals("Hide thumbnails", En.hideThumbs)
    assertEquals("Sembunyikan thumbnail", Id.hideThumbs)
    assertEquals("Enter a value from 0 to 100", En.positionHint)
    assertEquals("Masukkan nilai 0 sampai 100", Id.positionHint)
  }
}

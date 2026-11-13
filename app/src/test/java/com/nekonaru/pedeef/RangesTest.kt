package com.nekonaru.pedeef

import com.nekonaru.pedeef.engine.UserError
import com.nekonaru.pedeef.engine.isJpeg
import com.nekonaru.pedeef.engine.isPng
import com.nekonaru.pedeef.engine.jpegSize
import com.nekonaru.pedeef.engine.parseRanges
import com.nekonaru.pedeef.engine.unrotate
import com.nekonaru.pedeef.engine.winAnsi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RangesTest {
  @Test
  fun parseRanges_simple() {
    assertEquals(listOf(listOf(0, 1, 2), listOf(4), listOf(7, 8, 9)), parseRanges("1-3, 5, 8-", 10))
  }

  @Test
  fun parseRanges_singlePage() {
    assertEquals(listOf(listOf(2)), parseRanges("3", 10))
  }

  @Test
  fun parseRanges_openEnd() {
    assertEquals(listOf(listOf(0), listOf(4, 5, 6, 7, 8, 9)), parseRanges("1, 5-", 10))
  }

  @Test
  fun parseRanges_reversedFails() {
    assertThrows(UserError::class.java) { parseRanges("3-1", 5) }
  }

  @Test
  fun parseRanges_emptyFails() {
    assertThrows(UserError::class.java) { parseRanges("", 5) }
  }

  @Test
  fun parseRanges_clampsToPageCount() {
    assertEquals(listOf(listOf(0, 1, 2, 3, 4)), parseRanges("1-99", 5))
  }

  @Test
  fun winAnsi_dropsNonLatin() {
    assertEquals("Hello, w", winAnsi("Hello, w\u4E16\u754C"))
  }

  @Test
  fun unrotate_zero() {
    assertEquals(listOf(0.1f, 0.1f, 0.3f, 0.4f), unrotate(listOf(0.1f, 0.5f, 0.3f, 0.4f), 0).let { listOf(it[0], round2(it[1]), it[2], it[3]) })
  }

  private fun round2(f: Float) = Math.round(f * 100f) / 100f

  @Test
  fun unrotate_ninety() {
    assertEquals(listOf(0.5f, 0.1f, 0.4f, 0.3f), unrotate(listOf(0.1f, 0.5f, 0.3f, 0.4f), 90))
  }

  @Test
  fun unrotate_oneEighty() {
    assertEquals(listOf(0.6f, 0.5f, 0.3f, 0.4f), unrotate(listOf(0.1f, 0.5f, 0.3f, 0.4f), 180).let { listOf(round2(it[0]), it[1], it[2], it[3]) })
  }

  @Test
  fun unrotate_twoSeventy() {
    assertEquals(listOf(0.1f, 0.6f, 0.4f, 0.3f), unrotate(listOf(0.1f, 0.5f, 0.3f, 0.4f), 270).let { listOf(round2(it[0]), round2(it[1]), it[2], it[3]) })
  }

  @Test
  fun jpegSize_readsFrameHeader() {
    // Minimal SOF0 segment: FF C0 <len 0x11> <precision 8> <height 0x0140> <width 0x0238>
    val bytes = byteArrayOf(
      0xFF.toByte(), 0xD8.toByte(),
      0xFF.toByte(), 0xC0.toByte(), 0x00, 0x11, 0x08,
      0x01, 0x40, // height 320
      0x02, 0x38, // width 568
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    )
    assertEquals(568 to 320, jpegSize(bytes))
  }

  @Test
  fun jpegSize_garbageReturnsNull() {
    assertNull(jpegSize(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)))
  }

  @Test
  fun pngAndJpegMagic() {
    assertTrue(isPng(byteArrayOf(0x89.toByte(), 0x50)))
    assertTrue(isJpeg(byteArrayOf(0xFF.toByte(), 0xD8.toByte())))
  }
}

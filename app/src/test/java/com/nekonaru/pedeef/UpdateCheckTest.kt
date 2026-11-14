package com.nekonaru.pedeef

import com.nekonaru.pedeef.data.UpdateCheck
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCheckTest {
  @Test
  fun compareTag_newer() {
    assertEquals(1, UpdateCheck.compareTag("v1.2.1", "1.2.0"))
    assertEquals(1, UpdateCheck.compareTag("v2.0.0", "1.9.9"))
    assertEquals(1, UpdateCheck.compareTag("v1.10.0", "1.9.0"))
  }

  @Test
  fun compareTag_equal() {
    assertEquals(0, UpdateCheck.compareTag("v1.2.0", "1.2.0"))
    assertEquals(0, UpdateCheck.compareTag("1.2.0", "1.2"))
    assertEquals(0, UpdateCheck.compareTag("v1.2", "1.2.0"))
  }

  @Test
  fun compareTag_older() {
    assertEquals(-1, UpdateCheck.compareTag("v1.2.0", "1.2.1"))
    assertEquals(-1, UpdateCheck.compareTag("v1.9.9", "2.0.0"))
    assertEquals(-1, UpdateCheck.compareTag("v1.9.0", "1.10.0"))
  }

  @Test
  fun compareTag_garbageTreatedAsOlder() {
    assertEquals(-1, UpdateCheck.compareTag("garbage", "1.2.0"))
  }
}

package com.nekonaru.pedeef

import com.nekonaru.pedeef.ui.kit.RenderMath
import org.junit.Assert.assertEquals
import org.junit.Test

/** Render sizing is pure math, so the guards run on the JVM without a device. */
class RenderMathTest {
  @Test
  fun widthsUseStableBuckets() {
    assertEquals(120, RenderMath.widthBucket(100))
    assertEquals(120, RenderMath.widthBucket(180))
    assertEquals(240, RenderMath.widthBucket(241))
    assertEquals(2880, RenderMath.widthBucket(5000))
  }

  @Test
  fun pageWidthFollowsViewportAndDensity() {
    assertEquals(1080, RenderMath.targetPageWidth(360f, 1f, 3f))
    assertEquals(1620, RenderMath.targetPageWidth(360f, 1.5f, 3f))
    assertEquals(240, RenderMath.targetPageWidth(10f, 1f, 1f))
    assertEquals(2400, RenderMath.targetPageWidth(2000f, 2f, 3f))
  }

  @Test
  fun thumbnailsStaySmall() {
    assertEquals(120, RenderMath.ThumbnailWidth)
  }
}

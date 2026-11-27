package com.nekonaru.pedeef.ui.kit

/** Pure helpers for render sizes, kept separate so they can be tested on the JVM. */
object RenderMath {
  const val Bucket = 120
  const val ThumbnailWidth = 120

  /** Round a requested width to the nearest cache bucket and cap it. */
  fun widthBucket(widthPx: Int): Int = (widthPx / Bucket).coerceIn(1, 24) * Bucket

  /** Render at the actual screen size instead of a fixed width. */
  fun targetPageWidth(viewportDp: Float, zoom: Float, density: Float): Int =
    (viewportDp * zoom * density).toInt().coerceIn(240, 2400)
}

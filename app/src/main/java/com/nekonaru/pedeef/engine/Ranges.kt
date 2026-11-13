package com.nekonaru.pedeef.engine

// Pure helpers with no Android or PdfBox dependencies, so they are unit-testable on the JVM.

/** "1-3, 5, 8-" -> [[0,1,2],[4],[7..n-1]] */
fun parseRanges(str: String, n: Int): List<List<Int>> {
  val parts = str.split(',').map { it.trim() }.filter { it.isNotEmpty() }
  if (parts.isEmpty()) fail("Enter page ranges, e.g. 1-3, 5")
  return parts.map { part ->
    val ab = part.split('-').map { it.trim() }
    val start = maxOf(1, ab[0].toIntOrNull() ?: 1)
    val end = minOf(n, if (ab.size < 2) start else ab[1].toIntOrNull() ?: n)
    if (start > end) fail("\"$part\" is outside pages 1-$n")
    (start - 1 until end).toList()
  }
}

/** Sizes are matched loosely: "1-3" parses the same in any locale. */
fun isPlausiblePageList(str: String): Boolean = Regex("^[0-9,\\-\\s]+$").matches(str.trim())

/** A JPEG's [width, height], read from its frame header. Null when the bytes are not a baseline JPEG. */
fun jpegSize(b: ByteArray): Pair<Int, Int>? {
  fun u(i: Int) = b[i].toInt() and 0xFF
  if (b.size < 4) return null
  var i = 2
  while (i + 8 < b.size) {
    if (u(i) != 0xFF) return null
    val m = u(i + 1)
    if (m == 0xFF) { i++; continue } // fill byte
    if (m in 0xC0..0xCF && m != 0xC4 && m != 0xC8 && m != 0xCC) return ((u(i + 7) shl 8) or u(i + 8)) to ((u(i + 5) shl 8) or u(i + 6))
    i += 2 + ((u(i + 2) shl 8) or u(i + 3))
  }
  return null
}

/**
 * The standard PDF fonts are WinAnsi, so a character they cannot write is folded to a plain form or dropped,
 * rather than failing the whole document.
 */
fun winAnsi(s: String): String = s.filter { it.code in 0x20..0x7E || it.code in 0xA0..0xFF }

/** The page as displayed is the page as stored turned by /Rotate, so a rect drawn on screen is turned back for the page. */
fun unrotate(b: Box, rot: Int): Box {
  val (x, y, w, h) = b
  return when (rot) {
    90 -> listOf(y, x, h, w)
    180 -> listOf(1 - x - w, y, w, h)
    270 -> listOf(1 - y - h, 1 - x - w, h, w)
    else -> listOf(x, 1 - y - h, w, h)
  }
}

/** True for bytes that start with the PNG magic number. */
fun isPng(b: ByteArray): Boolean = b.size > 1 && b[0] == 0x89.toByte() && b[1] == 0x50.toByte()

/** True for bytes that start with the JPEG magic number. */
fun isJpeg(b: ByteArray): Boolean = b.size > 1 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte()

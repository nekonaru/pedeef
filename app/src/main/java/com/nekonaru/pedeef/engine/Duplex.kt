package com.nekonaru.pedeef.engine

import android.graphics.pdf.PdfRenderer

/** Keeps only odd- or even-numbered pages (1-based), for scanned duplex documents. */
fun keepOddEven(bytes: ByteArray, keep: String): ByteArray = loadPdf(bytes).use { doc ->
  val wantEven = keep == "even"
  val pages = (0 until doc.numberOfPages).filter { ((it + 1) % 2 == 0) == wantEven }
  if (pages.isEmpty()) fail("No pages to keep.")
  pickIntoNew(doc, pages)
}

/** Splits a PDF into parts separated by fully blank pages (the blank pages are dropped). */
fun splitAtBlanks(bytes: ByteArray, cacheDir: java.io.File, maxPages: Int = 500): List<Named> = withTempPdf(bytes, cacheDir, "splitblank") { tmp ->
  val src = loadPdf(bytes)
  val result = ArrayList<Named>()
  try {
    val blanks = HashSet<Int>()
    android.os.ParcelFileDescriptor.open(tmp, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
      PdfRenderer(fd).use { r ->
        for (i in 0 until minOf(r.pageCount, maxPages)) {
          r.openPage(i).use { if (isBlankPage(it)) blanks.add(i) }
        }
      }
    }
    if (blanks.isEmpty()) fail("No blank pages found. Nothing to split at.")
    var group = ArrayList<Int>()
    for (i in 0 until src.numberOfPages) {
      if (i in blanks) {
        if (group.isNotEmpty()) result.add(Named("part-${result.size + 1}", pickIntoNew(src, group)))
        group = ArrayList()
      } else {
        group.add(i)
      }
    }
    if (group.isNotEmpty()) result.add(Named("part-${result.size + 1}", pickIntoNew(src, group)))
    result
  } finally {
    src.close()
  }
}

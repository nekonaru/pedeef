package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.TreeMap

/**
 * Compares two PDFs page by page and produces a plain-text report: which pages are identical, which
 * changed (with the lines that differ), and which exist in only one document. Text comparison only;
 * formatting, images and annotations are ignored.
 */
fun comparePdfs(aName: String, aBytes: ByteArray, bName: String, bBytes: ByteArray): ByteArray {
  val a = loadPdf(aBytes)
  val b = loadPdf(bBytes)
  try {
    val pagesA = a.pages.count
    val pagesB = b.pages.count
    val n = maxOf(pagesA, pagesB)
    val out = StringBuilder()
    out.append("Pedeef PDF comparison\n")
    out.append("A: $aName (${pagesA} page${if (pagesA == 1) "" else "s"})\n")
    out.append("B: $bName (${pagesB} page${if (pagesB == 1) "" else "s"})\n\n")

    var identical = 0
    var changed = 0
    var onlyA = 0
    var onlyB = 0
    val changedLines = StringBuilder()

    for (i in 1..n) {
      val la = if (i <= pagesA) pageLines(a, i) else null
      val lb = if (i <= pagesB) pageLines(b, i) else null
      when {
        la == null -> { onlyB++; out.append("Page $i: only in B\n") }
        lb == null -> { onlyA++; out.append("Page $i: only in A\n") }
        la == lb -> { identical++; out.append("Page $i: identical\n") }
        else -> {
          changed++
          val (added, removed, excerpt) = diffLines(la, lb)
          out.append("Page $i: $added line(s) added, $removed line(s) removed\n")
          if (excerpt.isNotEmpty()) {
            changedLines.append("Page $i:\n")
            changedLines.append(excerpt)
            changedLines.append("\n")
          }
        }
      }
    }

    out.append("\nSummary\n")
    out.append("Identical pages: $identical\n")
    out.append("Changed pages: $changed\n")
    out.append("Only in A: $onlyA\n")
    out.append("Only in B: $onlyB\n")
    if (changedLines.isNotEmpty()) {
      out.append("\nDifferences (first differences per page, capped)\n\n")
      out.append(changedLines)
    }
    return out.toString().toByteArray(Charsets.UTF_8)
  } finally {
    runCatching { a.close() }
    runCatching { b.close() }
  }
}

private fun pageLines(doc: PDDocument, page: Int): List<String> {
  val stripper = PDFTextStripper().apply { startPage = page; endPage = page }
  return runCatching { stripper.getText(doc) }.getOrDefault("")
    .lines()
    .map { it.trim() }
    .filter { it.isNotEmpty() }
}

/** Line-level diff. Returns added/removed counts and a `+`/`-` excerpt, capped at ~4000 chars. */
private fun diffLines(a: List<String>, b: List<String>): Triple<Int, Int, String> {
  // Fallback for very large pages: multiset difference, no ordering.
  if (a.size.toLong() * b.size > 4_000_000L) {
    val remaining = TreeMap<String, Int>()
    for (l in b) remaining[l] = (remaining[l] ?: 0) + 1
    for (l in a) remaining[l] = (remaining[l] ?: 0) - 1
    var added = 0
    var removed = 0
    val sb = StringBuilder()
    for ((line, delta) in remaining) {
      if (delta > 0) { repeat(delta) { added++; if (sb.length < 4000) sb.append("+ $line\n") } }
      else if (delta < 0) { repeat(-delta) { removed++; if (sb.length < 4000) sb.append("- $line\n") } }
    }
    if (sb.length > 4000) sb.setLength(4000)
    return Triple(added, removed, sb.toString())
  }

  // LCS on lines; then walk the matrix to classify.
  val m = a.size
  val n = b.size
  val dp = Array(m + 1) { IntArray(n + 1) }
  for (i in m - 1 downTo 0) for (j in n - 1 downTo 0) {
    dp[i][j] = if (a[i] == b[j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
  }
  var i = 0
  var j = 0
  var added = 0
  var removed = 0
  val sb = StringBuilder()
  while (i < m && j < n) {
    when {
      a[i] == b[j] -> { i++; j++ }
      dp[i + 1][j] >= dp[i][j + 1] -> { removed++; if (sb.length < 4000) sb.append("- ${a[i]}\n"); i++ }
      else -> { added++; if (sb.length < 4000) sb.append("+ ${b[j]}\n"); j++ }
    }
  }
  while (i < m) { removed++; if (sb.length < 4000) sb.append("- ${a[i]}\n"); i++ }
  while (j < n) { added++; if (sb.length < 4000) sb.append("+ ${b[j]}\n"); j++ }
  if (sb.length > 4000) sb.setLength(4000)
  return Triple(added, removed, sb.toString())
}

package com.nekonaru.pedeef.data

import java.io.File

/** Recent search phrases, capped at 20. */
object SearchHistory {
  private const val MAX = 20
  private fun fileOf(context: android.content.Context) = File(context.filesDir, "search-history.txt")

  fun load(context: android.content.Context): List<String> = runCatching {
    fileOf(context).readLines().map { it.trim() }.filter { it.isNotEmpty() }.take(MAX)
  }.getOrDefault(emptyList())

  fun add(context: android.content.Context, query: String): List<String> {
    val q = query.trim()
    if (q.isEmpty()) return load(context)
    val next = (listOf(q) + load(context).filterNot { it.equals(q, true) }).distinct().take(MAX)
    runCatching { fileOf(context).writeText(next.joinToString("\n")) }
    return next
  }

  fun clear(context: android.content.Context) {
    runCatching { fileOf(context).writeText("") }
  }
}

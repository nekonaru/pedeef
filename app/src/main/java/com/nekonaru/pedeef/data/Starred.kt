package com.nekonaru.pedeef.data

import java.io.File

/** Starred file paths, capped and pruned when files disappear. */
object Starred {
  private const val MAX = 100
  private fun fileOf(context: android.content.Context) = File(context.filesDir, "starred.txt")

  fun load(context: android.content.Context): List<String> = runCatching {
    fileOf(context).readLines().map { it.trim() }.filter { it.isNotEmpty() }.take(MAX)
  }.getOrDefault(emptyList())

  fun toggle(context: android.content.Context, path: String): List<String> {
    val next = if (path in load(context)) load(context) - path else load(context) + path
    val capped = next.takeLast(MAX)
    runCatching { fileOf(context).writeText(capped.joinToString("\n")) }
    return capped
  }

  fun prune(context: android.content.Context, existing: Set<String>): List<String> {
    val next = load(context).filter { it in existing }
    runCatching { fileOf(context).writeText(next.joinToString("\n")) }
    return next
  }
}

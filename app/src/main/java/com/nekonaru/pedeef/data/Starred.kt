package com.nekonaru.pedeef.data

import java.io.File

/** Starred file paths, capped and cleaned when files disappear. */
object Starred {
  private const val MAX = 100
  private fun fileOf(context: android.content.Context) = File(context.filesDir, "starred.txt")

  fun load(context: android.content.Context): List<String> = runCatching {
    fileOf(context).readLines().map { it.trim() }.filter { it.isNotEmpty() }.take(MAX)
  }.getOrDefault(emptyList())

  fun toggle(context: android.content.Context, path: String): List<String> {
    val current = load(context)
    val next = if (path in current) current - path else current + path
    val capped = next.takeLast(MAX)
    runCatching { fileOf(context).writeText(capped.joinToString("\n")) }
    return capped
  }

  /** Drops only the given paths. No full tree walk is needed when the deleted paths are known. */
  fun remove(context: android.content.Context, paths: Set<String>): List<String> {
    if (paths.isEmpty()) return load(context)
    val next = load(context).filterNot { it in paths }
    runCatching { fileOf(context).writeText(next.joinToString("\n")) }
    return next
  }

  fun prune(context: android.content.Context, existing: Set<String>): List<String> {
    val next = load(context).filter { it in existing }
    runCatching { fileOf(context).writeText(next.joinToString("\n")) }
    return next
  }
}

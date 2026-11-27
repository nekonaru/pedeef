package com.nekonaru.pedeef.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Remembers which page each document was last viewed on, so reopening it continues where the user left off.
 * Keyed by name + byte size; when a file is replaced by a different document with the same name it is best effort.
 * Capped at 200 entries; oldest entries are dropped. The JSON is parsed once and kept in memory.
 */
object ReadingPositions {
  private const val MAX = 200
  private val json = Json { ignoreUnknownKeys = true }
  private var cachedPath: String? = null
  private var cached: LinkedHashMap<String, Int>? = null

  private fun fileOf(appContext: android.content.Context) = File(appContext.filesDir, "reading-positions.json")

  private fun loadAll(appContext: android.content.Context): LinkedHashMap<String, Int> =
    runCatching {
      val raw = json.decodeFromString<Map<String, Int>>(fileOf(appContext).readText())
      LinkedHashMap(raw)
    }.getOrDefault(LinkedHashMap())

  private fun mapOf(appContext: android.content.Context): LinkedHashMap<String, Int> {
    val path = appContext.filesDir.path
    val current = cached
    if (cachedPath == path && current != null) return current
    return loadAll(appContext).also {
      cached = it
      cachedPath = path
    }
  }

  /** Page number (1-based) the user was last on, or null when unknown. */
  fun pageFor(appContext: android.content.Context, key: String): Int? = mapOf(appContext)[key]

  fun save(appContext: android.content.Context, key: String, page: Int) {
    if (page < 1) return
    val map = mapOf(appContext)
    if (map[key] == page) return
    map.remove(key) // re-insert to mark it as most recent
    map[key] = page
    while (map.size > MAX) map.remove(map.keys.first())
    // Atomic write: never leave a half-written JSON behind on a kill.
    runCatching {
      val tmp = File(appContext.filesDir, "reading-positions.json.tmp")
      tmp.writeText(json.encodeToString(map))
      tmp.renameTo(fileOf(appContext))
    }
  }
}

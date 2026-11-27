package com.nekonaru.pedeef.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class DocumentBookmarks(val key: String, val pages: List<Int>)

/** Reader bookmarks, separate from the PDF's own outline. */
object UserBookmarks {
  private val json = Json { ignoreUnknownKeys = true }
  private fun fileOf(context: Context) = File(context.filesDir, "reader-bookmarks.json")

  private fun all(context: Context): MutableMap<String, List<Int>> = runCatching {
    json.decodeFromString<Map<String, List<Int>>>(fileOf(context).readText()).toMutableMap()
  }.getOrDefault(mutableMapOf())

  private fun save(context: Context, map: Map<String, List<Int>>) {
    runCatching { fileOf(context).writeText(json.encodeToString(map)) }
  }

  fun pages(context: Context, key: String): List<Int> = all(context)[key].orEmpty()

  fun toggle(context: Context, key: String, page: Int): List<Int> {
    val map = all(context)
    val next = (map[key].orEmpty()).toMutableList()
    if (page in next) next.remove(page) else if (page > 0) next.add(page)
    next.sort()
    map[key] = next.take(200)
    save(context, map)
    return map[key]!!
  }

  fun clear(context: Context, key: String) {
    val map = all(context)
    map.remove(key)
    save(context, map)
  }
}

package com.nekonaru.pedeef.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Per-tool default options, separate from named presets. */
object ToolDefaults {
  private val json = Json { ignoreUnknownKeys = true }
  private fun fileOf(context: Context) = File(context.filesDir, "tool-defaults.json")

  fun load(context: Context): Map<String, String> = runCatching {
    json.decodeFromString<Map<String, String>>(fileOf(context).readText())
  }.getOrDefault(emptyMap())

  fun save(context: Context, toolId: String, opts: String): Map<String, String> {
    val next = load(context).toMutableMap()
    next[toolId] = opts
    runCatching { fileOf(context).writeText(json.encodeToString(next)) }
    return next
  }

  fun forget(context: Context, toolId: String): Map<String, String> {
    val next = load(context).toMutableMap()
    next.remove(toolId)
    runCatching { fileOf(context).writeText(json.encodeToString(next)) }
    return next
  }
}

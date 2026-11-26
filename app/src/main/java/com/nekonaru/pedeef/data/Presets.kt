package com.nekonaru.pedeef.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

@Serializable
data class Preset(val id: String, val name: String, val toolId: String, val opts: JsonObject)

/** Named tool configurations saved as JSON in filesDir, capped at 30 entries. */
object Presets {
  private const val MAX = 30
  private val json = Json { ignoreUnknownKeys = true }
  private fun fileOf(context: android.content.Context) = File(context.filesDir, "presets.json")

  fun load(context: android.content.Context): List<Preset> = runCatching {
    json.decodeFromString<List<Preset>>(fileOf(context).readText())
  }.getOrDefault(emptyList())

  fun save(context: android.content.Context, value: Preset): List<Preset> {
    val next = (load(context).filterNot { it.id == value.id } + value).takeLast(MAX)
    runCatching { fileOf(context).writeText(json.encodeToString(next)) }
    return next
  }

  fun remove(context: android.content.Context, id: String): List<Preset> {
    val next = load(context).filterNot { it.id == id }
    runCatching { fileOf(context).writeText(json.encodeToString(next)) }
    return next
  }
}

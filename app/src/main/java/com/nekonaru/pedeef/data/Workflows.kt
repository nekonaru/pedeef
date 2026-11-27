package com.nekonaru.pedeef.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Workflow(
  val id: String,
  val name: String,
  val steps: List<com.nekonaru.pedeef.engine.WorkflowStep>,
  val created: Long = System.currentTimeMillis(),
)

/** Saved tool chains. Stored as JSON in app storage, never sent anywhere. */
object Workflows {
  private val json = Json { ignoreUnknownKeys = true }
  private const val MAX = 50
  private fun fileOf(context: Context) = File(context.filesDir, "workflows.json")

  fun load(context: Context): List<Workflow> = runCatching {
    json.decodeFromString<List<Workflow>>(fileOf(context).readText()).take(MAX)
  }.getOrDefault(emptyList())

  fun save(context: Context, items: List<Workflow>): List<Workflow> {
    val capped = items.takeLast(MAX)
    runCatching { fileOf(context).writeText(json.encodeToString(capped)) }
    return capped
  }

  fun add(context: Context, item: Workflow): List<Workflow> {
    val next = load(context).filterNot { it.id == item.id } + item
    return save(context, next)
  }

  fun remove(context: Context, id: String): List<Workflow> = save(context, load(context).filterNot { it.id == id })

  fun exportJson(context: Context): String = json.encodeToString(load(context))

  fun importJson(context: Context, raw: String): List<Workflow> = runCatching {
    val parsed = kotlinx.serialization.json.Json.parseToJsonElement(raw)
    val element = if (parsed is kotlinx.serialization.json.JsonObject) parsed["workflows"] ?: parsed else parsed
    val incoming = json.decodeFromString<List<Workflow>>(element.toString())
    save(context, load(context) + incoming)
  }.getOrDefault(load(context))
}

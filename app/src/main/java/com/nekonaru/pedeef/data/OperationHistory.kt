package com.nekonaru.pedeef.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Operation(
  val id: String,
  val toolId: String,
  val toolName: String,
  val time: Long,
  val outputs: List<String>,
  val opts: String = "{}",
)

/** A small local log of recent runs, used by History and by "repeat with same options". */
object OperationHistory {
  private val json = Json { ignoreUnknownKeys = true }
  private const val MAX = 20
  private fun fileOf(context: Context) = File(context.filesDir, "operations.json")

  fun load(context: Context): List<Operation> = runCatching {
    json.decodeFromString<List<Operation>>(fileOf(context).readText()).take(MAX)
  }.getOrDefault(emptyList())

  fun add(context: Context, operation: Operation): List<Operation> {
    val next = (listOf(operation) + load(context)).take(MAX)
    runCatching { fileOf(context).writeText(json.encodeToString(next)) }
    return next
  }

  fun clear(context: Context) {
    runCatching { fileOf(context).writeText("[]") }
  }
}

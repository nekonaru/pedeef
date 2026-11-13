package com.nekonaru.pedeef.engine

import kotlinx.serialization.Serializable

/** A message meant for the person using the app, shown as it is. */
class UserError(message: String) : Exception(message)

fun fail(message: String): Nothing = throw UserError(message)

/** A picked or produced file, held in memory. */
class Named(val name: String, val bytes: ByteArray) {
  val baseName: String get() = name.replace(Regex("\\.pdf$", RegexOption.IGNORE_CASE), "")
}

/** One product of a tool run. ext is "pdf", "zip", "jpg", "txt" or "png". */
data class Output(val name: String, val bytes: ByteArray, val ext: String = "pdf", val full: Boolean = false)

/** Page sizes in points, portrait. */
val SIZES: Map<String, Pair<Float, Float>> = linkedMapOf(
  "A4" to (595.28f to 841.89f), "A5" to (419.53f to 595.28f), "Letter" to (612f to 792f), "Legal" to (612f to 1008f),
)

/** [x, y, w, h] as fractions of the page as displayed, y from the top. */
typealias Box = List<Float>

@Serializable
data class PageOrder(val i: Int, val rot: Int = 0, val del: Boolean = false)

/** Something dropped onto a page in Fill & Sign. Positions are fractions of the page as displayed. */
@Serializable
data class SignItem(
  val page: Int,
  val kind: String, // text | image
  val x: Float,
  val y: Float,
  val size: Float = 0f,
  val text: String = "",
  val color: String? = null,
  val w: Float = 0f,
  val h: Float = 0f,
  val uri: String = "",
  val angle: Float = 0f,
  val opacity: Float = 1f,
)

@Serializable
data class WorkflowStep(val id: String, val op: String, val opts: kotlinx.serialization.json.JsonObject)

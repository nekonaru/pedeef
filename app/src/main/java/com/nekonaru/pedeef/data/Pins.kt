package com.nekonaru.pedeef.data

import com.nekonaru.pedeef.tools.toolById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** The ids of the tools pinned to the top of Home. */
object Pins {
  private val mutable = MutableStateFlow<List<String>>(emptyList())
  val ids: StateFlow<List<String>> = mutable

  private fun fileOf(appContext: android.content.Context) = File(appContext.filesDir, "pinned-tools.txt")

  fun load(appContext: android.content.Context) {
    mutable.value = runCatching { fileOf(appContext).readLines().filter { toolById(it) != null } }.getOrDefault(emptyList())
  }

  fun toggle(appContext: android.content.Context, id: String) {
    val next = if (id in mutable.value) mutable.value - id else mutable.value + id
    mutable.value = next
    runCatching { fileOf(appContext).writeText(next.joinToString("\n")) }
  }
}

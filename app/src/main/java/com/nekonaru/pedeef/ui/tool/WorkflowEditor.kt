package com.nekonaru.pedeef.ui.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.defaults
import com.nekonaru.pedeef.tools.put
import com.nekonaru.pedeef.tools.stepTools
import com.nekonaru.pedeef.engine.WorkflowStep
import com.nekonaru.pedeef.tools.Tool
import com.nekonaru.pedeef.ui.kit.Options
import kotlinx.serialization.json.decodeFromJsonElement

@Composable
fun WorkflowEditor(opts: Opts, onChange: (Opts) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val steps: List<WorkflowStep> = opts["steps"]?.let { runCatching { kotlinx.serialization.json.Json.decodeFromJsonElement<List<WorkflowStep>>(it) }.getOrNull() } ?: emptyList()

  fun apply(next: List<WorkflowStep>) = onChange(opts.put("steps", next))

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    if (steps.isEmpty()) {
      Text(
        if (indonesian) "Tambah langkah di bawah. Langkah berjalan dari atas ke bawah." else "Add steps below. They run top to bottom.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    steps.forEachIndexed { i, step ->
      val tool = com.nekonaru.pedeef.tools.toolById(step.op) ?: return@forEachIndexed
      Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${i + 1}.", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(tool.title(indonesian), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1)
            IconButton(onClick = { if (i > 0) apply(steps.swap(i, i - 1)) }, enabled = i > 0) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
            IconButton(onClick = { if (i < steps.size - 1) apply(steps.swap(i, i + 1)) }, enabled = i < steps.size - 1) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) }
            IconButton(onClick = { apply(steps.filterIndexed { j, _ -> j != i }) }) { Icon(Icons.Default.Close, contentDescription = null) }
          }
          if (tool.opts.isNotEmpty()) {
            Spacer8()
            Options(tool.opts, step.opts, indonesian) { newOpts ->
              apply(steps.mapIndexed { j, st -> if (j == i) st.copy(opts = newOpts) else st })
            }
          }
        }
      }
    }
    Text(if (indonesian) "Tambah langkah:" else "Add step:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(stepTools()) { t ->
        AssistChip(onClick = { apply(steps + WorkflowStep("s${System.nanoTime()}", t.id, defaults(t.opts))) }, label = { Text(t.title(indonesian)) })
      }
    }
  }
}

@Composable
private fun Spacer8() = androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))

private fun List<WorkflowStep>.swap(a: Int, b: Int): List<WorkflowStep> {
  if (a < 0 || b < 0 || a >= size || b >= size) return this
  return toMutableList().apply { val t = this[a]; this[a] = this[b]; this[b] = t }
}

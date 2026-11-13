package com.nekonaru.pedeef.ui.tool

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.put
import com.nekonaru.pedeef.tools.str
import com.nekonaru.pedeef.engine.PageOrder
import com.nekonaru.pedeef.ui.kit.rememberPageCount
import com.nekonaru.pedeef.ui.kit.rememberPageImage
import java.io.File
import kotlinx.serialization.json.decodeFromJsonElement

@Composable
fun OrganizeEditor(bytes: ByteArray?, opts: Opts, cacheDir: File, onChange: (Opts) -> Unit) {
  val s = LocalPedeefStrings.current
  if (bytes == null) {
    Text(s.pickFiles, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    return
  }
  val count = rememberPageCount(bytes, cacheDir) ?: return
  var order by remember(bytes) {
    val saved = opts["order"]?.let { runCatching { kotlinx.serialization.json.Json.decodeFromJsonElement<List<PageOrder>>(it) }.getOrNull() }
    mutableStateOf(saved?.takeIf { it.size == count } ?: (0 until count).map { PageOrder(it) })
  }
  fun apply(next: List<PageOrder>) {
    order = next
    onChange(opts.put("order", next))
  }

  val kept = order.count { !it.del }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("$kept / ${order.size}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
      OutlinedButton(onClick = { apply((0 until count).map { PageOrder(it) }) }) { Text("Reset") }
    }
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
    ) {
      itemsIndexed(order, key = { i, _ -> i }) { index, page ->
        val image = rememberPageImage(bytes, page.i, 220, cacheDir)
        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), tonalElevation = 1.dp) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp).alpha(if (page.del) 0.35f else 1f),
          ) {
            if (image != null) {
              Image(image, contentDescription = "Page ${page.i + 1}", contentScale = ContentScale.Fit, modifier = Modifier.size(72.dp))
            }
            Text("${page.i + 1}", style = MaterialTheme.typography.labelLarge)
            Row {
              IconButton(onClick = { apply(order.swap(index, index - 1)) }, enabled = index > 0) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
              IconButton(onClick = { apply(order.swap(index, index + 1)) }, enabled = index < order.size - 1) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
              IconButton(onClick = { apply(order.mapIndexed { i, p -> if (i == index) p.copy(rot = (p.rot + 90) % 360) else p }) }) { Icon(Icons.Default.Rotate90DegreesCw, contentDescription = null, modifier = Modifier.size(18.dp)) }
              IconButton(onClick = { apply(order.mapIndexed { i, p -> if (i == index) p.copy(del = !p.del) else p }) }) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp)) }
            }
          }
        }
      }
    }
  }
}

private fun List<PageOrder>.swap(a: Int, b: Int): List<PageOrder> {
  if (a < 0 || b < 0 || a >= size || b >= size) return this
  return toMutableList().apply { val t = this[a]; this[a] = this[b]; this[b] = t }
}

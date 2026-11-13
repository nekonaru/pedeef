package com.nekonaru.pedeef.ui.tool

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.engine.Box as PdfBox
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.put
import com.nekonaru.pedeef.ui.kit.rememberPageCount
import com.nekonaru.pedeef.ui.kit.rememberPageImage
import com.nekonaru.pedeef.ui.kit.rememberPageSizes
import java.io.File
import kotlinx.serialization.json.decodeFromJsonElement

private fun normBox(a: Offset, b: Offset): PdfBox? {
  val l = minOf(a.x, b.x).coerceIn(0f, 1f)
  val t = minOf(a.y, b.y).coerceIn(0f, 1f)
  val r = maxOf(a.x, b.x).coerceIn(0f, 1f)
  val bo = maxOf(a.y, b.y).coerceIn(0f, 1f)
  if (r - l < 0.02f || bo - t < 0.02f) return null
  return listOf(l, t, r - l, bo - t)
}

@Composable
fun RedactEditor(bytes: ByteArray?, opts: Opts, cacheDir: File, onChange: (Opts) -> Unit) {
  val s = LocalPedeefStrings.current
  if (bytes == null) {
    Text(s.pickFiles, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    return
  }
  val sizes = rememberPageSizes(bytes, cacheDir) ?: return
  val count = sizes.size
  var page by remember(bytes) { mutableStateOf(0) }
  var dragStart by remember(page) { mutableStateOf<Offset?>(null) }
  var dragEnd by remember(page) { mutableStateOf<Offset?>(null) }

  val boxes: Map<String, List<PdfBox>> =
    opts["boxes"]?.let { runCatching { kotlinx.serialization.json.Json.decodeFromJsonElement<Map<String, List<PdfBox>>>(it) }.getOrNull() } ?: emptyMap()

  fun apply(next: Map<String, List<PdfBox>>) = onChange(opts.put("boxes", next))

  val pageBoxes = boxes[page.toString()].orEmpty()
  val image = rememberPageImage(bytes, page, 900, cacheDir)
  val ratio = sizes[page].let { it.first.toFloat() / it.second.toFloat() }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(count) { i ->
        val has = boxes[i.toString()].orEmpty().isNotEmpty()
        Surface(
          onClick = { page = i; dragStart = null; dragEnd = null },
          shape = RoundedCornerShape(10.dp),
          tonalElevation = if (i == page) 4.dp else 1.dp,
          color = if (i == page) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ) {
          Text("${i + 1}${if (has) " *" else ""}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
        }
      }
    }
    Text(
      if (s === com.nekonaru.pedeef.i18n.Id) "Seret di atas area yang akan dihitamkan. Ketuk kotak hitam untuk menghapusnya." else "Drag across the areas to black out. Tap a black box to remove it.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Box(
      Modifier
        .fillMaxWidth()
        .aspectRatio(ratio)
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        .pointerInput(page) {
          detectDragGestures(
            onDragStart = { dragStart = it; dragEnd = it },
            onDrag = { change, _ -> dragEnd = change.position },
            onDragEnd = {
              val a = dragStart; val b = dragEnd
              dragStart = null; dragEnd = null
              if (a != null && b != null) {
                val fb = normBox(
                  Offset(a.x / size.width.toFloat(), a.y / size.height.toFloat()),
                  Offset(b.x / size.width.toFloat(), b.y / size.height.toFloat()),
                )
                if (fb != null) apply(boxes + (page.toString() to (pageBoxes + listOf(fb))))
              }
            },
          )
        }
        .pointerInput(page, pageBoxes) {
          detectTapGestures(onTap = { tap ->
            val fx = tap.x / size.width.toFloat()
            val fy = tap.y / size.height.toFloat()
            val idx = pageBoxes.indexOfLast { b -> fx >= b[0] && fx <= b[0] + b[2] && fy >= b[1] && fy <= b[1] + b[3] }
            if (idx >= 0) {
              val newPageBoxes: List<PdfBox> = pageBoxes.filterIndexed { i2, _ -> i2 != idx }
              apply(boxes + (page.toString() to newPageBoxes))
            }
          })
        },
    ) {
      if (image != null) {
        Image(image, contentDescription = "Page ${page + 1}", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxWidth().aspectRatio(ratio))
      }
      Canvas(Modifier.fillMaxWidth().aspectRatio(ratio)) {
        for (b in pageBoxes) {
          drawRect(Color.Black.copy(alpha = 0.85f), topLeft = Offset(b[0] * size.width, b[1] * size.height), size = Size(b[2] * size.width, b[3] * size.height))
        }
        val a = dragStart; val b = dragEnd
        if (a != null && b != null) {
          val l = minOf(a.x, b.x); val t = minOf(a.y, b.y)
          drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(l, t), size = Size(maxOf(a.x, b.x) - l, maxOf(a.y, b.y) - t))
        }
      }
    }
    Row {
      OutlinedButton(onClick = { apply(boxes + (page.toString() to emptyList())) }) { Text(if (s === com.nekonaru.pedeef.i18n.Id) "Hapus halaman ini" else "Clear page") }
      Spacer(Modifier.size(8.dp))
      OutlinedButton(onClick = { apply(emptyMap()) }) { Text(if (s === com.nekonaru.pedeef.i18n.Id) "Hapus semua" else "Clear all") }
    }
  }
}

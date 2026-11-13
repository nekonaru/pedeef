package com.nekonaru.pedeef.ui.tool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nekonaru.pedeef.tools.str
import com.nekonaru.pedeef.ui.kit.rememberPageImage
import com.nekonaru.pedeef.ui.kit.rememberPageSizes
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

private fun normBox(a: Offset, b: Offset): PdfBox? {
  val l = minOf(a.x, b.x).coerceIn(0f, 1f)
  val t = minOf(a.y, b.y).coerceIn(0f, 1f)
  val r = maxOf(a.x, b.x).coerceIn(0f, 1f)
  val bo = maxOf(a.y, b.y).coerceIn(0f, 1f)
  if (r - l < 0.05f || bo - t < 0.05f) return null
  return listOf(l, t, r - l, bo - t)
}

@Composable
fun CropEditor(bytes: ByteArray?, opts: Opts, cacheDir: File, onChange: (Opts) -> Unit) {
  val s = LocalPedeefStrings.current
  if (bytes == null || opts.str("mode") == "resize") return // the resize option speaks for itself
  val sizes = rememberPageSizes(bytes, cacheDir) ?: return
  val count = sizes.size
  var page by remember(bytes) { mutableStateOf(0) }
  var dragStart by remember(page) { mutableStateOf<Offset?>(null) }
  var dragEnd by remember(page) { mutableStateOf<Offset?>(null) }

  val boxes: Map<String, PdfBox> =
    opts["boxes"]?.let { runCatching { kotlinx.serialization.json.Json.decodeFromJsonElement<Map<String, PdfBox>>(it) }.getOrNull() } ?: emptyMap()

  fun apply(next: Map<String, PdfBox>) = onChange(opts.put("boxes", next))

  val image = rememberPageImage(bytes, page, 900, cacheDir)
  val ratio = sizes[page].let { it.first.toFloat() / it.second.toFloat() }
  val indonesian = s === com.nekonaru.pedeef.i18n.Id

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(count) { i ->
        val has = boxes[i.toString()] != null
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
      if (indonesian) "Seret untuk memilih area yang tetap ada di setiap halaman." else "Drag to choose the area to keep. Add the same crop to every page with one tap.",
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
                if (fb != null) apply(boxes + (page.toString() to fb))
              }
            },
          )
        },
    ) {
      if (image != null) {
        Image(image, contentDescription = "Page ${page + 1}", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxWidth().aspectRatio(ratio))
      }
      Canvas(Modifier.fillMaxWidth().aspectRatio(ratio)) {
        val b = boxes[page.toString()]
        if (b != null) {
          drawRect(Color.Black.copy(alpha = 0.45f), topLeft = Offset(0f, 0f), size = Size(b[0] * size.width, size.height))
          drawRect(Color.Black.copy(alpha = 0.45f), topLeft = Offset((b[0] + b[2]) * size.width, 0f), size = Size(size.width - (b[0] + b[2]) * size.width, size.height))
          drawRect(Color.Black.copy(alpha = 0.45f), topLeft = Offset(b[0] * size.width, 0f), size = Size(b[2] * size.width, b[1] * size.height))
          drawRect(Color.Black.copy(alpha = 0.45f), topLeft = Offset(b[0] * size.width, (b[1] + b[3]) * size.height), size = Size(b[2] * size.width, size.height - (b[1] + b[3]) * size.height))
        }
        val a = dragStart; val e = dragEnd
        if (a != null && e != null) {
          val l = minOf(a.x, e.x); val t = minOf(a.y, e.y)
          drawRect(Color.Black.copy(alpha = 0.3f), topLeft = Offset(l, t), size = Size(maxOf(a.x, e.x) - l, maxOf(a.y, e.y) - t))
        }
      }
    }
    Row {
      OutlinedButton(onClick = { if (boxes[page.toString()] != null) apply((0 until count).associate { it.toString() to boxes[page.toString()]!! }) }) {
        Text(if (indonesian) "Terapkan ke semua" else "Apply to all")
      }
      Spacer(Modifier.size(8.dp))
      OutlinedButton(onClick = { apply(boxes - page.toString()) }) { Text(if (indonesian) "Atur ulang" else "Reset page") }
    }
  }
}

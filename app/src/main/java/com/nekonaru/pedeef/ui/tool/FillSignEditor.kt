package com.nekonaru.pedeef.ui.tool

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.engine.SignItem
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.put
import com.nekonaru.pedeef.ui.kit.rememberPageImage
import com.nekonaru.pedeef.ui.kit.rememberPageSizes
import java.io.File
import java.util.UUID
import kotlinx.serialization.json.decodeFromJsonElement

private val TEXT_COLORS = listOf("#0F172A", "#1D4ED8", "#B91C1C", "#15803D")

@Composable
fun FillSignEditor(bytes: ByteArray?, opts: Opts, cacheDir: File, onChange: (Opts) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === com.nekonaru.pedeef.i18n.Id
  val context = LocalContext.current
  if (bytes == null) {
    Text(s.pickFiles, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    return
  }
  val sizes = rememberPageSizes(bytes, cacheDir) ?: return
  val count = sizes.size
  var page by remember(bytes) { mutableStateOf(0) }
  var pendingKind by remember { mutableStateOf<String?>(null) } // text | signature | picture
  var pendingImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
  var pendingUri by remember { mutableStateOf<String?>(null) }
  var pendingText by remember { mutableStateOf("") }
  var showTextDialog by remember { mutableStateOf(false) }
  var showSignaturePad by remember { mutableStateOf(false) }

  val items: List<SignItem> = opts["items"]?.let { runCatching { kotlinx.serialization.json.Json.decodeFromJsonElement<List<SignItem>>(it) }.getOrNull() } ?: emptyList()

  fun apply(next: List<SignItem>) = onChange(opts.put("items", next))

  fun addItem(item: SignItem) = apply(items + item)

  val pageItems = items.filter { it.page == page }
  val image = rememberPageImage(bytes, page, 900, cacheDir)
  val ratio = sizes[page].let { it.first.toFloat() / it.second.toFloat() }

  fun saveTempImage(b: android.graphics.Bitmap): String {
    val dir = File(cacheDir, "sign-images").apply { mkdirs() }
    val f = File(dir, "item-${UUID.randomUUID()}.png")
    f.outputStream().use { out -> b.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
    return f.absolutePath
  }

  val picturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    val b = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    if (b != null) {
      pendingImage = b
      pendingUri = saveTempImage(b)
      pendingKind = "picture"
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(count) { i ->
        val n = items.count { it.page == i }
        Surface(
          onClick = { page = i; pendingKind = null },
          shape = RoundedCornerShape(10.dp),
          tonalElevation = if (i == page) 4.dp else 1.dp,
          color = if (i == page) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ) {
          Text("${i + 1}${if (n > 0) " *" else ""}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilledTonalButton(onClick = { showTextDialog = true }) { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)); Text(if (indonesian) " Teks" else " Text") }
      FilledTonalButton(onClick = { showSignaturePad = true }) { Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp)); Text(if (indonesian) " TTD" else " Sign") }
      FilledTonalButton(onClick = { picturePicker.launch("image/*") }) { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)); Text(if (indonesian) " Gambar" else " Picture") }
    }
    if (pendingKind != null) {
      Text(
        if (indonesian) "Ketuk di halaman untuk menaruh ${if (pendingKind == "text") "teks" else "gambar"}." else "Tap the page to place the ${if (pendingKind == "text") "text" else "picture"}.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
      )
    }

    Box(
      Modifier
        .fillMaxWidth()
        .aspectRatio(ratio)
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        .pointerInput(page, pendingKind) {
          detectTapGestures(onTap = { tap ->
            val fx = (tap.x / size.width.toFloat()).coerceIn(0f, 1f)
            val fy = (tap.y / size.height.toFloat()).coerceIn(0f, 1f)
            when (pendingKind) {
              "text" -> if (pendingText.isNotBlank()) {
                addItem(SignItem(page = page, kind = "text", x = fx, y = fy, size = 0.032f, text = pendingText, color = TEXT_COLORS[0]))
                pendingKind = null
              }
              "signature", "picture" -> pendingImage?.let { b ->
                val w = if (pendingKind == "signature") 0.30f else 0.35f
                val h = w * b.height / b.width * ratio
                addItem(SignItem(page = page, kind = "image", x = (fx - w / 2).coerceIn(0f, 1f), y = (fy - h / 2).coerceIn(0f, 1f), w = w, h = h.coerceIn(0.03f, 0.8f), uri = pendingUri ?: ""))
                pendingKind = null
              }
            }
          })
        },
    ) {
      if (image != null) {
        Image(image, contentDescription = "Page ${page + 1}", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxWidth().aspectRatio(ratio))
      }
      BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(ratio)) {
        for (item in pageItems) ItemOverlay(item, this@BoxWithConstraints.maxWidth, this@BoxWithConstraints.maxHeight, cacheDir)
      }
    }

    for ((index, item) in items.withIndex()) {
      if (item.page != page) continue
      Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
          "${if (item.kind == "text") "T" else "IMG"}: ${item.text.ifEmpty { "image" }}  p.${item.page + 1}",
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          modifier = Modifier.weight(1f),
        )
        TextButton(onClick = {
          apply(items.mapIndexed { i, it ->
            if (i != index) it
            else if (it.kind == "text") it.copy(size = (it.size / 1.25f).coerceAtLeast(0.008f)) else it.copy(w = (it.w / 1.2f).coerceAtLeast(0.05f), h = (it.h / 1.2f).coerceAtLeast(0.02f))
          })
        }) { Text("A-") }
        TextButton(onClick = {
          apply(items.mapIndexed { i, it ->
            if (i != index) it
            else if (it.kind == "text") it.copy(size = (it.size * 1.25f).coerceAtMost(0.1f)) else it.copy(w = (it.w * 1.2f).coerceAtMost(0.9f), h = (it.h * 1.2f).coerceAtMost(0.9f))
          })
        }) { Text("A+") }
        TextButton(onClick = { apply(items.mapIndexed { i, it -> if (i == index) it.copy(angle = (it.angle + 15f) % 360f) else it }) }) { Text("15°") }
        TextButton(onClick = {
          apply(items.mapIndexed { i, it ->
            if (i == index && it.kind == "text") {
              val next = TEXT_COLORS[(TEXT_COLORS.indexOf(it.color).let { c -> if (c < 0) 0 else (c + 1) % TEXT_COLORS.size })]
              it.copy(color = next)
            } else it
          })
        }) { Text("Color", color = if (item.color != null) parseHex(item.color) else MaterialTheme.colorScheme.onSurface) }
        androidx.compose.material3.IconButton(onClick = { apply(items.filterIndexed { i, _ -> i != index }) }) {
          Icon(Icons.Default.Delete, contentDescription = null)
        }
      }
    }
    if (items.isNotEmpty()) {
      OutlinedButton(onClick = { apply(emptyList()) }) { Text(if (indonesian) "Hapus semua" else "Clear all") }
    }
  }

  if (showTextDialog) {
    AlertDialog(
      onDismissRequest = { showTextDialog = false },
      title = { Text(if (indonesian) "Teks yang akan ditaruh" else "Text to place") },
      text = { OutlinedTextField(value = pendingText, onValueChange = { pendingText = it }, singleLine = true) },
      confirmButton = {
        TextButton(onClick = {
          if (pendingText.isNotBlank()) { pendingKind = "text"; showTextDialog = false }
        }) { Text("OK") }
      },
      dismissButton = { TextButton(onClick = { showTextDialog = false }) { Text(if (indonesian) "Batal" else "Cancel") } },
    )
  }

  if (showSignaturePad) {
    SignaturePad(
      onDone = { bitmap ->
        pendingImage = bitmap
        pendingUri = saveTempImage(bitmap)
        pendingKind = "signature"
        showSignaturePad = false
      },
      onDismiss = { showSignaturePad = false },
      indonesian = indonesian,
    )
  }
}

private fun parseHex(hex: String?): Color =
  try { Color(android.graphics.Color.parseColor(hex ?: "#0F172A")) } catch (_: Exception) { Color(0xFF0F172A) }

@Composable
private fun ItemOverlay(item: SignItem, boxWidth: androidx.compose.ui.unit.Dp, boxHeight: androidx.compose.ui.unit.Dp, cacheDir: File) {
  val x = boxWidth * item.x
  val y = boxHeight * item.y
  if (item.kind == "text") {
    Text(
      item.text,
      color = parseHex(item.color),
      fontSize = (item.size * boxHeight.value).sp,
      modifier = Modifier
        .offset(x, y)
        .rotate(item.angle),
    )
    return
  }
  val uri = item.uri
  val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, uri) {
    value = null
    val f = File(uri)
    if (f.exists()) value = BitmapFactory.decodeFile(f.absolutePath)
  }
  val bmp = bitmap
  if (bmp != null) {
    Image(
      bmp.asImageBitmap(),
      contentDescription = null,
      modifier = Modifier
        .offset(x, y)
        .size(boxWidth * item.w, boxHeight * item.h)
        .rotate(item.angle)
        .alpha(item.opacity),
    )
  }
}


@Composable
private fun SignaturePad(onDone: (android.graphics.Bitmap) -> Unit, onDismiss: () -> Unit, indonesian: Boolean) {
  var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (indonesian) "Gambar tanda tangan" else "Draw your signature") },
    text = {
      Box(
        Modifier
          .fillMaxWidth()
          .size(width = 280.dp, height = 160.dp)
          .background(Color.White, RoundedCornerShape(8.dp))
          .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = { strokes = strokes + listOf(listOf(it)) },
              onDrag = { change, _ -> strokes = strokes.dropLast(1) + listOf(strokes.last() + change.position) },
            )
          },
      ) {
        Canvas(Modifier.fillMaxWidth()) {
          for (stroke in strokes) {
            for (i in 1 until stroke.size) {
              drawLine(Color(0xFF0F172A), stroke[i - 1], stroke[i], strokeWidth = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        if (strokes.isEmpty()) { onDismiss(); return@TextButton }
        val maxX = strokes.flatten().maxOf { it.x }.coerceAtLeast(1f)
        val maxY = strokes.flatten().maxOf { it.y }.coerceAtLeast(1f)
        val bmp = android.graphics.Bitmap.createBitmap(720, 360, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        val paint = android.graphics.Paint().apply {
          color = android.graphics.Color.parseColor("#0F172A"); strokeWidth = 10f
          style = android.graphics.Paint.Style.STROKE; isAntiAlias = true; strokeCap = android.graphics.Paint.Cap.ROUND
        }
        val kx = 720f / maxX; val ky = 360f / maxY
        for (stroke in strokes) {
          for (i in 1 until stroke.size) {
            canvas.drawLine(stroke[i - 1].x * kx, stroke[i - 1].y * ky, stroke[i].x * kx, stroke[i].y * ky, paint)
          }
        }
        onDone(bmp)
      }) { Text(if (indonesian) "Selesai" else "Done") }
    },
    dismissButton = {
      TextButton(onClick = { strokes = emptyList() }) { Text(if (indonesian) "Hapus" else "Clear") }
    },
  )
}

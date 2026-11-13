package com.nekonaru.pedeef.ui.tool

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.engine.Output
import com.nekonaru.pedeef.engine.UserError
import com.nekonaru.pedeef.engine.mentionsEncrypt
import com.nekonaru.pedeef.engine.unlockBytes
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.Input
import com.nekonaru.pedeef.tools.Tool
import com.nekonaru.pedeef.tools.toolById
import com.nekonaru.pedeef.ui.kit.Btn
import com.nekonaru.pedeef.ui.kit.PasswordDialog
import com.nekonaru.pedeef.ui.kit.Options
import com.nekonaru.pedeef.ui.kit.Spinner
import com.nekonaru.pedeef.ui.kit.fmtSize
import com.nekonaru.pedeef.tools.defaults
import com.nekonaru.pedeef.tools.str
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PickedFile(val name: String, val bytes: ByteArray)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(toolId: String, onBack: () -> Unit, onOpenViewer: (ByteArray, String) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val context = LocalContext.current
  val tool = toolById(toolId)
  if (tool == null) {
    onBack()
    return
  }
  val scope = rememberCoroutineScope()

  var picked by remember { mutableStateOf<List<PickedFile>>(emptyList()) }
  var opts by remember { mutableStateOf(defaults(tool.opts)) }
  var busy by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var results by remember { mutableStateOf<List<Output>?>(null) }
  var savedMessage by remember { mutableStateOf<String?>(null) }
  var locked by remember { mutableStateOf<List<Pair<String, ByteArray>>?>(null) } // queued locked PDFs: name to bytes
  var unlockError by remember { mutableStateOf<String?>(null) }

  androidx.compose.runtime.LaunchedEffect(picked) {
    if (tool.id == "metadata") {
      val f = picked.firstOrNull() ?: return@LaunchedEffect
      if (listOf("title", "author", "subject", "keywords").all { opts.str(it).isBlank() }) {
        runCatching { com.nekonaru.pedeef.engine.readDetails(f.bytes) }.getOrNull()?.let { d ->
          opts = kotlinx.serialization.json.JsonObject(
            mapOf(
              "title" to kotlinx.serialization.json.JsonPrimitive(d["title"].orEmpty()),
              "author" to kotlinx.serialization.json.JsonPrimitive(d["author"].orEmpty()),
              "subject" to kotlinx.serialization.json.JsonPrimitive(d["subject"].orEmpty()),
              "keywords" to kotlinx.serialization.json.JsonPrimitive(d["keywords"].orEmpty()),
            ),
          )
        }
      }
    }
  }

  fun reset() {
    picked = emptyList()
    opts = defaults(tool.opts)
    results = null
    error = null
    savedMessage = null
  }

  fun addFiles(new: List<PickedFile>) {
    val images = tool.input == Input.Images
    val next = if (images || tool.input == Input.Pdfs) picked + new else new.take(1)
    picked = next
    results = null
  }

  val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
    scope.launch {
      val out = withContext(Dispatchers.IO) { uris.mapNotNull { readUri(context, it) } }
      // Anything asking for a password is queued: decrypt on this thread, ask one at a time.
      val plain = out.filter { !mentionsEncrypt(it.bytes) }
      val secret = out.filter { mentionsEncrypt(it.bytes) }
      if (plain.isNotEmpty()) addFiles(plain)
      if (secret.isNotEmpty()) locked = (locked ?: emptyList()) + secret.map { it.name to it.bytes }
    }
  }
  val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
    scope.launch {
      val out = withContext(Dispatchers.IO) { uris.mapNotNull { readUri(context, it) } }
      if (out.isNotEmpty()) addFiles(out)
    }
  }

  fun pick() = when (tool.input) {
    Input.Images -> imagePicker.launch("image/*")
    else -> pdfPicker.launch("application/pdf")
  }

  fun unlockNext(password: String) {
    val q = locked ?: return
    if (q.isEmpty()) { locked = null; unlockError = null; return }
    val (name, bytes) = q.first()
    val unlocked = unlockBytes(bytes, password)
    if (unlocked == null) {
      unlockError = s.passwordWrong
      return
    }
    unlockError = null
    addFiles(listOf(PickedFile(name.replace(Regex("\\.pdf$", RegexOption.IGNORE_CASE), "") + ".pdf", unlocked)))
    locked = q.drop(1).ifEmpty { null }
  }

  fun run() {
    if (picked.isEmpty()) { error = s.pickFiles; return }
    val total = picked.sumOf { it.bytes.size.toLong() }
    if (total > 256L * 1024 * 1024) { error = s.fileTooLarge; return }
    scope.launch {
      busy = true
      error = null
      savedMessage = null
      try {
        val outs = withContext(Dispatchers.Default) {
          when (tool.input) {
            Input.Images, Input.Pdfs -> tool.run(picked.map { com.nekonaru.pedeef.engine.Named(it.name, it.bytes) }, opts)
            Input.Pdf -> picked.flatMap { f -> tool.run(listOf(com.nekonaru.pedeef.engine.Named(f.name, f.bytes)), opts) }
          }
        }
        results = outs
      } catch (e: UserError) {
        error = e.message
      } catch (e: Exception) {
        error = s.genericError
      } catch (e: Throwable) {
        // e.g. OutOfMemoryError on a hostile file
        error = s.genericError
      } finally {
        busy = false
      }
    }
  }

  fun saveAll() {
    val outs = results ?: return
    scope.launch {
      val root = FilesStore.root(context)
      var saved = 0
      for (o in outs) {
        val base = FilesStore.sanitizeName(o.name.removeSuffix(".pdf").ifEmpty { "output" })
        FilesStore.saveBytes(root, null, base, o.ext, o.bytes)
        saved++
      }
      savedMessage = "${s.savedTo}: $saved"
    }
  }

  fun share(o: Output) {
    scope.launch {
      val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        File(dir, FilesStore.sanitizeName(o.name) + "." + o.ext).apply { writeBytes(o.bytes) }
      }
      val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.files", file)
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = FilesStore.mimeOf(file.name)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, null))
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(tool.title(indonesian), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) } },
      )
    },
  ) { padding ->
    Column(
      Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(tool.desc(indonesian), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

      // Step 1: pick files
      when {
        picked.isEmpty() -> OutlinedButton(onClick = { pick() }, modifier = Modifier.fillMaxWidth()) {
          Icon(if (tool.input == Input.Images) Icons.Default.Image else Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
          Text(if (tool.input == Input.Images) s.pickImages else s.pickFiles, Modifier.padding(start = 8.dp))
        }
        else -> {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              "${picked.size} " + if (tool.input == Input.Images) (if (indonesian) "gambar dipilih" else "images selected") else (if (indonesian) "PDF dipilih" else "PDFs selected"),
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.weight(1f),
            )
            TextButtonLike(onClick = { pick() }, label = if (tool.input == Input.Pdf) s.pickFiles else s.pickImages)
            IconButton(onClick = { reset() }) { Icon(Icons.Default.Close, contentDescription = s.cancel) }
          }
          picked.forEachIndexed { i, f ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
              if (picked.size > 1 && tool.input != Input.Pdf) {
                // Reorder controls for multi-file tools (merge, mix, zip, workflow).
                IconButton(onClick = { picked = picked.swap(i, i - 1) }, enabled = i > 0) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
                IconButton(onClick = { picked = picked.swap(i, i + 1) }, enabled = i < picked.size - 1) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) }
              }
              if (mentionsEncrypt(f.bytes)) Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Text(f.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 4.dp))
              Text(fmtSize(f.bytes.size.toLong()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              IconButton(onClick = { picked = picked.filterIndexed { index, _ -> index != i }; results = null }) { Icon(Icons.Default.Close, contentDescription = s.delete) }
            }
          }
        }
      }

      // Step 2: tool-specific editor or generic options
      when (tool.id) {
        "organize" -> OrganizeEditor(picked.firstOrNull()?.bytes, opts, com.nekonaru.pedeef.tools.ToolContext.cacheDir) { opts = it }
        "redact" -> RedactEditor(picked.firstOrNull()?.bytes, opts, com.nekonaru.pedeef.tools.ToolContext.cacheDir) { opts = it }
        "fillSign" -> FillSignEditor(picked.firstOrNull()?.bytes, opts, com.nekonaru.pedeef.tools.ToolContext.cacheDir) { opts = it }
        "crop" -> Column { Options(tool.opts, opts, indonesian) { opts = it }; if (opts.str("mode") != "resize") CropEditor(picked.firstOrNull()?.bytes, opts, com.nekonaru.pedeef.tools.ToolContext.cacheDir) { opts = it } }
        "workflow" -> WorkflowEditor(opts) { opts = it }
        else -> if (tool.opts.isNotEmpty()) Options(tool.opts, opts, indonesian) { opts = it }
      }

      if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)

      // CTA
      Btn(
        text = tool.cta(indonesian),
        onClick = { run() },
        enabled = picked.isNotEmpty(),
        busy = busy,
        modifier = Modifier.fillMaxWidth(),
      )

      // Results
      results?.let { outs ->
        Text(s.resultReady, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        for (o in outs) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Icon(if (o.ext == "pdf") Icons.Default.Article else if (o.ext == "jpg" || o.ext == "png") Icons.Default.Image else Icons.Default.Article, contentDescription = null, modifier = Modifier.size(18.dp))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
              Text(o.name + "." + o.ext, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(fmtSize(o.bytes.size.toLong()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (o.ext == "pdf") {
              TextButtonLike(onClick = { onOpenViewer(o.bytes, o.name + ".pdf") }, label = s.openResult)
            }
            IconButton(onClick = { share(o) }) { Icon(Icons.Default.Share, contentDescription = s.share) }
          }
        }
        Btn(text = s.save + " (" + outs.size + ")", onClick = { saveAll() }, modifier = Modifier.fillMaxWidth())
        savedMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
      }

      Box(Modifier.padding(bottom = 32.dp))
    }

    // Password prompt for locked PDFs
    locked?.firstOrNull()?.let { (name, _) ->
      PasswordDialog(
        title = s.passwordPrompt + " " + name,
        error = unlockError,
        onSubmit = { unlockNext(it) },
        onDismiss = { locked = null; unlockError = null },
      )
    }
  }
}

@Composable
private fun TextButtonLike(onClick: () -> Unit, label: String) {
  OutlinedButton(onClick = onClick, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(10.dp)) { Text(label) }
}



private fun List<PickedFile>.swap(a: Int, b: Int): List<PickedFile> {
  if (a < 0 || b < 0 || a >= size || b >= size) return this
  return toMutableList().apply { val t = this[a]; this[a] = this[b]; this[b] = t }
}

private fun readUri(context: android.content.Context, uri: Uri): PickedFile? {
  return try {
    val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
      val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
      if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
    } ?: uri.lastPathSegment ?: "file"
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    PickedFile(name, bytes)
  } catch (_: Exception) {
    null
  }
}

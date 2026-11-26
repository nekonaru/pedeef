package com.nekonaru.pedeef.ui.batch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.Presets
import com.nekonaru.pedeef.data.PublicFiles
import com.nekonaru.pedeef.engine.Named
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.toolById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(onBack: () -> Unit) {
  val s = LocalPedeefStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var presets by remember { mutableStateOf(Presets.load(context)) }
  var selectedId by remember { mutableStateOf<String?>(null) }
  var expanded by remember { mutableStateOf(false) }
  var progress by remember { mutableStateOf("") }
  var done by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { presets = Presets.load(context) }
  val selected = presets.firstOrNull { it.id == selectedId }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(s.batchRun) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) } },
      )
    },
  ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
          value = selected?.name ?: s.batchSelect,
          onValueChange = {},
          readOnly = true,
          modifier = Modifier.fillMaxWidth().menuAnchor(),
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          presets.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedId = p.id; expanded = false }) }
        }
      }
      Button(
        onClick = {
          val preset = selected ?: return@Button
          scope.launch {
            done = false
            progress = s.batchProgress
            val result = withContext(Dispatchers.IO) {
              runBatch(context, preset.id, preset.opts.toString())
            }
            progress = result
            done = true
          }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        enabled = selected != null,
      ) { Text(s.batchRun) }
      if (progress.isNotEmpty()) Text(progress, Modifier.padding(top = 12.dp))
      if (done) Text(s.batchDone, Modifier.padding(top = 8.dp))
    }
  }
}

private fun runBatch(context: android.content.Context, presetId: String, optsJson: String): String {
  val preset = Presets.load(context).firstOrNull { it.id == presetId } ?: return "Preset missing"
  val tool = toolById(preset.toolId) ?: return "Tool missing"
  if (tool.min > 1 || tool.input == com.nekonaru.pedeef.tools.Input.Any || tool.input == com.nekonaru.pedeef.tools.Input.Images || tool.input == com.nekonaru.pedeef.tools.Input.None) {
    return "This preset needs manual file selection and cannot run in batch"
  }
  val root = PublicFiles.root(context) ?: FilesStore.root(context)
  val files = root.walkTopDown().filter {
    it.isFile && it.extension.equals("pdf", true) && !it.path.contains(java.io.File.separator + "Batches" + java.io.File.separator)
  }.take(100).toList()
  if (files.isEmpty()) return "No PDFs found"
  val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US).format(java.util.Date())
  val outDir = File(root, "Batches/$stamp").apply { mkdirs() }
  var saved = 0
  var failed = 0
  for (file in files) {
    try {
      val outs = tool.run(listOf(Named(file.name, file.readBytes())), preset.opts)
      for (out in outs) {
        val name = out.name.ifBlank { "output" }
        val base = FilesStore.sanitizeName(name.removeSuffix(".${out.ext}").ifEmpty { "output" })
        FilesStore.saveBytes(root, outDir, base, out.ext, out.bytes)
        saved++
      }
    } catch (_: Throwable) {
      failed++
    }
  }
  val report = "Batch: ${preset.name}\nFiles: ${files.size}\nSaved: $saved\nFailed: $failed\n"
  File(outDir, "report.txt").writeText(report)
  return "Files: ${files.size}, saved: $saved, failed: $failed"
}

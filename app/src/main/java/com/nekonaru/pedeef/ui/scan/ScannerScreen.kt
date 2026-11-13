package com.nekonaru.pedeef.ui.scan

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.engine.imagesToPdf
import com.nekonaru.pedeef.engine.ocrPdf
import com.nekonaru.pedeef.tools.ToolContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(onBack: () -> Unit, onOpenViewer: (ByteArray, String) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === com.nekonaru.pedeef.i18n.Id
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val root = remember { FilesStore.root(context) }

  var busy by remember { mutableStateOf(false) }
  var busyLabel by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }
  var savedName by remember { mutableStateOf<String?>(null) }
  var savedBytes by remember { mutableStateOf<ByteArray?>(null) }
  var makeSearchable by remember { mutableStateOf(true) }

  fun finish(bytes: ByteArray, baseName: String) {
    scope.launch {
      busy = true
      busyLabel = s.buildingPdf
      var out = bytes
      if (makeSearchable) {
        busyLabel = s.ocrInProgress
        out = withContext(Dispatchers.Default) { runCatching { ocrPdf(bytes, ToolContext.cacheDir) }.getOrDefault(bytes) }
      }
      val file = withContext(Dispatchers.IO) { FilesStore.saveBytes(root, null, baseName, "pdf", out) }
      savedName = file.name
      savedBytes = out
      busy = false
    }
  }

  val scannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
    val scan = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
    if (result.resultCode != android.app.Activity.RESULT_OK || scan == null) {
      // User cancelled or the scanner is unavailable; nothing to do.
      return@rememberLauncherForActivityResult
    }
    val pdfUri = scan.pdf?.uri
    scope.launch {
      busy = true
      busyLabel = s.buildingPdf
      error = null
      try {
        val bytes = withContext(Dispatchers.IO) { pdfUri?.let { context.contentResolver.openInputStream(it)?.use { i -> i.readBytes() } } }
        if (bytes == null) {
          error = s.scanFailed
        } else {
          finish(bytes, "scan-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()))
        }
      } catch (_: Exception) {
        error = s.scanFailed
      } finally {
        busy = false
      }
    }
  }

  val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
    if (uris.isEmpty()) return@rememberLauncherForActivityResult
    scope.launch {
      busy = true
      busyLabel = s.buildingPdf
      error = null
      try {
        val images = withContext(Dispatchers.IO) {
          uris.mapNotNull { uri ->
            context.contentResolver.openInputStream(uri)?.use { i ->
              val b = i.readBytes()
              val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && idx >= 0) c.getString(idx) else "image"
              } ?: "image"
              name to com.nekonaru.pedeef.engine.decodeScaled(b)
            }
          }
        }
        if (images.isEmpty()) error = s.scanFailed else finish(imagesToPdf(images), "scan-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()))
      } catch (_: Exception) {
        error = s.scanFailed
      } finally {
        busy = false
      }
    }
  }

  fun startScanner() {
    error = null
    val options = GmsDocumentScannerOptions.Builder()
      .setGalleryImportAllowed(false) // gallery import has its own button with our settings
      .setPageLimit(50)
      .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
      .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
      .build()
    try {
      val client = GmsDocumentScanning.getClient(options)
      client.getStartScanIntent(context as android.app.Activity)
        .addOnSuccessListener { sender -> scannerLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
        .addOnFailureListener { _ ->
          // The document scanner needs Play services; fall back to gallery import.
          error = s.playServicesMissing
        }
    } catch (_: Exception) {
      error = s.playServicesMissing
    }
  }

  Scaffold(
    topBar = { TopAppBar(title = { Text(s.scan) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) } }) },
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) {
      if (busy) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
          CircularProgressIndicator()
          Text(busyLabel, modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return@Box
      }
      if (savedName != null && savedBytes != null) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
          androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp),
          ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
            }
          }
          Text(if (indonesian) "Pindai tersimpan" else "Scan saved", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
          Text(savedName!!, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 20.dp)) {
            Button(onClick = { onOpenViewer(savedBytes!!, savedName!!) }) { Text(s.open) }
            androidx.compose.material3.TextButton(onClick = onBack) { Text(s.done) }
          }
        }
        return@Box
      }

      Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          if (indonesian) "Pindai dokumen dengan kamera atau impor gambar dari galeri. Semuanya diproses di ponselmu." else "Scan a document with the camera, or import images from the gallery. Everything runs on your phone.",
          style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = { startScanner() }, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(20.dp))
          Text(if (indonesian) "  Mulai memindai" else "  Start scan")
        }
        androidx.compose.material3.OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
          Text(if (indonesian) "  Impor dari galeri" else "  Import from gallery")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Text(s.makeSearchable, modifier = Modifier.weight(1f))
          Switch(checked = makeSearchable, onCheckedChange = { makeSearchable = it })
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
      }
    }
  }
}

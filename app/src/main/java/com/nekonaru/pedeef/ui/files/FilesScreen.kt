package com.nekonaru.pedeef.ui.files

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.FileEntry
import com.nekonaru.pedeef.data.PublicFiles
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.ToolContext
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private fun relPath(root: File, dir: File): String {
  val r = root.canonicalFile.path
  val d = dir.canonicalFile.path
  if (d == r) return ""
  return d.removePrefix(r + File.separator).replace(File.separatorChar, '/')
}

private fun iconFor(entry: FileEntry) = when {
  entry.isDir -> Icons.Default.Folder
  entry.name.endsWith(".pdf", true) -> Icons.Default.Article
  Regex("\\.(jpg|jpeg|png)$", RegexOption.IGNORE_CASE).matches(entry.name) -> Icons.Default.Image
  else -> Icons.Default.InsertDriveFile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(onOpenViewer: (ByteArray, String) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === com.nekonaru.pedeef.i18n.Id
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val privateRoot = remember { FilesStore.root(context) }
  var granted by remember { mutableStateOf(PublicFiles.hasAccess(context)) }
  var usePrivate by remember { mutableStateOf(false) }
  val root = remember(granted, usePrivate) {
    if (granted && !usePrivate) PublicFiles.root(context) ?: privateRoot else privateRoot
  }
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    granted = PublicFiles.hasAccess(context)
    if (granted) PublicFiles.migrateOnce(context)
  }
  val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { grantedPerm ->
    granted = grantedPerm || PublicFiles.hasAccess(context)
    if (granted) PublicFiles.migrateOnce(context)
  }

  var currentDir by remember(root) { mutableStateOf(root) }
  var query by remember { mutableStateOf("") }
  var refreshTick by remember { mutableStateOf(0) }
  var showNewFolder by remember { mutableStateOf(false) }
  var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
  var renameText by remember { mutableStateOf("") }
  var deleteTarget by remember { mutableStateOf<FileEntry?>(null) }
  var moveTarget by remember { mutableStateOf<FileEntry?>(null) }
  var busy by remember { mutableStateOf(false) }

  val items = remember(currentDir, query, refreshTick) {
    if (query.isBlank()) FilesStore.list(currentDir) else FilesStore.search(root, query)
  }

  fun shareFile(entry: FileEntry) {
    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.files", entry.file)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = FilesStore.mimeOf(entry.name)
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
  }

  fun exportCopy(entry: FileEntry, target: Uri) {
    scope.launch {
      busy = true
      withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(target)?.use { out -> out.write(entry.file.readBytes()) }
      }
      busy = false
    }
  }

  var exportTargetHolder by remember { mutableStateOf<FileEntry?>(null) }
  val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    androidx.activity.result.contract.ActivityResultContracts.CreateDocument("*/*"),
  ) { uri -> uri?.let { u -> exportTargetHolder?.let { entry -> exportCopy(entry, u) } } }

  fun openEntry(entry: FileEntry) {
    if (entry.isDir) { currentDir = entry.file; query = ""; return }
    if (entry.name.endsWith(".pdf", true)) {
      busy = true
      scope.launch {
        val bytes = withContext(Dispatchers.IO) { runCatching { entry.file.readBytes() }.getOrNull() }
        busy = false
        if (bytes != null) onOpenViewer(bytes, entry.name)
      }
      return
    }
    shareFile(entry)
  }

  // Storage gate: without it, files land in Download/Pedeef and the user can find them.
  if (!granted && !usePrivate) {
    Scaffold(
      topBar = { TopAppBar(title = { Text(s.storageAccessTitle) }, navigationIcon = {}) },
    ) { padding ->
      Column(
        Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text(s.storageAccessTitle, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
        Text(s.storageAccessBody, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Text(s.storageAccessDenied, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))
        androidx.compose.material3.Button(
          onClick = {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
              PublicFiles.requestManageAllFilesAccess(context)
            } else {
              permLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(s.storageAccessButton) }
        TextButton(onClick = { usePrivate = true }) { Text(s.storagePrivateFallback) }
      }
    }
    return
  }

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          title = { Text(s.filesTitle) },
          navigationIcon = {
            if (currentDir != root) {
              IconButton(onClick = { currentDir = currentDir.parentFile ?: root }) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) }
            }
          },
          actions = {
            IconButton(onClick = { showNewFolder = true }) { Icon(Icons.Default.CreateNewFolder, contentDescription = s.newFolder) }
          },
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
          Text(
            if (currentDir == root) s.filesTitle else relPath(root, currentDir),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(if (indonesian) "Cari di semua folder" else "Search all folders") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        )
      }
    },
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) {
      if (busy) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return@Box
      }
      if (items.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(s.filesEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        }
        return@Box
      }
      LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(items.size) { i ->
          val entry = items[i]
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { openEntry(entry) }.padding(horizontal = 10.dp, vertical = 10.dp),
          ) {
            Icon(iconFor(entry), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
              Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
              Text(
                (if (entry.isDir) (FilesStore.countIn(entry.file).toString() + if (indonesian) " file" else " files") else com.nekonaru.pedeef.ui.kit.fmtSize(entry.size)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            IconButton(onClick = { shareFile(entry) }) { Icon(Icons.Default.Share, contentDescription = s.share) }
            var menu by remember(entry.file.path) { mutableStateOf(false) }
            Box {
              IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
              DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(s.rename) }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = { menu = false; renameTarget = entry; renameText = entry.name })
                DropdownMenuItem(text = { Text(s.moveTo) }, leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) }, onClick = { menu = false; moveTarget = entry })
                DropdownMenuItem(text = { Text(s.exportTo) }, leadingIcon = { Icon(Icons.Default.SaveAlt, contentDescription = null) }, onClick = { menu = false; exportTargetHolder = entry; exportLauncher.launch(entry.name) })
                DropdownMenuItem(text = { Text(s.delete) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, onClick = { menu = false; deleteTarget = entry })
              }
            }
          }
        }
      }
    }
  }

  // New folder dialog
  if (showNewFolder) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showNewFolder = false },
      title = { Text(s.newFolder) },
      text = { OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text(s.folderName) }, singleLine = true) },
      confirmButton = {
        TextButton(onClick = {
          runCatching { FilesStore.createFolder(root, currentDir, name) }
          showNewFolder = false
          refreshTick++
        }) { Text(s.ok) }
      },
      dismissButton = { TextButton(onClick = { showNewFolder = false }) { Text(s.cancel) } },
    )
  }

  // Rename dialog
  renameTarget?.let { entry ->
    AlertDialog(
      onDismissRequest = { renameTarget = null },
      title = { Text(s.rename) },
      text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true) },
      confirmButton = {
        TextButton(onClick = {
          runCatching { FilesStore.rename(root, entry, renameText) }
          renameTarget = null
          refreshTick++
        }) { Text(s.ok) }
      },
      dismissButton = { TextButton(onClick = { renameTarget = null }) { Text(s.cancel) } },
    )
  }

  // Delete confirmation
  deleteTarget?.let { entry ->
    com.nekonaru.pedeef.ui.kit.ConfirmDialog(
      title = s.delete,
      body = entry.name,
      confirm = s.delete,
      dismiss = s.cancel,
      danger = true,
      onConfirm = {
        FilesStore.delete(root, entry.file)
        deleteTarget = null
        refreshTick++
      },
      onDismiss = { deleteTarget = null },
    )
  }

  // Move to... dialog: pick a target folder
  moveTarget?.let { entry ->
    val folders = remember(refreshTick, entry) {
      root.walkTopDown().filter { it.isDirectory }.toList()
    }
    AlertDialog(
      onDismissRequest = { moveTarget = null },
      title = { Text(s.moveTo) },
      text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
          folders.forEach { f ->
            val rel = if (f == root) s.filesTitle else relPath(root, f).ifEmpty { s.filesTitle }
            Text(rel, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
              runCatching { FilesStore.moveTo(root, entry, f) }
              moveTarget = null
              refreshTick++
            }.padding(10.dp))
          }
        }
      },
      confirmButton = { TextButton(onClick = { moveTarget = null }) { Text(s.cancel) } },
    )
  }
}

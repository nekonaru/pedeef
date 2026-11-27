package com.nekonaru.pedeef.ui.files

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.produceState
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
import com.nekonaru.pedeef.ui.kit.LocalSnackbar
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
  // (language is handled by the localizer; no other per-locale branches here)
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val showMessage = LocalSnackbar.current ?: { _: String -> Unit }
  val privateRoot = remember { FilesStore.root(context) }
  var granted by remember { mutableStateOf(PublicFiles.hasAccess(context)) }
  var usePrivate by remember { mutableStateOf(false) }
  val root = remember(granted, usePrivate) {
    if (granted && !usePrivate) PublicFiles.root(context) ?: privateRoot else privateRoot
  }
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    granted = PublicFiles.hasAccess(context)
    if (granted) scope.launch(Dispatchers.IO) { PublicFiles.migrateOnceSuspended(context) }
  }
  val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { grantedPerm ->
    granted = grantedPerm || PublicFiles.hasAccess(context)
    if (granted) scope.launch(Dispatchers.IO) { PublicFiles.migrateOnceSuspended(context) }
  }

  var currentDir by remember(root) { mutableStateOf(root) }
  var query by remember { mutableStateOf("") }
  var refreshTick by remember { mutableStateOf(0) }
  var sort by remember { mutableStateOf("name") } // name | recent | size | extension
  var smart by remember { mutableStateOf("all") } // all | recent | starred | large | pdf | images
  var showBin by remember { mutableStateOf(false) }
  var showRename by remember { mutableStateOf(false) }
  var renamePattern by remember { mutableStateOf("document-{n}") }
  var selectionMode by remember { mutableStateOf(false) }
  var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
  var starred by remember { mutableStateOf(com.nekonaru.pedeef.data.Starred.load(context).toSet()) }
  var showNewFolder by remember { mutableStateOf(false) }
  var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
  var renameText by remember { mutableStateOf("") }
  var renameError by remember { mutableStateOf<String?>(null) }
  var deleteTarget by remember { mutableStateOf<FileEntry?>(null) }
  var moveTarget by remember { mutableStateOf<FileEntry?>(null) }
  var moveError by remember { mutableStateOf<String?>(null) }
  var busy by remember { mutableStateOf(false) }

  val items by produceState<List<FileEntry>?>(initialValue = null, currentDir, query, refreshTick, sort) {
    value = withContext(Dispatchers.IO) {
      val raw = if (query.isBlank()) FilesStore.list(currentDir) else FilesStore.search(root, query)
      when (sort) {
        "recent" -> raw.sortedWith(compareByDescending<FileEntry> { it.isDir }.thenByDescending { it.modified })
        "size" -> raw.sortedWith(compareByDescending<FileEntry> { it.isDir }.thenByDescending { it.size })
        "extension" -> raw.sortedWith(compareByDescending<FileEntry> { it.isDir }.thenBy { it.name.lowercase(java.util.Locale.getDefault()).substringAfterLast('.', "") }.thenBy { it.name.lowercase(java.util.Locale.getDefault()) })
        else -> raw.sortedWith(compareByDescending<FileEntry> { it.isDir }.thenBy { it.name.lowercase(java.util.Locale.getDefault()) })
      }
    }
  }

  val folders by produceState<List<java.io.File>>(initialValue = emptyList(), moveTarget, refreshTick) {
    value = withContext(Dispatchers.IO) {
      if (moveTarget == null) emptyList()
      else runCatching { root.walkTopDown().filter { it.isDirectory }.take(500).toList() }.getOrDefault(emptyList())
    }
  }

  fun shareFile(entry: FileEntry) {
    if (entry.isDir) return // folders do not get shared; sharing a folder would grant read access to its files
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
        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape).padding(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(s.storageAccessTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
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
          title = {
            Column {
              Text(if (selectionMode) "${selected.size} ${s.selected}" else s.filesTitle)
              val path = if (currentDir == root) "" else relPath(root, currentDir)
              if (!selectionMode && path.isNotEmpty()) {
                Text(path, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
            }
          },
          navigationIcon = {
            if (selectionMode) {
              IconButton(onClick = { selectionMode = false; selected = emptySet() }) { Icon(Icons.Default.Close, contentDescription = s.cancel) }
            } else if (currentDir != root) {
              IconButton(onClick = { currentDir = currentDir.parentFile ?: root }) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) }
            }
          },
          actions = {
            if (selectionMode) {
              IconButton(onClick = { selected = if (selected.size == items?.size) emptySet() else (items ?: emptyList()).map { it.file.absolutePath }.toSet() }) { Icon(Icons.Default.SelectAll, contentDescription = s.selectAll) }
              IconButton(onClick = {
                scope.launch(Dispatchers.IO) {
                  val removed = selected.flatMap { path ->
                    val target = java.io.File(path)
                    if (target.isDirectory) target.walkTopDown().filter { it.isFile }.map { it.absolutePath }.toList() else listOf(path)
                  }.toSet()
                  selected.forEach { path -> runCatching { java.io.File(path) }.getOrNull()?.let { com.nekonaru.pedeef.data.RecycleBin.trash(context, root, it) } }
                  val next = com.nekonaru.pedeef.data.Starred.remove(context, removed)
                  withContext(Dispatchers.Main) { starred = next.toSet(); selectionMode = false; selected = emptySet(); refreshTick++ }
                }
              }) { Icon(Icons.Default.Delete, contentDescription = s.delete) }
            }
            var sortMenu by remember { mutableStateOf(false) }
            Box {
              IconButton(onClick = { sortMenu = true }) { Icon(Icons.Default.Sort, contentDescription = s.sortName) }
              DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                listOf("name" to s.sortName, "recent" to s.sortDate, "size" to s.sortSize, "extension" to s.sortExt).forEach { (key, label) ->
                  DropdownMenuItem(text = { Text(label) }, onClick = { sort = key; sortMenu = false })
                }
              }
            }
            if (!selectionMode) IconButton(onClick = { selectionMode = true }) { Icon(Icons.Default.Checklist, contentDescription = s.selectAll) }
            if (selectionMode && selected.isNotEmpty()) {
              IconButton(onClick = {
                scope.launch(Dispatchers.IO) {
                  val files = selected.mapNotNull { path -> runCatching { java.io.File(path) }.getOrNull() }.filter { it.isFile }
                  if (files.sumOf { it.length() } > 256L * 1024 * 1024) {
                    withContext(Dispatchers.Main) { showMessage(s.fileTooLarge) }
                    return@launch
                  }
                  val zipped = runCatching {
                    com.nekonaru.pedeef.engine.zipFiles(files.map { com.nekonaru.pedeef.engine.Named(it.name, it.readBytes()) })
                  }.getOrNull()
                  if (zipped != null) com.nekonaru.pedeef.data.FilesStore.saveBytes(root, null, "selection", "zip", zipped)
                  withContext(Dispatchers.Main) { showMessage(if (zipped == null) s.genericError else s.exportZip) }
                }
              }) { Icon(Icons.Default.SaveAlt, contentDescription = s.exportZip) }
              IconButton(onClick = { showRename = true }) { Icon(Icons.Default.Edit, contentDescription = s.batchRename) }
            }
            if (!selectionMode) IconButton(onClick = { showBin = true }) { Icon(Icons.Default.Delete, contentDescription = s.recycleBin) }
            IconButton(onClick = { showNewFolder = true }) { Icon(Icons.Default.CreateNewFolder, contentDescription = s.newFolder) }
          },
        )
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(s.searchInFolder) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(
            "all" to s.filesTitle,
            "recent" to s.smartRecent,
            "starred" to s.smartStarred,
            "large" to s.smartLarge,
            "pdf" to s.smartPdf,
            "images" to s.smartImages,
          ).forEach { (key, label) ->
            FilterChip(selected = smart == key, onClick = { smart = key }, label = { Text(label) })
          }
        }
      }
    },
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) {
      if (busy) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return@Box
      }
      val list = items
      val shown = when (smart) {
        "starred" -> list?.filter { it.file.absolutePath in starred }
        "large" -> list?.filter { it.size > 10L * 1024 * 1024 }
        "pdf" -> list?.filter { it.name.endsWith(".pdf", true) }
        "images" -> list?.filter { Regex("\\.(jpg|jpeg|png)$", RegexOption.IGNORE_CASE).matches(it.name) }
        "recent" -> list?.sortedByDescending { it.modified }?.take(30)
        else -> list
      }
      if (shown == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return@Box
      }
      if (shown.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
          Box(Modifier.size(88.dp).background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(s.filesEmpty, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
        }
        return@Box
      }
      LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(shown, key = { entry -> entry.file.absolutePath }) { entry ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
              if (selectionMode) {
                val path = entry.file.absolutePath
                selected = if (path in selected) selected - path else selected + path
              } else {
                openEntry(entry)
              }
            }.padding(horizontal = 10.dp, vertical = 10.dp),
          ) {
            Box(
              Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(iconFor(entry), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
              Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
              Text(
                (if (entry.isDir) ((if (entry.size >= 10_000) "10,000+ " else "${entry.size} ") + (if (entry.size == 1L) s.fileOne else s.fileMany)) else com.nekonaru.pedeef.ui.kit.fmtSize(entry.size)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (!entry.isDir) {
              IconButton(onClick = { starred = com.nekonaru.pedeef.data.Starred.toggle(context, entry.file.absolutePath).toSet() }) {
                Icon(if (entry.file.absolutePath in starred) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = if (entry.file.absolutePath in starred) s.unstar else s.star)
              }
              IconButton(onClick = { shareFile(entry) }) { Icon(Icons.Default.Share, contentDescription = s.share) }
            } else {
              Box(Modifier.size(48.dp))
            }
            var menu by remember(entry.file.path) { mutableStateOf(false) }
            Box {
              IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
              DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(s.rename) }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = { menu = false; renameTarget = entry; renameText = entry.name })
                DropdownMenuItem(text = { Text(s.moveTo) }, leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) }, onClick = { menu = false; moveTarget = entry })
                if (!entry.isDir) {
                  DropdownMenuItem(text = { Text(s.exportTo) }, leadingIcon = { Icon(Icons.Default.SaveAlt, contentDescription = null) }, onClick = { menu = false; exportTargetHolder = entry; exportLauncher.launch(entry.name) })
                }
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

  // Recycle bin
  if (showBin) {
    AlertDialog(
      onDismissRequest = { showBin = false },
      title = { Text(s.recycleBin) },
      text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
          val binned = com.nekonaru.pedeef.data.RecycleBin.list(context)
          if (binned.isEmpty()) Text(s.filesEmpty)
          binned.forEach { file ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
              Text(file.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
              TextButton(onClick = {
                scope.launch(Dispatchers.IO) {
                  com.nekonaru.pedeef.data.RecycleBin.restore(context, root, file)
                  withContext(Dispatchers.Main) { refreshTick++ }
                }
              }) { Text(s.restore) }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          com.nekonaru.pedeef.data.RecycleBin.empty(context)
          showBin = false
          refreshTick++
        }) { Text(s.emptyBin) }
      },
      dismissButton = { TextButton(onClick = { showBin = false }) { Text(s.cancel) } },
    )
  }

  // Batch rename
  if (showRename) {
    AlertDialog(
      onDismissRequest = { showRename = false },
      title = { Text(s.batchRename) },
      text = {
        Column {
          Text(s.renamePattern, style = MaterialTheme.typography.bodyMedium)
          OutlinedTextField(value = renamePattern, onValueChange = { renamePattern = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
          Text(s.renamePreview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
      confirmButton = {
        TextButton(onClick = {
          selected.forEachIndexed { index, path ->
            val file = runCatching { java.io.File(path) }.getOrNull() ?: return@forEachIndexed
            val name = renamePattern.replace("{n}", "${index + 1}")
            val entry = FileEntry(file.name, file, file.isDirectory, if (file.isDirectory) 0L else file.length(), file.lastModified())
            runCatching { FilesStore.rename(root, entry, name) }
          }
          showRename = false
          selectionMode = false
          selected = emptySet()
          refreshTick++
        }) { Text(s.apply) }
      },
      dismissButton = { TextButton(onClick = { showRename = false }) { Text(s.cancel) } },
    )
  }

  // Rename dialog
  renameTarget?.let { entry ->
    AlertDialog(
      onDismissRequest = { renameTarget = null },
      title = { Text(s.rename) },
      text = {
        Column {
          OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true)
          renameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          val err = runCatching { FilesStore.rename(root, entry, renameText) }.exceptionOrNull()
          if (err != null) renameError = s.genericError else { renameTarget = null; renameError = null; refreshTick++ }
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
        scope.launch(Dispatchers.IO) {
          val removed = if (entry.isDir) {
            entry.file.walkTopDown().filter { it.isFile }.map { it.absolutePath }.toSet()
          } else {
            setOf(entry.file.absolutePath)
          }
          runCatching { com.nekonaru.pedeef.data.RecycleBin.trash(context, root, entry.file) }
          val next = com.nekonaru.pedeef.data.Starred.remove(context, removed)
          withContext(Dispatchers.Main) { starred = next.toSet(); deleteTarget = null; refreshTick++ }
        }
      },
      onDismiss = { deleteTarget = null },
    )
  }

  // Move to... dialog: pick a target folder
  moveTarget?.let { entry ->
    AlertDialog(
      onDismissRequest = { moveTarget = null },
      title = { Text(s.moveTo) },
      text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
          folders.forEach { f ->
            val rel = if (f == root) s.filesTitle else relPath(root, f).ifEmpty { s.filesTitle }
            Text(rel, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
              val err = runCatching { FilesStore.moveTo(root, entry, f) }.exceptionOrNull()
              if (err != null) moveError = s.genericError else { moveTarget = null; refreshTick++ }
            }.padding(10.dp))
          }
          moveError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
        }
      },
      confirmButton = { TextButton(onClick = { moveTarget = null }) { Text(s.cancel) } },
    )
  }
}

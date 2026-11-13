package com.nekonaru.pedeef.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.engine.extractText
import com.nekonaru.pedeef.engine.mentionsEncrypt
import com.nekonaru.pedeef.engine.unlockBytes
import com.nekonaru.pedeef.engine.withTempPdf
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.ToolContext
import com.nekonaru.pedeef.ui.kit.PasswordDialog
import com.nekonaru.pedeef.ui.kit.pageSizesOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(onBack: () -> Unit) {
  val s = LocalPedeefStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val clipboard = LocalClipboardManager.current
  val initialBytes = com.nekonaru.pedeef.ui.ViewerPayload.bytes
  var payload by remember { mutableStateOf(initialBytes) }
  val name = com.nekonaru.pedeef.ui.ViewerPayload.name
  var passwordError by remember { mutableStateOf<String?>(null) }
  var query by remember { mutableStateOf("") }
  var searching by remember { mutableStateOf(false) }
  var matches by remember { mutableStateOf<List<Int>?>(null) }
  var matchIndex by remember(matches) { mutableStateOf(0) }
  var pageTextIndex by remember { mutableStateOf<Int?>(null) }
  var copyMessage by remember { mutableStateOf<String?>(null) }
  val listState = rememberLazyListState()

  val bytes = payload
  if (bytes == null) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
      Text(s.cannotOpenPdf)
      TextButton(onClick = onBack) { Text(s.back) }
    }
    return
  }

  if (mentionsEncrypt(bytes)) {
    PasswordDialog(
      title = s.passwordPrompt,
      error = passwordError,
      onSubmit = { pw ->
        val unlocked = unlockBytes(bytes, pw)
        if (unlocked == null) passwordError = s.passwordWrong else { payload = unlocked; com.nekonaru.pedeef.ui.ViewerPayload.bytes = unlocked }
      },
      onDismiss = onBack,
    )
    return
  }

  val sizesState = produceState<List<Pair<Int, Int>>?>(initialValue = null, bytes) {
    value = runCatching { pageSizesOf(bytes, ToolContext.cacheDir) }.getOrNull()
  }
  val sizes = sizesState.value

  LaunchedEffect(matches) {
    val m = matches ?: return@LaunchedEffect
    if (m.isNotEmpty()) listState.animateScrollToItem(m[matchIndex.coerceIn(0, m.size - 1)])
  }
  LaunchedEffect(pageTextIndex) {
    val i = pageTextIndex ?: return@LaunchedEffect
    val text = withContext(Dispatchers.Default) {
      withTempPdf(bytes, ToolContext.cacheDir, "pagetext") { tmp ->
        com.tom_roush.pdfbox.pdmodel.PDDocument.load(tmp).use { doc ->
          val stripper = com.tom_roush.pdfbox.text.PDFTextStripper().apply { startPage = i + 1; endPage = i + 1 }
          stripper.getText(doc)
        }
      }
    }
    clipboard.setText(AnnotatedString(text))
    copyMessage = s.pageTextCopied
  }

  fun runSearch() {
    val q = query.trim()
    if (q.isEmpty()) return
    scope.launch {
      searching = true
      val hits = withContext(Dispatchers.Default) {
        withTempPdf(bytes, ToolContext.cacheDir, "search") { tmp ->
          com.tom_roush.pdfbox.pdmodel.PDDocument.load(tmp).use { doc ->
            val n = doc.numberOfPages
            val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
            val needle = q.lowercase().replace(Regex("\\s+"), " ")
            (0 until n).mapNotNull { i ->
              stripper.startPage = i + 1
              stripper.endPage = i + 1
              val text = runCatching { stripper.getText(doc) }.getOrNull() ?: ""
              if (text.lowercase().replace(Regex("\\s+"), " ").contains(needle)) i else null
            }
          }
        }
      }
      matches = hits
      matchIndex = 0
      searching = false
    }
  }

  fun shareExternally() {
    scope.launch {
      val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        File(dir, com.nekonaru.pedeef.data.FilesStore.sanitizeName(name.removeSuffix(".pdf")) + ".pdf").apply { writeBytes(bytes) }
      }
      val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.files", file)
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, null))
    }
  }

  val n = sizes?.size ?: 0
  val currentPage = if (n > 0) (listState.firstVisibleItemIndex + 1).coerceIn(1, n) else 0

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          title = { Text(name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
          navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) } },
          actions = {
            IconButton(onClick = { pageTextIndex = currentPage - 1 }) { Icon(Icons.Default.ContentCopy, contentDescription = s.copyPageText) }
            IconButton(onClick = { shareExternally() }) { Icon(Icons.Default.OpenInNew, contentDescription = s.openElsewhere) }
          },
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
          OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(s.viewerSearchHint) },
            singleLine = true,
            trailingIcon = { IconButton(onClick = { runSearch() }) { Icon(Icons.Default.Search, contentDescription = null) } },
            modifier = Modifier.weight(1f).padding(bottom = 6.dp),
          )
        }
        if (searching) {
          Row(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp)) }
        }
        val m = matches
        if (m != null) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
            Text(
              if (m.isEmpty()) s.textNotFound else "${s.searchResults} ${matchIndex + 1}/${m.size}",
              style = MaterialTheme.typography.bodyMedium,
              color = if (m.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(1f),
            )
            if (m.isNotEmpty()) {
              IconButton(onClick = { matchIndex = (matchIndex - 1 + m.size) % m.size; scope.launch { listState.animateScrollToItem(m[matchIndex]) } }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = s.prevMatch) }
              IconButton(onClick = { matchIndex = (matchIndex + 1) % m.size; scope.launch { listState.animateScrollToItem(m[matchIndex]) } }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = s.nextMatch) }
              IconButton(onClick = { matches = null; query = "" }) { Icon(Icons.Outlined.Close, contentDescription = s.cancel) }
            }
          }
        }
        copyMessage?.let {
          Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }
      }
    },
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surfaceVariant)) {
      if (sizes == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return@Box
      }
      LazyColumn(
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(sizes.size) { i -> PageItem(bytes, i, sizes[i], matches?.contains(i) == true) }
      }
      // Bottom page indicator
      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
          IconButton(onClick = { scope.launch { listState.animateScrollToItem((currentPage - 2).coerceAtLeast(0)) } }, enabled = currentPage > 1) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
          Text("$currentPage ${s.pageOf} $n", style = MaterialTheme.typography.labelLarge)
          IconButton(onClick = { scope.launch { listState.animateScrollToItem((currentPage).coerceAtMost(n - 1)) } }, enabled = currentPage < n) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) }
        }
      }
    }
  }
}

@Composable
private fun PageItem(bytes: ByteArray, index: Int, size: Pair<Int, Int>, isMatch: Boolean) {
  val image by produceState<android.graphics.Bitmap?>(initialValue = null, bytes, index) {
    value = com.nekonaru.pedeef.ui.kit.renderPage(bytes, index, 1200, ToolContext.cacheDir)
  }
  val ratio = size.first.toFloat() / size.second.toFloat()
  Surface(
    shape = RoundedCornerShape(4.dp),
    color = androidx.compose.ui.graphics.Color.White,
    border = if (isMatch) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
    modifier = Modifier.fillMaxWidth().aspectRatio(ratio),
  ) {
    val bmp = image
    if (bmp != null) {
      Image(bmp.asImageBitmap(), contentDescription = "Page ${index + 1}", modifier = Modifier.fillMaxSize())
    } else {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
    }
  }
}

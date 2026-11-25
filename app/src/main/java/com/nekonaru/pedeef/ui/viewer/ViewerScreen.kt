package com.nekonaru.pedeef.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
import java.io.File

/** RGB inversion for night mode page rendering. */
private val invertColors = androidx.compose.ui.graphics.ColorMatrix(
  floatArrayOf(
    -1f, 0f, 0f, 0f, 255f,
    0f, -1f, 0f, 0f, 255f,
    0f, 0f, -1f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f,
  ),
)

private sealed interface AutoDecrypt {
  data object Working : AutoDecrypt
  data object Plain : AutoDecrypt
  data class Ok(val bytes: ByteArray) : AutoDecrypt
  data object NeedsPassword : AutoDecrypt
}

private sealed interface PageSizes {
  data class Ok(val list: List<Pair<Int, Int>>) : PageSizes
  data object Failed : PageSizes
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(onBack: () -> Unit, nightMode: Boolean = false, onToggleNightMode: () -> Unit = {}) {
  val s = LocalPedeefStrings.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val clipboard = LocalClipboardManager.current
  val initialBytes = com.nekonaru.pedeef.ui.ViewerPayload.bytes
  val payloadVersion = com.nekonaru.pedeef.ui.ViewerPayload.version
  var payload by remember(payloadVersion) { mutableStateOf(initialBytes) }
  val name = com.nekonaru.pedeef.ui.ViewerPayload.name
  var passwordError by remember { mutableStateOf<String?>(null) }
  var query by remember { mutableStateOf("") }
  var searching by remember { mutableStateOf(false) }
  var matches by remember { mutableStateOf<List<Int>?>(null) }
  var highlights by remember { mutableStateOf<Map<Int, List<android.graphics.RectF>>?>(null) }
  var matchIndex by remember(matches) { mutableStateOf(0) }
  var pageTextIndex by remember { mutableStateOf<Int?>(null) }
  var pageText by remember { mutableStateOf<String?>(null) }
  var copyToken by remember { mutableStateOf(0) }
  var speaking by remember { mutableStateOf(false) }
  var speakToken by remember { mutableStateOf(0) }
  val speaker = remember { PageSpeaker(context, onDone = { speaking = false }) }
  androidx.compose.runtime.DisposableEffect(Unit) { onDispose { speaker.close() } }
  var copyMessage by remember { mutableStateOf<String?>(null) }
  var showThumbs by remember { mutableStateOf(false) }
  var showBookmarks by remember { mutableStateOf(false) }
  var zoomLevel by remember { mutableStateOf(0) } // 0 = 100%, 1 = 125%, 2 = 150%
  val zoomFactor = when (zoomLevel) { 0 -> 1f; 1 -> 1.25f; else -> 1.5f }
  var savedMessage by remember { mutableStateOf<String?>(null) }
  var persisted by remember { mutableStateOf(com.nekonaru.pedeef.ui.ViewerPayload.persisted) }
  val listState = rememberLazyListState()
  val firstVisible by remember { androidx.compose.runtime.derivedStateOf { listState.firstVisibleItemIndex } }

  val bytes = payload
  if (bytes == null) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
      Text(s.cannotOpenPdf)
      TextButton(onClick = onBack) { Text(s.back) }
    }
    return
  }

  // Decide encryption on a background thread; rendering the viewer off the main thread is non-negotiable.
  val autoDecrypt by produceState<AutoDecrypt>(initialValue = AutoDecrypt.Working, bytes) {
    value = withContext(Dispatchers.Default) {
      if (!mentionsEncrypt(bytes)) AutoDecrypt.Plain
      else unlockBytes(bytes, "")?.let { AutoDecrypt.Ok(it) } ?: AutoDecrypt.NeedsPassword
    }
  }
  // System back must also clear the in-memory payload. Register before any early return so it is
  // active during the decrypt spinner; the password dialog handles its own dismissal.
  androidx.activity.compose.BackHandler(enabled = autoDecrypt !is AutoDecrypt.NeedsPassword, onBack = onBack)
  when (val a = autoDecrypt) {
    AutoDecrypt.Working -> {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
      return
    }
    AutoDecrypt.NeedsPassword -> {
      PasswordDialog(
        title = s.passwordPrompt,
        error = passwordError,
        onSubmit = { pw ->
          scope.launch(Dispatchers.Default) {
            val unlockedWithPw = unlockBytes(bytes, pw)
            withContext(Dispatchers.Main) {
              if (unlockedWithPw == null) passwordError = s.passwordWrong else { payload = unlockedWithPw; com.nekonaru.pedeef.ui.ViewerPayload.set(unlockedWithPw, name, com.nekonaru.pedeef.ui.ViewerPayload.persisted) }
            }
          }
        },
        onDismiss = onBack,
      )
      return
    }
    else -> Unit
  }
  val working = when (val a = autoDecrypt) {
    is AutoDecrypt.Ok -> a.bytes
    else -> bytes
  }

  val sizesState = produceState<PageSizes?>(initialValue = null, working) {
    value = runCatching { PageSizes.Ok(pageSizesOf(working, ToolContext.cacheDir)) }.getOrDefault(PageSizes.Failed)
  }
  val sizes = (sizesState.value as? PageSizes.Ok)?.list
  val sizesFailed = sizesState.value == PageSizes.Failed

  // Resume reading: reopen at the page the user left off on (keyed by name + size).
  val resumeKey = remember(working, name) { name + "#" + working.size }
  var resumed by remember(working) { mutableStateOf(false) }
  LaunchedEffect(sizes != null) {
    if (resumed || sizes == null) return@LaunchedEffect
    resumed = true
    val pos = com.nekonaru.pedeef.data.ReadingPositions.pageFor(context, resumeKey)
    if (pos != null && pos in 2..sizes.size) listState.scrollToItem(pos - 1)
  }
  LaunchedEffect(firstVisible, resumed) {
    // Do not save page 1 over the stored position before the resume scroll has landed.
    if (!resumed) return@LaunchedEffect
    kotlinx.coroutines.delay(600)
    val page = firstVisible + 1
    if (page >= 1) withContext(Dispatchers.IO) { com.nekonaru.pedeef.data.ReadingPositions.save(context, resumeKey, page) }
  }

  LaunchedEffect(matches) {
    val m = matches ?: return@LaunchedEffect
    if (m.isNotEmpty()) listState.animateScrollToItem(m[matchIndex.coerceIn(0, m.size - 1)])
  }
  LaunchedEffect(pageTextIndex) {
    val i = pageTextIndex ?: return@LaunchedEffect
    pageText = withContext(Dispatchers.Default) {
      runCatching {
        withTempPdf(working, ToolContext.cacheDir, "pagetext") { tmp ->
          com.tom_roush.pdfbox.pdmodel.PDDocument.load(tmp).use { doc ->
            val stripper = com.tom_roush.pdfbox.text.PDFTextStripper().apply { startPage = i + 1; endPage = i + 1 }
            stripper.getText(doc)
          }
        }
      }.getOrNull()
    }
    if (pageText == null) copyMessage = s.genericError
  }
  LaunchedEffect(pageText, speakToken) {
    if (!speaking) return@LaunchedEffect
    val text = pageText ?: return@LaunchedEffect
    speaker.speak(text)
  }
  LaunchedEffect(pageText, copyToken) {
    if (copyToken == 0) return@LaunchedEffect
    val text = pageText ?: return@LaunchedEffect
    clipboard.setText(AnnotatedString(text))
    copyMessage = s.pageTextCopied
  }

  fun runSearch() {
    val q = query.trim()
    if (q.isEmpty()) return
    scope.launch {
      searching = true
      val result = withContext(Dispatchers.Default) {
        runCatching { com.nekonaru.pedeef.engine.searchPdf(working, q, ToolContext.cacheDir) }.getOrNull()
      }
      if (result == null) {
        matches = null
        highlights = null
        copyMessage = s.genericError
      } else {
        matches = result.pages
        highlights = result.boxes
        matchIndex = 0
      }
      searching = false
    }
  }

  fun shareExternally() {
    scope.launch {
      val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        File(dir, com.nekonaru.pedeef.data.FilesStore.sanitizeName(name.removeSuffix(".pdf")) + ".pdf").apply { writeBytes(working) }
      }
      val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.files", file)
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, null))
    }
  }

  fun saveToFiles() {
    scope.launch {
      val saved = withContext(Dispatchers.IO) {
        runCatching {
          val public = com.nekonaru.pedeef.data.PublicFiles.root(context)
          val dir = public ?: com.nekonaru.pedeef.data.FilesStore.root(context)
          val base = com.nekonaru.pedeef.data.FilesStore.sanitizeName(name.removeSuffix(".pdf").ifEmpty { "document" })
          val file = com.nekonaru.pedeef.data.FilesStore.saveBytes(dir, null, base, "pdf", working)
          (if (public != null) s.savedToDownload else s.savedToApp) + ": " + file.name
        }.getOrNull()
      }
      if (saved == null) {
        savedMessage = s.genericError
        return@launch
      }
      persisted = true
      com.nekonaru.pedeef.ui.ViewerPayload.persisted = true
      savedMessage = saved
    }
  }

  fun printPdf() {
    scope.launch(Dispatchers.IO) { com.nekonaru.pedeef.engine.printPdf(context, working, name) }
  }

  val outline by produceState<List<Pair<String, Int>>>(initialValue = emptyList(), working) {
    value = withContext(Dispatchers.IO) { runCatching { com.nekonaru.pedeef.engine.readOutline(working) }.getOrDefault(emptyList()) }
  }

  val n = sizes?.size ?: 0
  val currentPage = if (n > 0) (firstVisible + 1).coerceIn(1, n) else 0

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          title = { Text(name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
          navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = s.back) } },
          actions = {
            if (outline.isNotEmpty()) {
              IconButton(onClick = { showBookmarks = !showBookmarks }) { Icon(Icons.Default.Bookmarks, contentDescription = s.bookmarks) }
            }
            TextButton(onClick = { zoomLevel = (zoomLevel + 1) % 3 }) {
              Text("${(zoomFactor * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onToggleNightMode) {
              Icon(
                if (nightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = s.nightMode,
              )
            }

            if (!persisted) {
              IconButton(onClick = { saveToFiles() }) { Icon(Icons.Default.SaveAlt, contentDescription = s.saveToFiles) }
            }
            IconButton(onClick = { showThumbs = !showThumbs }) { Icon(Icons.Default.GridView, contentDescription = s.pageThumbs) }
            IconButton(onClick = { printPdf() }) { Icon(Icons.Default.Print, contentDescription = s.print) }
            IconButton(onClick = { copyToken++; pageTextIndex = (currentPage - 1).coerceAtLeast(0) }, enabled = n > 0) { Icon(Icons.Default.ContentCopy, contentDescription = s.copyPageText) }
            IconButton(
              onClick = {
                if (speaking) { speaker.stop(); speaking = false }
                else { speaking = true; speakToken++; pageTextIndex = (currentPage - 1).coerceAtLeast(0) }
              },
              enabled = n > 0,
            ) { Icon(if (speaking) Icons.Default.Stop else Icons.Default.VolumeUp, contentDescription = if (speaking) s.stopSpeech else s.speakPage) }
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
              IconButton(onClick = { matches = null; highlights = null; query = "" }) { Icon(Icons.Outlined.Close, contentDescription = s.cancel) }
            }
          }
        }
        copyMessage?.let {
          Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }
        savedMessage?.let {
          Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }
        if (autoDecrypt is AutoDecrypt.Ok) {
          Text(s.decryptNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }
      }
    },
  ) { padding ->
    BoxWithConstraints(
      Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
      val viewport = maxWidth
      if (sizes == null) {
        if (sizesFailed) {
          Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(s.cannotOpenPdf)
            TextButton(onClick = onBack) { Text(s.back) }
          }
        } else {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return@BoxWithConstraints
      }
      // Scroll the pages horizontally when zoomed in; overlays stay pinned to the viewport.
      Box(Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
        LazyColumn(
          state = listState,
          contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(sizes.size) { i -> PageItem(working, i, sizes[i], viewport, zoomFactor, matches?.contains(i) == true, highlights?.get(i) ?: emptyList(), nightMode) }
        }
      }
      // Bookmarks panel overlay (top-aligned)
      androidx.compose.animation.AnimatedVisibility(
        visible = showBookmarks,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(160)),
        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(110)),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.padding(8.dp).width(340.dp).heightIn(max = 420.dp),
        ) {
          LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
            items(outline) { (title, page) ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { scope.launch { listState.animateScrollToItem(page) }; showBookmarks = false }
                  .padding(horizontal = 12.dp, vertical = 8.dp),
              ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(start = 10.dp).weight(1f))
                Text("${page + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
      // Bottom page indicator + optional thumbnail strip
      Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)) {
        if (showThumbs && sizes != null) {
          androidx.compose.animation.AnimatedVisibility(
            visible = showThumbs,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(160)),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(110)),
          ) {
            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.padding(bottom = 8.dp).width(340.dp),
            ) {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              ) {
                items(sizes.size) { i -> ThumbItem(working, i, i == currentPage - 1, nightMode) { scope.launch { listState.animateScrollToItem(i) } } }
              }
            }
          }
        }
        Surface(
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
          shape = RoundedCornerShape(18.dp),
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
}

@Composable
private fun PageItem(bytes: ByteArray, index: Int, size: Pair<Int, Int>, viewport: androidx.compose.ui.unit.Dp, zoomFactor: Float, isMatch: Boolean, highlights: List<android.graphics.RectF> = emptyList(), nightMode: Boolean = false) {
  val s = LocalPedeefStrings.current
  val image by produceState<android.graphics.Bitmap?>(initialValue = null, bytes, index, zoomFactor) {
    value = com.nekonaru.pedeef.ui.kit.renderPage(bytes, index, (1200 * zoomFactor).toInt(), ToolContext.cacheDir)
  }
  val ratio = size.first.toFloat() / size.second.toFloat()
  Box(Modifier.width(viewport * zoomFactor).aspectRatio(ratio)) {
    Surface(
      shape = RoundedCornerShape(4.dp),
      color = androidx.compose.ui.graphics.Color.White,
      border = if (isMatch) {
        androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
      } else {
        null
      },
      modifier = Modifier.fillMaxSize(),
    ) {
      val bmp = image
      if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = "${s.pageName} ${index + 1}", colorFilter = if (nightMode) androidx.compose.ui.graphics.ColorFilter.colorMatrix(invertColors) else null, modifier = Modifier.fillMaxSize())
      } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
      }
    }
    if (highlights.isNotEmpty()) {
      androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        for (h in highlights) {
          drawRect(
            color = androidx.compose.ui.graphics.Color(0xFFFFD54F).copy(alpha = 0.45f),
            topLeft = Offset(h.left * this.size.width, h.top * this.size.height),
            size = Size((h.right - h.left) * this.size.width, (h.bottom - h.top) * this.size.height),
          )
        }
      }
    }
  }
}

@Composable
private fun ThumbItem(bytes: ByteArray, index: Int, selected: Boolean, nightMode: Boolean = false, onClick: () -> Unit) {
  val s = LocalPedeefStrings.current
  val image by produceState<android.graphics.Bitmap?>(initialValue = null, bytes, index) {
    value = com.nekonaru.pedeef.ui.kit.renderPage(bytes, index, 180, ToolContext.cacheDir)
  }
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(2.dp),
  ) {
    Box(
      Modifier
        .size(52.dp)
        .border(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        .clip(RoundedCornerShape(8.dp))
        .background(androidx.compose.ui.graphics.Color.White),
      contentAlignment = Alignment.Center,
    ) {
      val bmp = image
      if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = "${s.pageName} ${index + 1}", colorFilter = if (nightMode) androidx.compose.ui.graphics.ColorFilter.colorMatrix(invertColors) else null, contentScale = androidx.compose.ui.layout.ContentScale.Fit, modifier = Modifier.fillMaxSize())
      } else {
        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
      }
    }
    Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
  }
}

package com.nekonaru.pedeef

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.Pins
import com.nekonaru.pedeef.tools.ToolContext
import com.nekonaru.pedeef.ui.App
import com.nekonaru.pedeef.ui.ViewerPayload
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
  private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PDFBoxResourceLoader.init(applicationContext)
    ToolContext.cacheDir = cacheDir
    ToolContext.filesDir = filesDir
    // Clear temp files left by a kill: they hold plaintext document content. Off the main thread.
    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
      cacheDir.listFiles()?.forEach { f ->
        if (f.isFile && listOf("ocr-", "redact-", "export-", "viewer-", "page-", "pagetext-", "search-", "searchhl-", "sizes-", "thread", "share-", "incoming-", "print-", "blank-", "dup-", "lint-", "report-", "compare-").any { f.name.startsWith(it) }) f.delete()
        if (f.isDirectory && f.name == "share") f.deleteRecursively()
      }
    }
    FilesStore.root(applicationContext) // make sure the tree exists before any screen reads it
    Pins.load(applicationContext)
    // Walks the tree and talks to the shortcut service; never on the main thread.
    scope.launch(kotlinx.coroutines.Dispatchers.IO) { com.nekonaru.pedeef.data.RecentShortcuts.refresh(applicationContext) }
    consumeLaunchIntent(intent)
    enableEdgeToEdge()
    setContent { App() }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    scope.launch(kotlinx.coroutines.Dispatchers.IO) { com.nekonaru.pedeef.data.RecentShortcuts.refresh(applicationContext) }
    consumeLaunchIntent(intent)
  }

  /** Routes a launcher shortcut picked by the user to the matching screen. */
  private fun consumeShortcut(intent: Intent?) {
    val action = intent?.action ?: return
    if (action == com.nekonaru.pedeef.data.RecentShortcuts.ACTION_OPEN_RECENT) {
      val file = com.nekonaru.pedeef.data.RecentShortcuts.openable(this, intent.getStringExtra(com.nekonaru.pedeef.data.RecentShortcuts.EXTRA_PATH)) ?: return
      scope.launch {
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return@launch
        withContext(kotlinx.coroutines.Dispatchers.Main) {
          ViewerPayload.set(bytes, file.name, persisted = true)
        }
      }
      return
    }
    val route = when (intent?.action) {
      "com.nekonaru.pedeef.action.SCAN" -> "scan"
      "com.nekonaru.pedeef.action.MERGE" -> "tool/merge"
      "com.nekonaru.pedeef.action.SIGN" -> "tool/fillSign"
      "com.nekonaru.pedeef.action.ORGANIZE" -> "tool/organize"
      else -> return
    }
    com.nekonaru.pedeef.ui.ShortcutRoute.open(route)
  }

  /** Opens a PDF handed to us by another app in the viewer; consent is asked before saving it to the file tree. */
  private fun consumeLaunchIntent(intent: Intent?) {
    val action = intent?.action ?: return
    consumeShortcut(intent)
    val uri = when (action) {
      Intent.ACTION_VIEW -> intent.data
      Intent.ACTION_SEND -> @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
      else -> null
    } ?: return
    // Open from memory without copying: the user decides whether to keep it via the viewer's Save button.
    // Read the stream off the main thread so a large PDF never stalls the launcher activity.
    scope.launch {
      val bytes = runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return@launch
      val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "document.pdf"
      withContext(kotlinx.coroutines.Dispatchers.Main) {
        ViewerPayload.set(bytes, com.nekonaru.pedeef.data.FilesStore.sanitizeName(name.removeSuffix(".pdf")).let { if (it.isEmpty()) "document" else "$it.pdf" }, persisted = false)
      }
    }
  }
}

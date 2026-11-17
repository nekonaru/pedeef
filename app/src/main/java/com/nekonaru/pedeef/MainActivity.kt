package com.nekonaru.pedeef

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.Pins
import com.nekonaru.pedeef.tools.ToolContext
import com.nekonaru.pedeef.ui.App
import com.nekonaru.pedeef.ui.ViewerPayload
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PDFBoxResourceLoader.init(applicationContext)
    ToolContext.cacheDir = cacheDir
    ToolContext.filesDir = filesDir
    // Clear temp files left by a kill: they hold plaintext document content.
    cacheDir.listFiles()?.forEach { f ->
      if (f.isFile && listOf("ocr-", "redact-", "export-", "viewer-", "page-", "pagetext-", "search-", "searchhl-", "sizes-", "thread", "share-", "incoming-").any { f.name.startsWith(it) }) f.delete()
      if (f.isDirectory && f.name == "share") f.deleteRecursively()
    }
    FilesStore.root(applicationContext) // make sure the tree exists before any screen reads it
    Pins.load(applicationContext)
    consumeLaunchIntent(intent)
    enableEdgeToEdge()
    setContent { App() }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    consumeLaunchIntent(intent)
  }

  /** Opens a PDF handed to us by another app in the viewer; consent is asked before saving it to the file tree. */
  private fun consumeLaunchIntent(intent: Intent?) {
    val action = intent?.action ?: return
    val uri = when (action) {
      Intent.ACTION_VIEW -> intent.data
      Intent.ACTION_SEND -> @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
      else -> null
    } ?: return
    // Open from memory without copying: the user decides whether to keep it via the viewer's Save button.
    runCatching {
      val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
      val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "document.pdf"
      ViewerPayload.set(bytes, com.nekonaru.pedeef.data.FilesStore.sanitizeName(name.removeSuffix(".pdf")).let { if (it.isEmpty()) "document" else "$it.pdf" }, persisted = false)
    }
  }
}

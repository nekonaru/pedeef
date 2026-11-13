package com.nekonaru.pedeef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.Pins
import com.nekonaru.pedeef.tools.ToolContext
import com.nekonaru.pedeef.ui.App
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PDFBoxResourceLoader.init(applicationContext)
    ToolContext.cacheDir = cacheDir
    // Clear temp files left by a kill: they hold plaintext document content.
    cacheDir.listFiles()?.forEach { f ->
      if (f.isFile && listOf("ocr-", "redact-", "export-", "viewer-", "page-", "pagetext-", "search-", "sizes-", "thread").any { f.name.startsWith(it) }) f.delete()
    }
    FilesStore.root(applicationContext) // make sure the tree exists before any screen reads it
    Pins.load(applicationContext)
    enableEdgeToEdge()
    setContent { App() }
  }
}

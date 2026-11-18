package com.nekonaru.pedeef.data

import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import java.io.File

/**
 * Publishes up to 4 "reopen this PDF" shortcuts on the launcher icon, driven by the
 * most recently modified PDFs in the file tree. Shortcuts are validated and re-read
 * from disk on every launch, never from a captured document snapshot.
 */
object RecentShortcuts {
  const val ACTION_OPEN_RECENT = "com.nekonaru.pedeef.action.OPEN_RECENT"
  const val EXTRA_PATH = "path"
  const val MAX = 4

  /** The stored file for a candidate path, or null when it is missing, not a PDF, or outside our roots. */
  fun openable(appContext: android.content.Context, rawPath: String?): File? {
    val raw = rawPath ?: return null
    val f = File(raw)
    if (!f.isFile || !f.extension.equals("pdf", ignoreCase = true)) return null
    val canon = runCatching { f.canonicalPath }.getOrNull() ?: return null
    val roots = listOfNotNull(PublicFiles.root(appContext), FilesStore.root(appContext))
    val inside = roots.any { root ->
      val rc = runCatching { root.canonicalPath }.getOrNull() ?: return@any false
      canon == rc || canon.startsWith(rc + File.separator)
    }
    return if (inside) f else null
  }

  fun refresh(appContext: android.content.Context) {
    val recent = RecentFiles.list(appContext, MAX)
    val shortcuts = recent.map { entry ->
      val short = entry.name.removeSuffix(".pdf").removeSuffix(".PDF").ifBlank { "Document" }.take(24)
      val intent = Intent(ACTION_OPEN_RECENT)
        .putExtra(EXTRA_PATH, entry.file.absolutePath)
        .setClass(appContext, com.nekonaru.pedeef.MainActivity::class.java)
      ShortcutInfoCompat.Builder(appContext, "recent-${entry.file.absolutePath.hashCode()}")
        .setShortLabel(short)
        .setLongLabel(short)
        .setIcon(IconCompat.createWithResource(appContext, com.nekonaru.pedeef.R.mipmap.ic_launcher))
        .setIntent(intent)
        .build()
    }
    runCatching { ShortcutManagerCompat.setDynamicShortcuts(appContext, shortcuts) }
  }
}

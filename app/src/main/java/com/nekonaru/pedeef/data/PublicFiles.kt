package com.nekonaru.pedeef.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Where user files live: `Download/Pedeef` on the phone. On Android 10 and below the legacy storage permission
 * model covers this; from Android 11 it needs "All files access" (MANAGE_EXTERNAL_STORAGE), the standard route
 * for file managers and PDF tools. Without it, Pedeef falls back to its private app storage.
 */
object PublicFiles {
  /** The public folder, or null when it cannot be used (permission missing or card
unmounted). */
  fun root(context: Context): File? {
    if (!hasAccess(context)) return null
    return runCatching {
      File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Pedeef").apply { mkdirs() }
    }.getOrNull()
  }

  fun hasAccess(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
    else ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED

  /** Opens the system screen where the user grants "All files access" (Android 11+). */
  fun requestManageAllFilesAccess(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return // nothing to request on older versions
    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
      data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
  }

  /** One-time migration of files written before public storage was introduced. */
  fun migrateOnce(context: Context) {
    val public = root(context) ?: return
    val marker = File(public, ".migrated")
    if (marker.exists()) return
    val private = FilesStore.root(context)
    if (private != public) {
      runCatching {
        private.listFiles()?.forEach { f ->
          if (f.name == marker.name) return@forEach
          f.copyRecursively(File(public, f.name), overwrite = false)
          f.deleteRecursively()
        }
      }
    }
    runCatching { marker.writeText("1") }
  }
}

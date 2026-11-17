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
  /** The public folder, or null when it cannot be used (permission missing or card unmounted). */
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

  /**
   * One-time migration of files written before public storage was introduced.
   * Every source file is copied to a temp name, verified by size, renamed into place, and only then deleted.
   * The .migrated marker is written only when every file succeeded; partial runs retry on next launch.
   */
  fun migrateOnce(context: Context) {
    val public = root(context) ?: return
    val marker = File(public, ".migrated")
    if (marker.exists()) return
    val private = FilesStore.root(context)
    if (private == public) {
      runCatching { marker.writeText("1") }
      return
    }
    var ok = true
    private.listFiles()?.forEach { src ->
      if (src.name.startsWith(".")) return@forEach
      val dest = File(public, src.name)
      try {
        if (src.isDirectory) {
          ok = ok && copyDirVerified(src, dest)
        } else {
          ok = ok && copyFileVerified(src, dest)
        }
      } catch (_: Exception) {
        ok = false
      }
    }
    // Only mark migrated when everything made it, so a crash mid-copy retries instead of losing files.
    if (ok) runCatching { marker.writeText("1") }
  }

  private fun copyDirVerified(src: File, dest: File): Boolean {
    var ok = true
    dest.mkdirs()
    src.listFiles()?.forEach { child ->
      val target = File(dest, child.name)
      ok = ok && if (child.isDirectory) copyDirVerified(child, target) else copyFileVerified(child, target)
    }
    if (ok && dest.isDirectory && src.listFiles().orEmpty().all { File(dest, it.name).exists() }) {
      src.deleteRecursively()
    }
    return ok
  }

  private fun copyFileVerified(src: File, dest: File): Boolean {
    val name = dest.name
    val parent = dest.parentFile
    // Never clobber a file the user already has in the public folder; pick a free name instead.
    var target = dest
    var n = 2
    while (target.exists()) {
      target = File(parent, FilesStore.sanitizeName(name.substringBeforeLast('.')) + " ($n)" + (if ('.' in name) ".${name.substringAfterLast('.')}" else ""))
      n++
    }
    val tmp = File(parent, target.name + ".part")
    src.inputStream().use { input -> tmp.outputStream().use { output -> input.copyTo(output) } }
    if (tmp.length() != src.length()) {
      tmp.delete()
      return false
    }
    if (!tmp.renameTo(target)) {
      tmp.delete()
      return false
    }
    src.delete()
    return true
  }
}

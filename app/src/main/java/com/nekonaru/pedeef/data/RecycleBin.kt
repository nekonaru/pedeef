package com.nekonaru.pedeef.data

import android.content.Context
import java.io.File

/** A local recycle bin. Files are copied into app storage, then removed from their original path. */
object RecycleBin {
  fun root(context: Context): File = File(context.filesDir, "RecycleBin").apply { mkdirs() }

  private fun inside(root: File, file: File): Boolean {
    val r = root.canonicalFile.path
    val f = file.canonicalFile.path
    return f == r || f.startsWith(r + File.separator)
  }

  fun trash(context: Context, root: File, file: File): Boolean {
    if (!inside(root, file) || file == root || !file.exists()) return false
    val bin = root(context)
    val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US).format(java.util.Date())
    val target = File(bin, "${FilesStore.sanitizeName(file.name)}-$stamp")
    return runCatching {
      if (file.isDirectory) {
        target.mkdirs()
        file.walkTopDown().forEach { child ->
          if (child.isFile) {
            val destination = File(target, child.relativeTo(file).path)
            destination.parentFile?.mkdirs()
            child.copyTo(destination, overwrite = true)
          }
        }
        file.deleteRecursively()
      } else {
        file.copyTo(target, overwrite = true)
        file.delete()
      }
      true
    }.getOrDefault(false)
  }

  fun list(context: Context): List<File> =
    root(context).listFiles()?.toList()?.sortedByDescending { it.lastModified() }.orEmpty()

  fun restore(context: Context, root: File, file: File): Boolean {
    if (!inside(root(context), file)) return false
    val name = file.name.replace(Regex("-\\d{8}-\\d{6}$"), "")
    var target = File(root, name)
    var n = 2
    while (target.exists()) {
      val dot = name.lastIndexOf('.')
      val base = if (dot > 0) name.substring(0, dot) else name
      val ext = if (dot > 0) name.substring(dot) else ""
      target = File(root, "$base ($n)$ext")
      n++
    }
    return runCatching {
      if (!file.renameTo(target)) {
        if (file.isDirectory) {
          target.mkdirs()
          file.walkTopDown().forEach { child ->
            if (child.isFile) {
              val destination = File(target, child.relativeTo(file).path)
              destination.parentFile?.mkdirs()
              child.copyTo(destination, overwrite = true)
            }
          }
          file.deleteRecursively()
        } else {
          file.copyTo(target, overwrite = true)
          file.delete()
        }
      }
      true
    }.getOrDefault(false)
  }

  fun empty(context: Context) {
    root(context).listFiles()?.forEach { runCatching { it.deleteRecursively() } }
  }
}

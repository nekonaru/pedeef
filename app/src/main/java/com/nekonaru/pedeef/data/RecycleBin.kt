package com.nekonaru.pedeef.data

import android.content.Context
import java.io.File

/** A local recycle bin. Files are renamed and moved inside the app tree, never deleted on trash. */
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
    return runCatching { file.renameTo(target) }.getOrDefault(false)
  }

  fun list(context: Context): List<File> =
    root(context).listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() }.orEmpty()

  fun restore(context: Context, root: File, file: File): Boolean {
    if (!inside(root(context), file)) return false
    val name = file.name.replace(Regex("-\\d{8}-\\d{6}$"), "")
    var target = File(root, name)
    var n = 2
    while (target.exists()) {
      target = File(root, "${name.substringBeforeLast('.', name)} ($n).${name.substringAfterLast('.', "")}".trimEnd('.'))
      n++
    }
    return runCatching { file.renameTo(target) }.getOrDefault(false)
  }

  fun empty(context: Context) {
    root(context).listFiles()?.forEach { runCatching { it.deleteRecursively() } }
  }
}

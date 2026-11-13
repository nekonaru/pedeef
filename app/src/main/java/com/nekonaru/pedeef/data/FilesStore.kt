package com.nekonaru.pedeef.data

import java.io.File

/** One entry in the app's file tree. */
class FileEntry(val name: String, val file: File, val isDir: Boolean, val size: Long, val modified: Long)

/**
 * The app's own file tree: filesDir/Pedeef, one level of user folders. Safe by construction: every path is
 * resolved and checked to stay inside the root before anything touches the disk.
 */
object FilesStore {
  fun root(appContext: android.content.Context): File =
    File(appContext.filesDir, "Pedeef").apply { mkdirs() }

  private fun insideRoot(root: File, f: File): Boolean {
    val r = root.canonicalFile.toPath()
    val t = f.canonicalFile.toPath()
    return t == r || t.startsWith(r)
  }

  fun list(dir: File): List<FileEntry> {
    val children = dir.listFiles()?.toList().orEmpty()
    return children
      .filter { !it.name.startsWith(".") }
      .map { FileEntry(it.name, it, it.isDirectory, if (it.isDirectory) dirSize(it) else it.length(), it.lastModified()) }
      .sortedWith(compareByDescending<FileEntry> { it.isDir }.thenBy { it.name.lowercase() })
  }

  private fun dirSize(dir: File): Long = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

  fun countIn(dir: File): Int = dir.walkTopDown().filter { it.isFile }.count()

  fun totalSize(root: File): Long = root.walkTopDown().filter { it.isFile }.sumOf { it.length() }

  fun search(root: File, query: String): List<FileEntry> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    return root.walkTopDown()
      .filter { it.isFile || it.isDirectory }
      .filter { it != root && it.name.lowercase().contains(q) }
      .map { FileEntry(it.name, it, it.isDirectory, if (it.isDirectory) dirSize(it) else it.length(), it.lastModified()) }
      .sortedByDescending { it.modified }
      .toList()
  }

  fun createFolder(root: File, parent: File, name: String): File {
    val clean = sanitizeName(name)
    require(clean.isNotEmpty()) { "Name the folder." }
    val target = File(parent, clean)
    check(insideRoot(root, target))
    target.mkdirs()
    return target
  }

  fun rename(root: File, entry: FileEntry, newName: String): File {
    val clean = sanitizeName(newName)
    require(clean.isNotEmpty()) { "Name the file." }
    val target = File(entry.file.parentFile!!, clean)
    check(insideRoot(root, target))
    require(entry.file.renameTo(target)) { "Could not rename." }
    return target
  }

  fun moveTo(root: File, entry: FileEntry, targetDir: File): File {
    check(insideRoot(root, targetDir))
    val target = File(targetDir, entry.file.name)
    require(!target.exists()) { "A file with that name already exists there." }
    require(entry.file.renameTo(target)) { "Could not move." }
    return target
  }

  fun delete(root: File, file: File) {
    check(insideRoot(root, file) && file != root) { "Refusing to delete outside the app files." }
    file.deleteRecursively()
  }

  /** Writes bytes without ever overwriting: "name (2).pdf". */
  fun saveBytes(root: File, dir: File?, baseName: String, ext: String, bytes: ByteArray): File {
    val targetDir = dir ?: root
    check(insideRoot(root, targetDir))
    targetDir.mkdirs()
    val base = sanitizeName(baseName).ifEmpty { "output" }
    var target = File(targetDir, "$base.$ext")
    check(insideRoot(root, target)) { "Target would escape the app files." }
    var n = 2
    while (target.exists()) {
      target = File(targetDir, "$base ($n).$ext")
      n++
    }
    target.writeBytes(bytes)
    return target
  }

  /** File names must not escape the directory. */
  fun sanitizeName(name: String): String =
    name.trim().replace(Regex("[\\\\/:*?\"<>|\u0000-\u001F]"), "_").take(120).trim().trimEnd('.')

  fun mimeOf(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
    "pdf" -> "application/pdf"
    "zip" -> "application/zip"
    "txt" -> "text/plain"
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    else -> "application/octet-stream"
  }
}

package com.nekonaru.pedeef.data

import java.io.File

/** The most recently modified PDFs across the public and private folders (max 5). */
object RecentFiles {
  fun list(context: android.content.Context, max: Int = 5): List<FileEntry> {
    val public = PublicFiles.root(context)
    val private = FilesStore.root(context)
    val seen = HashSet<String>()
    val all = ArrayList<File>()
    for (root in listOfNotNull(public, private)) {
      root.walkTopDown().forEach { f ->
        if (f.isFile && f.extension.equals("pdf", ignoreCase = true)) {
          if (seen.add(f.absolutePath)) all.add(f)
        }
      }
    }
    return all
      .sortedByDescending { it.lastModified() }
      .take(max)
      .map { FileEntry(it.name, it, false, it.length(), it.lastModified()) }
  }
}

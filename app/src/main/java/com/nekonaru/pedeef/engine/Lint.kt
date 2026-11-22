package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.font.PDFont

/** A plain-text internal report: pages, version, encryption, fonts, XMP, producer and issue notes. */
fun lintPdf(name: String, bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val sb = StringBuilder()
  sb.append("Pedeef document check\n")
  sb.append("File: $name\n")

  fun row(label: String, value: String?) {
    if (!value.isNullOrBlank()) sb.append("$label: $value\n")
  }

  row("Version", doc.documentCatalog.version)
  row("Encrypted", if (doc.isEncrypted) "yes" else "no")
  row("Producer", doc.documentInformation.producer)
  row("Creator", doc.documentInformation.creator)
  row("Linearized", if (doc.documentCatalog.cosObject.getItem(COSName.getPDFName("Linearized")) != null) "yes" else "no")
  row("Pages", doc.numberOfPages.toString())

  val fonts = LinkedHashMap<String, MutableList<Int>>()
  val issues = ArrayList<String>()
  val seenNotEmbedded = ArrayList<String>()
  for (page in doc.pages) {
    val idx = doc.pages.indexOf(page) + 1
    val res = page.resources ?: continue
    for (resName in res.fontNames) {
      val font = runCatching { res.getFont(resName) }.getOrNull()
      if (font == null) {
        issues.add("Font '$resName' could not be loaded (page $idx)")
        continue
      }
      val fname = runCatching { font.fontDescriptor?.fontName }.getOrNull()?.ifBlank { null } ?: resName.name
      val embedded = when (font) {
        is com.tom_roush.pdfbox.pdmodel.font.PDType1Font -> runCatching { font.isEmbedded() }.getOrDefault(true)
        is com.tom_roush.pdfbox.pdmodel.font.PDType0Font -> runCatching { font.isEmbedded() }.getOrDefault(true)
        else -> true
      }
      val entry = fonts.getOrPut(fname) { mutableListOf() }
      entry.add(idx)
      if (!embedded && fname !in seenNotEmbedded) {
        seenNotEmbedded.add(fname)
        issues.add("Font '$fname' is not embedded")
      }
    }
  }
  row("XMP metadata present", if (doc.documentCatalog.cosObject.getItem(COSName.METADATA) != null) "yes" else "no")

  sb.append("\nFonts (${fonts.size} used)\n")
  if (fonts.isEmpty()) sb.append("(none)\n")
  fonts.entries.forEach { (name, pages) ->
    val uniq = pages.distinct()
    val range = when (uniq.size) {
      0 -> ""
      1 -> " (page ${uniq[0]})"
      else -> " (pages ${uniq.first()}-${uniq.last()})"
    }
    sb.append("- $name$range\n")
  }

  if (issues.isNotEmpty()) {
    sb.append("\nIssues\n")
    issues.forEach { sb.append("- $it\n") }
  } else {
    sb.append("\nIssues: none found\n")
  }
  return Output("lint", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
}

private val PDFont.fontDescriptor: com.tom_roush.pdfbox.pdmodel.font.PDFontDescriptor? get() = runCatching { getFontDescriptor() }.getOrNull()

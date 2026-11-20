package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.text.SimpleDateFormat
import java.util.Locale

/** Everything the file says about itself, as a plain-text report. */
fun reportPdf(name: String, bytes: ByteArray): ByteArray {
  loadPdf(bytes).use { doc ->
    val info = doc.documentInformation
    val sb = StringBuilder()
    fun row(label: String, value: String?) {
      if (!value.isNullOrBlank()) sb.append("$label: $value\n")
    }
    val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    fun cal(c: java.util.Calendar?): String = c?.time?.let(dateFmt::format) ?: "unknown"

    sb.append("Pedeef document report\n")
    sb.append("File: $name (${"%,d".format(bytes.size)} bytes)\n")
    sb.append("Pages: ${doc.numberOfPages}\n")
    runCatching { doc.getPage(0) }.getOrNull()?.let { page ->
      sb.append("First page: ${page.mediaBox.width.toInt()} x ${page.mediaBox.height.toInt()} pt, rotated ${page.rotation % 360}\u00B0\n")
    }
    sb.append("\nMetadata\n")
    row("Title", info.title)
    row("Author", info.author)
    row("Subject", info.subject)
    row("Keywords", info.keywords)
    row("Creator", info.creator)
    row("Producer", info.producer)
    row("Created", cal(info.creationDate))
    row("Modified", cal(info.modificationDate))

    // Embedded images across all pages
    var images = 0
    for (page in doc.pages) {
      val res = page.resources ?: continue
      for (resName in res.xObjectNames) {
        if (runCatching { res.getXObject(resName) }.getOrNull() is PDImageXObject) images++
      }
    }
    sb.append("\nContent\n")
    row("Embedded images", images.toString())

    // AcroForm fields, capped so a form-heavy document does not flood the report
    val form = runCatching { doc.documentCatalog.acroForm }.getOrNull()
    if (form != null && form.fields.isNotEmpty()) {
      sb.append("\nForm fields (${form.fields.size})\n")
      for (field in form.fields.take(200)) {
        val value = runCatching { field.valueAsString }.getOrNull().orEmpty().trim()
        sb.append("- ${field.fullyQualifiedName}: ${value.ifBlank { "(empty)" }}\n")
      }
      if (form.fields.size > 200) sb.append("- ... and ${form.fields.size - 200} more\n")
    }
    return Output("report", sb.toString().toByteArray(Charsets.UTF_8), ext = "txt").bytes
  }
}

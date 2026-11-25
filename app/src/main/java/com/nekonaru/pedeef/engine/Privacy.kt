package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.common.PDNameTreeNode
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import com.nekonaru.pedeef.data.FilesStore.sanitizeName

/** Removes every annotation (comments, highlights, links, form widgets) from all pages. */
fun removeAnnotations(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  for (page in doc.pages) page.setAnnotations(emptyList())
  // Document-level form fields can also hold data independently of page annotations.
  if (runCatching { doc.documentCatalog.acroForm }.getOrNull() != null) doc.documentCatalog.setAcroForm(null)
  // A certification signature can survive in /Root /Perms even with /AcroForm removed.
  doc.documentCatalog.cosObject.removeItem(COSName.getPDFName("Perms"))
  doc.bytes()
}

private const val MAX_ATTACHMENT_BYTES = 128L * 1024 * 1024

/** Extracts every embedded file attachment from the document name tree. */
fun extractAttachments(bytes: ByteArray): List<Output> {
  val outputs = ArrayList<Output>()
  val seen = HashSet<String>()
  var total = 0L
  var capped = false
  fun walk(node: PDNameTreeNode<PDComplexFileSpecification>?, depth: Int) {
    if (node == null || depth > 8 || capped) return
    runCatching { node.getNames() }.getOrNull()?.forEach { (key, spec) ->
      if (capped) return@forEach
      val data = runCatching { (spec.embeddedFileUnicode ?: spec.embeddedFile)?.toByteArray() }.getOrNull()
      if (data != null && seen.add(key)) {
        total += data.size
        if (total > MAX_ATTACHMENT_BYTES) { capped = true; return@forEach }
        // Prefer the file's own displayed name; the name-tree key is often just a number.
        val display = runCatching { spec.filename }.getOrNull()?.ifBlank { null } ?: key
        val safe = sanitizeName(display).ifEmpty { "attachment-${outputs.size + 1}" }
        val ext = safe.substringAfterLast('.', "").takeIf { it.length in 1..8 && it.all(Char::isLetterOrDigit) }.orEmpty()
        val base = if (ext.isEmpty()) safe else safe.removeSuffix(".$ext")
        outputs.add(Output(base, data, ext = ext.ifEmpty { "bin" }, full = true))
      }
    }
    if (!capped) runCatching { node.getKids() }.getOrNull()?.forEach { walk(it, depth + 1) }
  }
  loadPdf(bytes).use { doc ->
    walk(runCatching { doc.documentCatalog.names?.embeddedFiles }.getOrNull(), 0)
  }
  return outputs
}

package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.common.PDNameTreeNode
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import com.nekonaru.pedeef.data.FilesStore.sanitizeName

/** Removes every annotation (comments, highlights, links, form widgets) from all pages. */
fun removeAnnotations(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  var removed = 0
  for (page in doc.pages) {
    val before = runCatching { page.annotations.size }.getOrDefault(0)
    page.setAnnotations(emptyList())
    removed += before
  }
  // Document-level form fields can also hold data independently of page annotations.
  val form = runCatching { doc.documentCatalog.acroForm }.getOrNull()
  if (form != null) doc.documentCatalog.setAcroForm(null)
  doc.bytes()
}

/** Extracts every embedded file attachment from the document name tree. */
fun extractAttachments(bytes: ByteArray): List<Output> {
  val outputs = ArrayList<Output>()
  val seen = HashSet<String>()
  fun walk(node: PDNameTreeNode<PDComplexFileSpecification>?, depth: Int) {
    if (node == null || depth > 8) return
    runCatching { node.getNames() }.getOrNull()?.forEach { (name, spec) ->
      val data = runCatching { spec.embeddedFile?.toByteArray() }.getOrNull()
      if (data != null && seen.add(name)) {
        val safe = sanitizeName(name).ifEmpty { "attachment-${outputs.size + 1}" }
        outputs.add(Output(safe, data, ext = "bin", full = true))
      }
    }
    runCatching { node.getKids() }.getOrNull()?.forEach { walk(it, depth + 1) }
  }
  loadPdf(bytes).use { doc ->
    walk(runCatching { doc.documentCatalog.names?.embeddedFiles }.getOrNull(), 0)
  }
  return outputs
}

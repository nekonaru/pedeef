package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.common.PDMetadata

/**
 * Writes a minimal PDF/A identification marker into the document catalog (version 1.7 + XMP pdfaid
 * "PDF/A-1b"). This does not guarantee full PDF/A conformance: fonts still need embedding and the
 * file is not formally validated. Described as such in the tool copy.
 */
fun markForArchiving(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val xmp = """
    <?xpacket begin="?" id="W5M0MpCehiHzreSzNTczkc9d"?>
    <x:xmpmeta xmlns:x="adobe:ns:meta/">
      <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        <rdf:Description rdf:about=""
          xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
          <pdfaid:part>1</pdfaid:part>
          <pdfaid:conformance>B</pdfaid:conformance>
        </rdf:Description>
      </rdf:RDF>
    </x:xmpmeta>
    <?xpacket end="w"?>
  """.trimIndent().toByteArray(Charsets.UTF_8)
  doc.documentCatalog.version = "1.7"
  PDMetadata(doc, xmp.inputStream()).let { doc.documentCatalog.setMetadata(it) }
  doc.bytes()
}

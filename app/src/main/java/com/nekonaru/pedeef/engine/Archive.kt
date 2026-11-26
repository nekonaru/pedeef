package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.common.PDMetadata

/**
 * Writes a minimal PDF/A-2b identification marker (XMP pdfaid + version 1.7). Full conformance is
 * not guaranteed: fonts still need embedding and the file is not formally validated.
 */
fun markForArchiving(bytes: ByteArray): ByteArray = loadPdf(bytes).use { doc ->
  val xmp = """
    <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
    <x:xmpmeta xmlns:x="adobe:ns:meta/">
      <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        <rdf:Description rdf:about=""
          xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
          <pdfaid:part>2</pdfaid:part>
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

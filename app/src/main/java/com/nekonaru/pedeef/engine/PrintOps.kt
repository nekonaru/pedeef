package com.nekonaru.pedeef.engine

import android.content.Context
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.PageRange
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.File
import java.io.FileOutputStream

/** Sends a PDF to the system print service. The whole file is passed to the service untouched. Runs on a background thread. */
suspend fun printPdf(context: Context, bytes: ByteArray, name: String) {
  val tmp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    File(context.cacheDir, "print-${System.currentTimeMillis()}.pdf").apply { writeBytes(bytes) }
  }
  val adapter = PdfPrintAdapter(tmp, name.removeSuffix(".pdf") + ".pdf")
  val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
  val jobName = name.removeSuffix(".pdf").ifEmpty { "document" }
  pm.print(jobName, adapter, android.print.PrintAttributes.Builder().build())
}

private class PdfPrintAdapter(private val file: File, private val displayName: String) : PrintDocumentAdapter() {
  private fun pageCount(): Int = PDDocument.load(file).use { it.numberOfPages }

  /** The service may finish without writing (user dismissed the dialog); never leave plaintext behind. */
  override fun onFinish() {
    file.delete()
    super.onFinish()
  }

  override fun onLayout(
    oldAttributes: android.print.PrintAttributes?,
    newAttributes: android.print.PrintAttributes?,
    cancelSignal: android.os.CancellationSignal?,
    callback: LayoutResultCallback,
    extras: android.os.Bundle?,
  ) {
    callback.onLayoutFinished(
      PrintDocumentInfo.Builder(displayName)
        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
        .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
        .build(),
      true,
    )
  }

  override fun onWrite(pages: Array<PageRange>, destination: ParcelFileDescriptor, cancelSignal: android.os.CancellationSignal, callback: WriteResultCallback) {
    try {
      val total = pageCount()
      val all = pages.isEmpty() || (pages.size == 1 && pages[0].start == 0 && pages[0].end >= total - 1)
      val outBytes = if (all) file.readBytes() else rewritePages(pages, total)
      FileOutputStream(destination.fileDescriptor).use { it.write(outBytes) }
      file.delete()
      callback.onWriteFinished(pages)
    } catch (e: Exception) {
      file.delete()
      callback.onWriteFailed(e.message ?: "Print failed")
    }
  }

  private fun rewritePages(pages: Array<PageRange>, total: Int): ByteArray {
    val indices = pages.flatMap { range -> (range.start..minOf(range.end, total - 1)).toList() }.filter { it in 0 until total }.toSortedSet().toList()
    if (indices.isEmpty()) throw IllegalArgumentException("No pages in the selected range.")
    PDDocument.load(file).use { src ->
      com.tom_roush.pdfbox.pdmodel.PDDocument().use { out ->
        for (i in indices) out.adopt(src.getPage(i))
        java.io.ByteArrayOutputStream().use { out2 -> out.save(out2); return out2.toByteArray() }
      }
    }
  }

  private fun PDDocument.adopt(p: com.tom_roush.pdfbox.pdmodel.PDPage): com.tom_roush.pdfbox.pdmodel.PDPage {
    val n = importPage(p)
    if (!n.cosObject.containsKey(com.tom_roush.pdfbox.cos.COSName.RESOURCES)) n.resources = p.resources ?: com.tom_roush.pdfbox.pdmodel.PDResources()
    return n
  }
}

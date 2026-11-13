package com.nekonaru.pedeef.tools

import com.nekonaru.pedeef.engine.*
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.io.File

// Tool catalog: what each screen asks for and which engine op it runs. Bilingual (EN, ID).

typealias Opts = JsonObject

val NoOpts = JsonObject(emptyMap())
fun Opts.str(k: String): String = (this[k] as? JsonPrimitive)?.contentOrNull ?: ""
fun Opts.bool(k: String): Boolean = (this[k] as? JsonPrimitive)?.booleanOrNull ?: false
fun Opts.with(k: String, v: String): Opts = JsonObject(this + (k to JsonPrimitive(v)))
fun Opts.with(k: String, v: Boolean): Opts = JsonObject(this + (k to JsonPrimitive(v)))

/** Stores any serializable value as JSON under `key`. */
inline fun <reified T> Opts.put(k: String, v: T): Opts =
  JsonObject(this + (k to Json.encodeToJsonElement(v)))

sealed class OptDef(val key: String, val en: String, val id: String, val visible: ((Opts) -> Boolean)?) {
  class Seg(key: String, en: String, id: String, val choices: List<Triple<String, String, String>>, val def: String, visible: ((Opts) -> Boolean)? = null) :
    OptDef(key, en, id, visible)
  class Text(key: String, en: String, id: String, val def: String, val placeholder: String = "", val keyboard: Int = 0, val strip: Regex? = null, visible: ((Opts) -> Boolean)? = null) :
    OptDef(key, en, id, visible)
  class Toggle(key: String, en: String, id: String, val def: Boolean, visible: ((Opts) -> Boolean)? = null) :
    OptDef(key, en, id, visible)
}

fun defaults(defs: List<OptDef>): Opts = JsonObject(defs.associate {
  it.key to when (it) {
    is OptDef.Seg -> JsonPrimitive(it.def)
    is OptDef.Text -> JsonPrimitive(it.def)
    is OptDef.Toggle -> JsonPrimitive(it.def)
  }
})

enum class Input { Pdf, Pdfs, Images }

class Tool(
  val id: String,
  val section: Int,
  val titleEn: String, val titleId: String,
  val descEn: String, val descId: String,
  val ctaEn: String, val ctaId: String,
  val doingEn: String, val doingId: String,
  val input: Input,
  val min: Int = 1,
  val noPreview: Boolean = false,
  val opts: List<OptDef> = emptyList(),
  val run: (List<Named>, Opts) -> List<Output>,
) {
  fun title(indonesian: Boolean) = if (indonesian) titleId else titleEn
  fun desc(indonesian: Boolean) = if (indonesian) descId else descEn
  fun cta(indonesian: Boolean) = if (indonesian) ctaId else ctaEn
  fun doing(indonesian: Boolean) = if (indonesian) doingId else doingEn
}

/** Sections, in the order the jobs usually come: get pages in, put them in order, put something on them, finish. */
val SECTIONS = listOf(
  "secCapture" to ("Capture" to "Tangkap"),
  "secCombineSplit" to ("Combine & split" to "Gabung & pisah"),
  "secArrange" to ("Arrange pages" to "Atur halaman"),
  "secFillMarkup" to ("Fill & mark up" to "Isi & tandai"),
  "secProtectShrink" to ("Protect & shrink" to "Lindungi & perkecil"),
  "secAutomate" to ("Automate" to "Otomatisasi"),
  "secNew" to ("New" to "Baru"),
)
const val SECTION_CAPTURE = 0
const val SECTION_COMBINE = 1
const val SECTION_ARRANGE = 2
const val SECTION_FILL = 3
const val SECTION_PROTECT = 4
const val SECTION_AUTOMATE = 5
const val SECTION_NEW = 6

private fun seg(key: String, en: String, id: String, choices: List<Triple<String, String, String>>, def: String, visible: ((Opts) -> Boolean)? = null) =
  OptDef.Seg(key, en, id, choices, def, visible)

private fun text(key: String, en: String, id: String, def: String, placeholder: String = "", strip: Regex? = null, visible: ((Opts) -> Boolean)? = null) =
  OptDef.Text(key, en, id, def, placeholder, 0, strip, visible)

private fun num(key: String, en: String, id: String, def: String, placeholder: String) =
  text(key, en, id, def, placeholder, Regex("[^0-9.]"))

private fun pageList(key: String, en: String, id: String, placeholder: String, visible: ((Opts) -> Boolean)? = null) =
  text(key, en, id, "", placeholder, Regex("[^0-9,\\-\\s]"), visible)

private fun toggle(key: String, en: String, id: String, def: Boolean, visible: ((Opts) -> Boolean)? = null) =
  OptDef.Toggle(key, en, id, def, visible)

private object O {
  val squeeze = seg("mode", "Compress by", "Kompres berdasarkan",
    listOf(Triple("level", "Level", "Level"), Triple("size", "Fit under a size", "Sesuai uk")), "level")
  val quality = seg("quality", "Compression level", "Tingkat kompresi",
    listOf(Triple("80", "Light", "Ringan"), Triple("60", "Balanced", "Seimbang"), Triple("35", "Strong", "Kuat")), "60") { it.str("mode") != "size" }
  val target = num("targetMB", "Fit under (MB)", "Sesuai ukuran (MB)", "2", "2").let { def ->
    OptDef.Text(def.key, def.en, def.id, def.def, def.placeholder, 0, def.strip) { it.str("mode") == "size" }
  }
  val wmText = text("text", "Watermark text", "Teks watermark", "CONFIDENTIAL", "CONFIDENTIAL")
  val wmScale = seg("scale", "Size", "Ukuran",
    listOf(Triple("0.6", "Small", "Kecil"), Triple("1", "Medium", "Sedang"), Triple("1.4", "Large", "Besar")), "1")
  val opacity = seg("opacity", "Opacity", "Opasitas",
    listOf(Triple("0.1", "10%", "10%"), Triple("0.2", "20%", "20%"), Triple("0.35", "35%", "35%")), "0.2")
  val numPos = seg("position", "Position", "Posisi",
    listOf(Triple("left", "Left", "Kiri"), Triple("center", "Center", "Tengah"), Triple("right", "Right", "Kanan")), "center")
  val numFmt = seg("format", "Format", "Format",
    listOf(Triple("n", "1, 2, 3", "1, 2, 3"), Triple("of", "Page 1 of N", "Hal 1 dari N")), "n")
  val angle = seg("angle", "Rotate clockwise by", "Putar searah jarum jam",
    listOf(Triple("90", "90\u00B0", "90\u00B0"), Triple("180", "180\u00B0", "180\u00B0"), Triple("270", "270\u00B0", "270\u00B0")), "90")
  val axis = seg("axis", "Mirror", "Cerminkan",
    listOf(Triple("h", "Horizontal", "Horizontal"), Triple("v", "Vertical", "Vertikal"), Triple("both", "Both", "Keduanya")), "h")
  val perSheet = seg("perSheet", "Pages per sheet", "Halaman per lembar",
    listOf(Triple("2", "2", "2"), Triple("4", "4", "4"), Triple("6", "6", "6"), Triple("9", "9", "9"), Triple("16", "16", "16")), "4")
}

private fun one(suffix: String, fn: (ByteArray, Opts) -> ByteArray) = { files: List<Named>, o: Opts -> listOf(Output("${files[0].baseName}-$suffix", fn(files[0].bytes, o))) }
private fun many(fn: (ByteArray, Opts) -> List<Named>) = { files: List<Named>, o: Opts -> fn(files[0].bytes, o).map { Output(it.name, it.bytes) } }

/** Workflow steps are the tools themselves. Excluded: scanner, custom editors, non-PDF output, and password tools. */
private val NOT_STEPS = setOf("scan", "workflow", "organize", "redact", "fillSign", "zip", "password", "unlock", "metadata", "pdfToImages", "extractText")
fun stepTools() = TOOLS.filter { it.id !in NOT_STEPS }

private fun runWorkflow(files: List<Named>, steps: List<WorkflowStep>): List<Output> {
  if (steps.isEmpty()) fail("Add at least one step.")
  var cur = files.map { Named(it.baseName, it.bytes) }
  for (step in steps) {
    val t = toolById(step.op) ?: fail("A step in this workflow is no longer available. Remove it and try again.")
    val opts = JsonObject(step.opts)
    if (t.input == Input.Pdfs) {
      val base = cur[0].name
      cur = t.run(cur, opts).map { Named("$base-${it.name}", it.bytes) }
      continue
    }
    cur = cur.flatMap { f -> // one file at a time keeps memory flat
      val outs = t.run(listOf(f), opts)
      outs.map { Named(if (outs.size > 1) "${f.name}-${it.name}" else f.name, it.bytes) }
    }
  }
  return if (cur.size == 1) listOf(Output("processed", cur[0].bytes)) else cur.map { Output("${it.name}-processed", it.bytes, full = true) }
}

private fun imagesIn(files: List<Named>) = files.map { it.name to decodeScaled(it.bytes) }

val TOOLS: List<Tool> = listOf(
  Tool(
    "workflow", SECTION_AUTOMATE,
    "PDF Workflow Builder", "Pembuat Workflow PDF",
    "Chain tools in order on one or many PDFs, for example merge, watermark, compress.", "Rangkai alat berurutan pada satu atau banyak PDF.",
    "Run workflow", "Jalankan workflow", "Running steps", "Menjalankan langkah",
    Input.Pdfs, min = 1,
  ) { files, o -> runWorkflow(files, o["steps"]?.let { Json.decodeFromJsonElement<List<WorkflowStep>>(it) } ?: emptyList()) },
  Tool(
    "merge", SECTION_COMBINE,
    "Merge PDFs", "Gabung PDF",
    "Combine multiple PDFs into one document.", "Gabungkan beberapa PDF menjadi satu dokumen.",
    "Merge PDFs", "Gabung PDF", "Merging", "Menggabungkan", Input.Pdfs,
  ) { files, _ -> listOf(Output("merged", merge(files))) },
  Tool(
    "alternate", SECTION_COMBINE,
    "Alternate & Mix PDF", "Selang-seling & Campur PDF",
    "Interleave the pages of two or more PDFs, one page from each in turn.", "Selingi halaman dua PDF atau lebih, satu halaman bergiliran.",
    "Mix pages", "Campur halaman", "Interleaving pages", "Menyisipkan halaman", Input.Pdfs,
    opts = listOf(toggle("reverse", "Reverse later files (back sides of a duplex scan)", "Balik file berikutnya (sisi belakang hasil pindai dupleks)", false)),
  ) { files, o -> listOf(Output("mixed", alternate(files, o.bool("reverse")))) },
  Tool(
    "compress", SECTION_PROTECT,
    "Compress PDF", "Kompres PDF",
    "Shrink the photos and scans inside a PDF. Text stays sharp.", "Perkecil foto dan hasil pindai di dalam PDF. Teks tetap tajam.",
    "Compress PDF", "Kompres PDF", "Recompressing images", "Mengompres ulang gambar", Input.Pdf,
    opts = listOf(O.squeeze, O.quality, O.target),
  ) { files, o ->
    val b = files[0].bytes
    listOf(Output("compressed", if (o.str("mode") == "size") compressToSize(b, o.str("targetMB")) else compress(b, o.str("quality").toIntOrNull() ?: 60)))
  },
  Tool(
    "split", SECTION_COMBINE,
    "Split PDF", "Pisah PDF",
    "Every page as a file, or extract custom ranges.", "Setiap halaman menjadi file, atau rentang kustom.",
    "Split PDF", "Pisah PDF", "Splitting", "Memisahkan", Input.Pdf,
    opts = listOf(
      seg("mode", "Split into", "Pisahkan menjadi",
        listOf(Triple("every", "Every page", "Setiap halaman"), Triple("ranges", "Custom ranges", "Rentang kustom")), "every"),
      pageList("ranges", "Page ranges (one file per range)", "Rentang halaman (satu file per rentang)", "1-3, 4-6, 7-") { it.str("mode") == "ranges" },
    ),
    run = many { b, o -> split(b, o.str("mode"), o.str("ranges")) },
  ),
  Tool(
    "splitText", SECTION_COMBINE,
    "Split PDF by Text", "Pisah PDF berdasarkan Teks",
    "Start a new file at every page containing a phrase you choose.", "Mulai file baru pada setiap halaman yang memuat frasa pilihanmu.",
    "Split by text", "Pisah berdasarkan teks", "Reading pages", "Membaca halaman", Input.Pdf,
    opts = listOf(text("phrase", "Split at pages containing", "Pisahkan pada halaman yang memuat", "", "e.g. Invoice No.")),
    run = many { b, o -> splitByText(b, o.str("phrase")) },
  ),
  Tool(
    "splitBookmarks", SECTION_COMBINE,
    "Split PDF by Bookmarks", "Pisah PDF berdasarkan Bookmark",
    "Turn each bookmarked section into its own PDF.", "Ubah setiap bagian bookmark menjadi PDF tersendiri.",
    "Split by bookmarks", "Pisah berdasarkan bookmark", "Reading bookmarks", "Membaca bookmark", Input.Pdf,
    run = many { b, _ -> splitByBookmarks(b) },
  ),
  Tool(
    "splitHalf", SECTION_COMBINE,
    "Split PDF in Half", "Pisah PDF Jadi Dua",
    "Cut into two files, or slice every page down the middle.", "Bagi menjadi dua file, atau iris setiap halaman di tengah.",
    "Split in half", "Pisah jadi dua", "Cutting pages", "Memotong halaman", Input.Pdf,
    opts = listOf(seg("mode", "How to split", "Cara memisah",
      listOf(Triple("file", "Two files", "Dua file"), Triple("vertical", "Left | Right", "Kiri | Kanan"), Triple("horizontal", "Top / Bottom", "Atas / Bawah")), "file")),
    run = many { b, o -> splitHalf(b, o.str("mode")) },
  ),
  Tool(
    "splitSize", SECTION_COMBINE,
    "Split PDF by Size", "Pisah PDF berdasarkan Ukuran",
    "Break a large PDF into parts under a size limit.", "Bagi PDF besar menjadi bagian di bawah batas ukuran.",
    "Split by size", "Pisah berdasarkan ukuran", "Measuring parts", "Mengukur bagian", Input.Pdf,
    opts = listOf(num("maxMB", "Max size per part (MB)", "Ukuran maksimum per bagian (MB)", "5", "5")),
    run = many { b, o -> splitBySize(b, o.str("maxMB")) },
  ),
  Tool(
    "zip", SECTION_COMBINE,
    "PDF to ZIP", "PDF ke ZIP",
    "Bundle multiple PDFs into a single ZIP archive.", "Gabungkan beberapa PDF ke satu arsip ZIP.",
    "Create ZIP", "Buat ZIP", "Zipping", "Membuat ZIP", Input.Pdfs, min = 1, noPreview = true,
  ) { files, _ -> listOf(Output("bundle", zipFiles(files), ext = "zip")) },
  Tool(
    "organize", SECTION_ARRANGE,
    "Organize Pages", "Atur Halaman",
    "Rotate, delete, reorder or rearrange PDF pages.", "Putar, hapus, atau urutkan ulang halaman PDF.",
    "Save pages", "Simpan halaman", "Saving page order", "Menyimpan urutan halaman", Input.Pdf, noPreview = true,
  ) { files, o ->
    val order = o["order"]?.let { Json.decodeFromJsonElement<List<PageOrder>>(it) } ?: emptyList()
    listOf(Output("organized", organize(files[0].bytes, order)))
  },
  Tool(
    "rotate", SECTION_ARRANGE,
    "Rotate PDF", "Putar PDF",
    "Rotate pages 90, 180 or 270 degrees.", "Putar halaman 90, 180 atau 270 derajat.",
    "Rotate PDF", "Putar PDF", "Rotating", "Memutar", Input.Pdf,
    opts = listOf(O.angle, pageList("pages", "Pages (blank for all)", "Halaman (kosongkan untuk semua)", "e.g. 1, 3-5")),
    run = one("rotated") { b, o -> rotate(b, o.str("angle").toIntOrNull() ?: 90, o.str("pages")) },
  ),
  Tool(
    "flip", SECTION_ARRANGE,
    "Flip PDF", "Balik PDF",
    "Mirror every page horizontally, vertically or both.", "Cerminkan setiap halaman secara horizontal, vertikal, atau keduanya.",
    "Flip PDF", "Balik PDF", "Mirroring pages", "Mencerminkan halaman", Input.Pdf,
    opts = listOf(O.axis),
    run = one("flipped") { b, o -> flip(b, o.str("axis")) },
  ),
  Tool(
    "nup", SECTION_ARRANGE,
    "Multiple Pages Per Sheet", "Beberapa Halaman per Lembar",
    "Place 2, 4, 6, 9 or 16 PDF pages on a single sheet.", "Tempatkan 2, 4, 6, 9 atau 16 halaman PDF pada satu lembar.",
    "Arrange on sheets", "Atur di lembar", "Laying out sheets", "Menyusun lembar", Input.Pdf,
    opts = listOf(O.perSheet, toggle("lines", "Thin lines between pages", "Garis tipis antarhalaman", false)),
    run = one("n-up") { b, o -> nUp(b, o.str("perSheet"), o.bool("lines")) },
  ),
  Tool(
    "crop", SECTION_ARRANGE,
    "Crop & Resize", "Pangkas & Ubah Ukuran",
    "Crop margins or resize every page to A4, Letter or Legal.", "Pangkas margin atau ubah ukuran setiap halaman ke A4, Letter atau Legal.",
    "Apply", "Terapkan", "Applying", "Menerapkan", Input.Pdf,
    opts = listOf(
      seg("mode", "Mode", "Mode", listOf(Triple("crop", "Crop pages", "Pangkas halaman"), Triple("resize", "Resize pages", "Ubah ukuran halaman")), "crop"),
      seg("size", "Page size", "Ukuran halaman", SIZES.keys.map { Triple(it, it, it) }, "A4") { it.str("mode") == "resize" },
    ),
    run = one("resized") { b, o ->
      val boxes = o["boxes"]?.let { Json.decodeFromJsonElement<Map<String, Box>>(it) } ?: emptyMap()
      cropResize(b, o.str("mode").ifEmpty { "crop" }, o.str("size").ifEmpty { "A4" }, boxes)
    },
  ),
  Tool(
    "fillSign", SECTION_FILL,
    "Fill & Sign", "Isi & Tanda Tangan",
    "Type onto a form and drop in a picture of your signature.", "Ketik pada formulir dan letakkan gambar tanda tanganmu.",
    "Apply", "Terapkan", "Writing on pages", "Menulis pada halaman", Input.Pdf, noPreview = true,
  ) { files, o ->
    val items = o["items"]?.let { Json.decodeFromJsonElement<List<SignItem>>(it) } ?: emptyList()
    if (items.isEmpty()) fail("Add some text or a picture to a page first.")
    val images = HashMap<String, ByteArray>()
    for (item in items) {
      if (item.kind != "image" || item.uri in images) continue
      val f = File(item.uri)
      if (!f.exists()) fail("A picture added on page ${item.page + 1} is no longer available. Remove it and add it again.")
      images[item.uri] = f.readBytes()
    }
    listOf(Output("signed", fillSign(files[0].bytes, items, images)))
  },
  Tool(
    "watermark", SECTION_FILL,
    "Add Watermark", "Tambah Watermark",
    "Stamp diagonal text like CONFIDENTIAL across every page.", "Beri teks diagonal seperti CONFIDENTIAL pada setiap halaman.",
    "Add watermark", "Tambah watermark", "Stamping pages", "Memberi watermark", Input.Pdf,
    opts = listOf(O.wmText, O.wmScale, O.opacity),
    run = one("watermarked") { b, o -> watermark(b, o.str("text"), o.str("opacity").toFloatOrNull() ?: 0.2f, o.str("scale").toFloatOrNull() ?: 1f) },
  ),
  Tool(
    "pageNumbers", SECTION_FILL,
    "Add Page Numbers", "Tambah Nomor Halaman",
    "Number every page at the bottom, left, center or right.", "Beri nomor setiap halaman di bawah: kiri, tengah atau kanan.",
    "Add page numbers", "Tambah nomor halaman", "Numbering pages", "Memberi nomor halaman", Input.Pdf,
    opts = listOf(O.numPos, O.numFmt),
    run = one("numbered") { b, o -> pageNumbers(b, o.str("position"), o.str("format")) },
  ),
  Tool(
    "redact", SECTION_FILL,
    "Redact PDF", "Redaksi PDF",
    "Black out anything private for good, removed from the file.", "Hitamkan informasi pribadi secara permanen, terhapus dari file.",
    "Redact PDF", "Redaksi PDF", "Blacking out", "Menghitamkan", Input.Pdf, noPreview = true,
  ) { files, o ->
    val boxes = o["boxes"]?.let { Json.decodeFromJsonElement<Map<String, List<Box>>>(it) } ?: emptyMap()
    if (boxes.isEmpty()) fail("Drag across at least one area to black it out.")
    listOf(Output("redacted", redactPdf(files[0].bytes, boxes, ToolContext.cacheDir)))
  },
  Tool(
    "metadata", SECTION_FILL,
    "Edit PDF Details", "Edit Detail PDF",
    "Change the title, author, subject and keywords.", "Ubah judul, penulis, subjek dan kata kunci.",
    "Save details", "Simpan detail", "Saving details", "Menyimpan detail", Input.Pdf, noPreview = true,
    opts = listOf(
      text("title", "Title", "Judul", ""),
      text("author", "Author", "Penulis", ""),
      text("subject", "Subject", "Subjek", ""),
      text("keywords", "Keywords", "Kata kunci", ""),
    ),
  ) { files, o -> listOf(Output("details", writeDetails(files[0].bytes, o.str("title"), o.str("author"), o.str("subject"), o.str("keywords")))) },
  Tool(
    "ocr", SECTION_PROTECT,
    "OCR Scanned PDF", "OCR PDF Pindai",
    "Read the text in a scanned PDF and lay it invisibly over the pages.", "Baca teks pada PDF pindai dan lapiskan secara tak kasat mata pada halaman.",
    "Make searchable", "Jadikan dapat dicari", "Reading pages", "Membaca halaman", Input.Pdf,
  ) { files, _ -> listOf(Output("searchable", ocrPdf(files[0].bytes, ToolContext.cacheDir))) },
  Tool(
    "unlock", SECTION_PROTECT,
    "Remove Password", "Hapus Kata Sandi",
    "Save a copy that opens without a password and no restrictions.", "Simpan salinan yang terbuka tanpa kata sandi dan batasan.",
    "Remove password", "Hapus kata sandi", "Unlocking", "Membuka kunci", Input.Pdf, noPreview = true,
  ) { files, _ -> listOf(Output("unlocked", removePassword(files[0].bytes))) },
  Tool(
    "password", SECTION_PROTECT,
    "Set Password", "Setel Kata Sandi",
    "Lock a PDF so it asks for a password before it opens.", "Kunci PDF agar meminta kata sandi saat dibuka.",
    "Set password", "Setel kata sandi", "Encrypting", "Mengenkripsi", Input.Pdf, noPreview = true,
    opts = listOf(text("password", "Password", "Kata sandi", "", "Type a password")),
  ) { files, o ->
    val pw = o.str("password")
    if (pw.isEmpty()) fail("Enter a password.")
    listOf(Output("protected", setPdfPassword(files[0].bytes, pw) ?: fail("This PDF already has a password.")))
  },
  // New in Pedeef
  Tool(
    "imagesToPdf", SECTION_NEW,
    "Images to PDF", "Gambar ke PDF",
    "Turn JPG and PNG images into one PDF, one page per image.", "Ubah gambar JPG dan PNG menjadi satu PDF, satu halaman per gambar.",
    "Create PDF", "Buat PDF", "Building PDF", "Membuat PDF", Input.Images, noPreview = true,
  ) { files, _ -> listOf(Output("images", imagesToPdf(imagesIn(files)))) },
  Tool(
    "pdfToImages", SECTION_NEW,
    "PDF to Images", "PDF ke Gambar",
    "Export every page as a JPEG image, or a ZIP for many pages.", "Ekspor setiap halaman sebagai JPEG, atau ZIP untuk banyak halaman.",
    "Export images", "Ekspor gambar", "Rendering pages", "Merender halaman", Input.Pdf, noPreview = true,
    run = { files, _ -> pdfToJpegs(files[0].bytes, ToolContext.cacheDir) },
  ),
  Tool(
    "extractText", SECTION_NEW,
    "Extract Text", "Ekstrak Teks",
    "Copy all text from a PDF into a .txt file.", "Salin semua teks dari PDF ke file .txt.",
    "Extract text", "Ekstrak teks", "Reading pages", "Membaca halaman", Input.Pdf, noPreview = true,
    run = { files, _ ->
      val text = extractText(files[0].bytes)
      if (text.isBlank()) fail("This PDF has no extractable text. If it is a scan, run OCR Scanned PDF first.")
      listOf(Output("text", text.toByteArray(Charsets.UTF_8), ext = "txt"))
    },
  ),
)

fun toolById(id: String?) = TOOLS.find { it.id == id }

/** Cache directory shared with the catalog (set once at app start). */
object ToolContext {
  lateinit var cacheDir: File
}

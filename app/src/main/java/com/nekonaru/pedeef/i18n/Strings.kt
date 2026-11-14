package com.nekonaru.pedeef.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** Every user-facing string, English first then Bahasa Indonesia below. */
@Immutable
class Strings(
  // common
  val back: String, val save: String, val share: String, val open: String, val delete: String,
  val rename: String, val cancel: String, val done: String, val ok: String, val search: String, val apply: String,
  // home
  val homeTagline: String, val pinned: String, val allTools: String, val searchHint: String,
  val noMatches: String, val pin: String, val unpin: String, val scan: String, val files: String, val settings: String,
  val onboardingTitle: String, val onboardingBody: String, val onboardingStart: String,
  // sections
  val secCapture: String, val secCombineSplit: String, val secArrange: String, val secFillMarkup: String,
  val secProtectShrink: String, val secAutomate: String, val secNew: String,
  // tool screen
  val preview: String, val resultReady: String, val nothingYet: String, val pickFiles: String,
  val pickImages: String, val runBusy: String, val savedTo: String, val shareFile: String, val tryAgain: String,
  val passwordPrompt: String, val passwordWrong: String, val unlock: String,
  val batch: String, val batchHint: String, val openResult: String, val imagesToPdfNote: String,
  // viewer
  val viewerSearchHint: String, val copyPageText: String, val pageTextCopied: String, val jumpToPage: String,
  val pageOf: String, val openElsewhere: String, val textNotFound: String, val noTextOnPage: String,
  val searchResults: String, val prevMatch: String, val nextMatch: String,
  // files
  val filesTitle: String, val filesEmpty: String, val newFolder: String, val folderName: String,
  val saveAs: String, val moveTo: String, val savedCopies: String, val exportTo: String,
  // settings
  val settingsTitle: String, val appearance: String, val themeSystem: String, val themeLight: String, val themeDark: String,
  val scheme: String, val schemeEmber: String, val schemeCherry: String, val schemeArctic: String,
  val dynamicColor: String, val dynamicColorHint: String, val language: String, val about: String,
  val versionLabel: String, val privacyPolicy: String, val openSource: String, val clearAppData: String,
  val clearAppDataConfirm: String, val appDataCleared: String,
  // scanner
  val scanFailed: String, val playServicesMissing: String, val importImages: String, val makeSearchable: String,
  val scanFilterColor: String, val scanFilterGray: String, val scanFilterBw: String, val scanFilterWhiteboard: String,
  val scanSave: String, val scanEmpty: String, val buildingPdf: String, val ocrInProgress: String,
  // errors
  val genericError: String, val cannotOpenPdf: String, val fileTooLarge: String,
  // storage access
  val storageAccessTitle: String, val storageAccessBody: String, val storageAccessButton: String,
  val storageAccessHint: String, val storageAccessDenied: String, val storagePrivateFallback: String,
  val savedToDownload: String, val savedToApp: String,
  // update check and sponsor
  val versionRow: String, val checkingUpdates: String, val upToDate: String, val updateAvailable: String,
  val checkFailed: String, val sponsor: String, val sponsorBody: String,
  val developer: String, val viewOnGithub: String,
)

val En = Strings(
  // common
  "Back", "Save", "Share", "Open", "Delete",
  "Rename", "Cancel", "Done", "OK", "Search", "Apply",
  // home
  "Every PDF tool you need, on your phone.", "Pinned", "All tools", "Search tools",
  "No tools match that search.", "Pin", "Unpin", "Scan", "Files", "Settings",
  "Welcome to Pedeef", "Every tool runs on your phone. No account, no network, no ads. Your documents never leave the device unless you share them yourself.", "Get started",
  // sections
  "Capture", "Combine & split", "Arrange pages", "Fill & mark up",
  "Protect & shrink", "Automate", "New",
  // tool screen
  "Preview", "Result is ready", "Pick files to get started.", "Pick PDFs",
  "Pick images", "Working…", "Saved", "Share file", "Try again",
  "This PDF has a password.", "Wrong password.", "Unlock",
  "Batch", "Run on every selected PDF", "Open", "Images become one page each.",
  // viewer
  "Search in document", "Copy page text", "Page text copied", "Jump to page",
  "of", "Open in another app", "Text not found", "This page has no text",
  "matches", "Previous match", "Next match",
  // files
  "Files", "Saved files will appear here.", "New folder", "Folder name",
  "Save as…", "Move to…", "Saved copies", "Export to…",
  // settings
  "Settings", "Appearance", "System", "Light", "Dark",
  "Color scheme", "Ember", "Black Cherry", "Arctic Blue",
  "Dynamic color", "Use your wallpaper colors (Android 12+)", "Language", "About",
  "Version", "Privacy policy", "Open source licenses", "Clear saved files",
  "Delete every file Pedeef saved? This cannot be undone.", "Saved files deleted",
  // scanner
  "Scan failed. Try the gallery import instead.", "The document scanner needs Google Play services. You can import images from the gallery instead.", "Import images", "Make searchable (OCR)",
  "Color", "Grayscale", "Black & white", "Whiteboard",
  "Save PDF", "Add pages to scan or import first.", "Building PDF…", "Reading text…",
  // errors
  "Something went wrong. Try again.", "Could not open this PDF.", "This file is too large to process.",
  // storage access
  "File access", "Pedeef stores your PDFs and scans in Download/Pedeef so you can find them.", "Allow file access",
  "You can find every file Pedeef saves in Download/Pedeef with your file manager.", "Without access, files stay inside the app.", "Use app storage instead",
  "Download/Pedeef", "app storage",
  // update check and sponsor
  "Check for updates", "Checking…", "You are on the latest version.", "Version %1\$s is available.",
  "Could not check. Check your connection.", "Sponsor", "Pedeef is free and always will be. If it is useful to you, you can support the developer:",
  "Developer", "View on GitHub",
)

val Id = Strings(
  // common
  "Kembali", "Simpan", "Bagikan", "Buka", "Hapus",
  "Ganti nama", "Batal", "Selesai", "OK", "Cari", "Terapkan",
  // home
  "Semua alat PDF yang kamu butuhkan, di ponselmu.", "Disematkan", "Semua alat", "Cari alat",
  "Tidak ada alat yang cocok.", "Sematkan", "Lepas sematan", "Pindai", "File", "Pengaturan",
  "Selamat datang di Pedeef", "Semua alat berjalan di ponselmu. Tanpa akun, tanpa jaringan, tanpa iklan. Dokumenmu tidak pernah keluar dari perangkat kecuali kamu sendiri yang membagikannya.", "Mulai",
  // sections
  "Tangkap", "Gabung & pisah", "Atur halaman", "Isi & tandai",
  "Lindungi & perkecil", "Otomatisasi", "Baru",
  // tool screen
  "Pratinjau", "Hasil sudah siap", "Pilih file untuk mulai.", "Pilih PDF",
  "Pilih gambar", "Memproses…", "Tersimpan", "Bagikan file", "Coba lagi",
  "PDF ini memiliki kata sandi.", "Kata sandi salah.", "Buka kunci",
  "Batch", "Jalankan untuk setiap PDF yang dipilih", "Buka", "Gambar menjadi satu halaman per gambar.",
  // viewer
  "Cari di dokumen", "Salin teks halaman", "Teks halaman tersalin", "Lompat ke halaman",
  "dari", "Buka di aplikasi lain", "Teks tidak ditemukan", "Halaman ini tidak memiliki teks",
  "hasil", "Hasil sebelumnya", "Hasil berikutnya",
  // files
  "File", "File yang tersimpan akan muncul di sini.", "Folder baru", "Nama folder",
  "Simpan sebagai…", "Pindahkan ke…", "Salinan tersimpan", "Ekspor ke…",
  // settings
  "Pengaturan", "Tampilan", "Sistem", "Terang", "Gelap",
  "Skema warna", "Ember", "Black Cherry", "Arctic Blue",
  "Warna dinamis", "Ikuti warna wallpaper (Android 12+)", "Bahasa", "Tentang",
  "Versi", "Kebijakan privasi", "Lisensi sumber terbuka", "Hapus file tersimpan",
  "Hapus semua file yang disimpan Pedeef? Tindakan ini tidak bisa dibatalkan.", "File tersimpan dihapus",
  // scanner
  "Pemindaian gagal. Coba impor dari galeri.", "Pemindai dokumen membutuhkan Google Play services. Kamu bisa mengimpor gambar dari galeri.", "Impor gambar", "Jadikan dapat dicari (OCR)",
  "Warna", "Skala abu", "Hitam putih", "Papan tulis",
  "Simpan PDF", "Tambahkan halaman untuk memindai atau mengimpor dulu.", "Membuat PDF…", "Membaca teks…",
  // errors
  "Terjadi kesalahan. Coba lagi.", "Tidak bisa membuka PDF ini.", "File ini terlalu besar untuk diproses.",
  // storage access
  "Akses file", "Pedeef menyimpan PDF dan pindaianmu di Download/Pedeef supaya mudah ditemukan.", "Izinkan akses file",
  "Kamu bisa menemukan semua file yang disimpan Pedeef di Download/Pedeef lewat file manager.", "Tanpa akses, file tersimpan di dalam aplikasi.", "Pakai penyimpanan aplikasi saja",
  "Download/Pedeef", "penyimpanan aplikasi",
  // update check and sponsor
  "Periksa pembaruan", "Memeriksa…", "Kamu memakai versi terbaru.", "Versi %1\$s tersedia.",
  "Tidak bisa memeriksa. Periksa koneksimu.", "Sponsor", "Pedeef gratis dan akan selalu gratis. Kalau kamu merasa terbantu, kamu bisa mendukung pengembangnya:",
  "Pengembang", "Lihat di GitHub",
)

val LocalPedeefStrings = staticCompositionLocalOf { En }

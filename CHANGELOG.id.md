# Changelog

Semua perubahan penting pada Pedeef dicatat di sini. Formatnya mengikuti [Keep a Changelog](https://keepachangelog.com).

## v2.1.0

Rilis desain dan kenyamanan: token gerak bersama, ikon alat yang lebih jelas, kontrol viewer yang lebih tenang, dan umpan balik yang lebih cepat di semua layar.

### Peningkatan

- **Sistem desain:** satu skala jarak dan radius, satu kosakata gerak yang mengikuti pengaturan animasi sistem, serta tulisan tangan untuk nama aplikasi.
- **Ikon alat:** ke-75 alat punya ikon tersendiri, dijaga oleh unit test.
- **Home:** tombol hapus pencarian, label offline, aksi cepat berikon, skeleton file terbaru, dan pemisah tanggal yang sudah benar di kartu terbaru.
- **Viewer:** aksi sekunder pindah ke menu, kontrol yang hilang saat membaca dan muncul kembali saat disentuh, indikator halaman beranimasi, label dua halaman, dan bottom sheet bookmark.
- **Layar lain:** breadcrumb seksi di alat, breadcrumb jalur di Files, bottom sheet untuk privasi dan lisensi, umpan balik progres di Batch dan Scanner, serta konfirmasi Snackbar menggantikan teks inline.
- **Navigasi:** posisi tab tersimpan, back prediktif di Android 13, dan transisi layar berarah.

### Perbaikan bug

- **Viewer dua halaman:** tombol halaman berhenti di pasangan terakhir dan tidak menggulir melewati dokumen.
- **Batch:** tombol jalankan dinonaktifkan saat batch berjalan sehingga dua proses tidak bisa tumpang tindih.
- **File terbaru:** status pemuatan hilang meski pemindaian file gagal.

## v2.0.0

Rilis mayor: 24 alat PDF baru, preset dan pemrosesan batch, workspace yang lebih kaya, serta viewer dua halaman.

### Fitur baru

- **24 alat baru.** Cap gambar, sorot frasa, ganti halaman, timpa PDF, booklet, petak poster, sisip halaman kosong, duplikat halaman, bingkai halaman, nomor Bates, cap teks, hitam putih, laporan halaman kosong, ekstrak komentar, hapus lampiran, hapus JavaScript, proteksi terperinci, buka dengan kata sandi pemilik, PDF ke gambar panjang, gambar ke gambar panjang, teks bermarker halaman, manifest checksum, cek integritas, dan bersihkan semua.
- **Preset.** Simpan konfigurasi alat dengan nama dan muat ulang dari layar alat yang sama.
- **Batch runner.** Pilih preset dan jalankan ke maksimal 100 PDF di pohon file. Hasil masuk folder bertanggal dengan laporan teks.
- **Workspace Files.** Pilih banyak file untuk hapus massal, bintangi file penting, dan pilih semua.
- **Riwayat pencarian.** Frasa terakhir tampil sebagai chip di Home dan bisa dihapus.
- **Aksi cepat.** Batch runner dan empat alat terpin paling sering tampil di atas grid alat.
- **Dasar Viewer Pro.** Mode buku dua halaman dan grid thumbnail penuh, melengkapi strip thumbnail dan zoom yang sudah ada.
- **Input.Any.** Alat kini bisa menerima PDF dan gambar dalam satu pemilih.

### Peningkatan

- **Katalog alat:** 75 alat, tes katalog dan README EN/ID diperbarui; suite unit kini 54 tes.
- **Pemakaian ulang engine:** penempatan halaman, form embedding, dan operasi pembersihan memakai helper internal yang sama dengan alat lama.
- **Spek dan review:** setiap alat baru mengikuti pola English source, Indonesian translation.

### Perbaikan bug

- **Tidak ada regresi yang diketahui saat rilis.** Kode baru lolos 54 unit test dan lint sebelum CI.

## v1.23.0

Pisah di Halaman Kosong dan Ambil Halaman Ganjil/Genap, plus audit ronde kelimabelas.

### Fitur baru

- **Pisah di Halaman Kosong.** Mendeteksi halaman pemisah yang benar-benar kosong pada pindai dan memisah PDF menjadi bagian-bagian, membuang pemisahnya. Dibatasi 500 halaman yang dianalisis.
- **Ambil Halaman Ganjil/Genap.** Mengambil hanya halaman ganjil atau genap, untuk depan atau belakang pindai duplex.

### Peningkatan

- **Katalog alat:** 51 alat; tes katalog dan README EN/ID diperbarui.

### Perbaikan bug

- **Nomor halaman dan penanda di halaman 180 derajat (High).** `unrotate(180)` tidak membalik y, sehingga nomor halaman, header/footer, dan penempatan crop salah sisi. Diperbaiki beserta ekspektasi unit test.
- **Paket XMP arsip (Medium).** Memakai atribut `begin` kosong yang sesuai spesifikasi serta menyatakan PDF/A-2b (sesuai PDF 1.7), bukan kombinasi PDF/A-1b + 1.7 yang kontradiktif.
- **Bacakan Keras mulai dua kali (Medium).** Effect bicara tidak lagi menulis salah satu kuncinya sendiri, mencegah flush/ulang halaman yang sama.
- **PDF terkunci di thread UI (Medium).** Percobaan kata sandi kosong dan dialog kata sandi kini berjalan di dispatcher latar.
- **Ketuk file terbaru (Low).** Membaca PDF terbaru kini di luar thread utama.
- **Spinner scanner (Low).** `finish()` yang mengelola state busy; blok `finally` pemanggil tidak membatalkannya lebih awal.
- **Laporan ukuran halaman (Low).** Membulatkan alih-alih memotong dan melewati dimensi halaman tidak valid.

## v1.22.0

Salinan Arsip dan Laporan Ukuran Halaman, plus audit ronde keempatbelas dengan perbaikan High pada Pangkas Margin dan permintaan teks halaman.

### Fitur baru

- **Salinan Arsip.** Menulis penanda identifikasi XMP PDF/A-1b minimal dan menetapkan versi PDF 1.7. Konformitas penuh tidak divalidasi (tanpa cek font), dan alat menyatakannya.
- **Laporan Ukuran Halaman.** Mencantumkan setiap ukuran halaman yang berbeda dalam point, milimeter, dan inci.

### Peningkatan

- **Katalog alat:** 49 alat; tes katalog dan README EN/ID diperbarui.

### Perbaikan bug

- **Pangkas Margin sumbu vertikal terbalik (High).** Batas piksel berarah atas-bawah sedangkan ruang form PDF bawah-atas; jendela pangkas kini dikonversi ke koordinat bawah-atas sebelum penempatan form.
- **Pangkas Margin mengabaikan rotasi halaman (High).** Form hasil pangkas kini ditempatkan dengan helper sadar-rotasi; batas tampilan dipakai pada halaman sebagaimana ditampilkan.
- **Salin/Bacakan Keras no-op pada ketukan pertama (High).** Token disimpan sampai teks halaman yang diminta siap, bukan dikonsumsi saat `pageText` sementara null.
- **Teks halaman lama saat PDF masuk (Medium).** `pageText` dan `pageTextIndex` kini ber-kunci versi payload.
- **Header/footer di halaman terputar (Medium).** Memakai penempatan sadar-rotasi yang sama dengan nomor halaman.
- **Sapuan cache.** Prefix `trim-` ditambahkan, sehingga force-stop saat Pangkas Margin tidak menyisakan salinan PDF plaintext.
- **Pisah Tiap N Halaman** kini menamai bagian satu halaman `page-N` secara konsisten.

## v1.21.0

Dua alat tata letak halaman (Pisah Tiap N Halaman, Pangkas Margin), plus audit ronde ketigabelas dengan perbaikan High pada penempatan header/footer.

### Fitur baru

- **Pisah Tiap N Halaman.** Pisah PDF menjadi bagian berurutan dengan jumlah halaman tetap (1-500).
- **Pangkas Margin.** Deteksi batas isi non-putih pada tiap halaman pindai dan pangkas margin putih otomatis.

### Peningkatan

- **Katalog alat:** 47 alat; tes katalog dan README EN/ID diperbarui.

### Perbaikan bug

- **Footer saja tidak terlihat (High).** Penempatan footer memakai offset relatif dari matriks teks header yang tidak ada, sehingga berada di luar halaman. Header dan footer kini memakai matriks teks absolut, benar untuk mediaBox dengan titik awal bukan nol.
- **Posisi x footer (Medium).** Variabel yang tertutup membuat footer mewarisi x header; tiap baris kini dipusatkan sendiri, dan teks dipotong menurut lebar terukur.
- **Jebakan token clipboard/bicara (Medium).** Token dikonsumsi meski ekstraksi teks menghasilkan null, sehingga perubahan halaman berikutnya tidak menyalin atau membacakan secara diam-diam.
- **State dokumen lama (Medium).** Hasil pencarian, sorotan, dan teks halaman kini ber-kunci versi payload, jadi PDF yang masuk saat viewer terbuka tidak mewarisi hasil file sebelumnya.
- **Penjaga generasi TTS (Medium).** Error terlambat dari sesi bicara lama tidak menghentikan sesi baru.
- **Crash penyimpanan scanner (Medium).** Penyimpanan gagal kini menampilkan error generik, bukan melempar exception di coroutine tanpa penangan.

## v1.20.0

Cari di PDF dan Header & Footer, plus audit ronde kedua belas yang memperbaiki sisa edge case Bacakan Keras.

### Fitur baru

- **Cari di PDF.** Alat baru: cari frasa dalam satu atau beberapa PDF dan dapatkan laporan teks nomor halaman yang cocok per file.
- **Header & Footer.** Alat baru: cap baris header dan/atau footer pendek di tengah setiap halaman dengan font standar dan normalisasi teks.

### Peningkatan

- **Katalog alat:** 45 alat; tes katalog dan README EN/ID diperbarui.

### Perbaikan bug

- **Bacakan Keras menimpa clipboard (Medium).** Efek salin menyimpan token dan terpicu kembali saat Bacakan Keras mengubah `pageText`, menimpa clipboard. Token kini dikonsumsi setelah satu kali salin.
- **Bacakan Keras membacakan halaman lama (Medium).** `pageText` kini dikosongkan saat halaman yang diminta berubah, sehingga teks lama tidak pernah dibacakan atau disalin.
- **Tombol Simpan hilang untuk PDF masuk (Medium).** `persisted` diremember tanpa kunci versi; PDF masuk yang tidak tersimpan kini menawarkan Simpan ke file meski viewer sudah terbuka.
- **Kegagalan antrean TTS (Medium).** Error pada potongan mana pun kini mengakhiri bacaan, bukan membiarkan UI Macet di "Stop reading"; ID utterance memuat penghitung generasi agar callback terlambat dari sesi lama tidak menghentikan sesi baru.
- **Error rename (Low).** Rename yang gagal kini menampilkan error di dialog, alih-alih diam-diam menutupnya.

## v1.19.0

Audit ronde kesebelas: menstabilkan Bacakan Keras dan perbaikan pengiriman payload masuk.

### Peningkatan

- **Status bar dan aksesibilitas** tidak berubah; 43 alat dan 48 unit test tetap hijau.

### Perbaikan bug

- **Bacakan Keras memotong halaman panjang (High).** Potongan teks TTS diantre dengan `QUEUE_ADD`, lalu potongan terakhir memakai `QUEUE_FLUSH` yang menghapus potongan sebelumnya. Sekarang potongan pertama flush dan sisanya di-ADD; selesai hanya dipicu pada potongan terakhir dan diposting ke thread utama untuk state Compose.
- **Bacakan Keras tidak bisa diulang (High).** Ketuk bicara, berhenti, lalu ketuk lagi di halaman yang sama tidak memicu suara. Penghitung permintaan kini jadi kunci effect.
- **Penemuan mesin TTS.** Query `TTS_SERVICE` ditambahkan ke manifest agar perangkat Android 11+ bisa menemukan mesin bicara.
- **Teks TTS tertunda terpotong.** Saat synthesizer masih init, hanya 3500 karakter pertama yang disimpan; sekarang seluruh halaman diputar ulang lewat helper chunking.
- **Bacakan Keras menimpa clipboard.** Jalur speaker tidak lagi menulis ke clipboard atau menampilkan toast "tersalin"; hanya tombol Salin yang melakukannya.
- **Pengiriman payload masuk (Medium).** `ViewerPayload.version` dan `ShortcutRoute.version` kini state Compose, sehingga PDF/shortcut yang masuk saat di `shell` langsung dinavigasi, dan viewer membaca ulang payload saat dokumen baru tiba saat viewer terbuka.
- **Penutupan TTS** kini mereset state dan menghapus antrean pending.

## v1.18.0

Ratakan Halaman, bacakan keras di viewer, dan audit ronde kesepuluh yang memperbaiki ekstraksi lampiran serta pembersihan privasi.

### Fitur baru

- **Ratakan Halaman.** Alat baru: merender tiap halaman jadi gambar sehingga tampilannya beku. Teks yang bisa diedit, tautan, formulir, dan anotasi tidak lagi ada; teks tidak bisa dipilih. Bisa juga dipakai sebagai langkah workflow.
- **Bacakan keras.** Viewer punya tombol speaker yang membacakan halaman saat ini memakai text-to-speech perangkat; ketuk lagi untuk berhenti. Memakai ekstraksi teks yang sama dengan salin teks halaman.

### Peningkatan

- **Katalog alat:** 43 alat; tes katalog dan README EN/ID diperbarui.

### Perbaikan bug

- **Lampiran bernama Unicode (Medium).** Ekstraksi kini fallback dari `/EF /UF` ke `/EF /F`, dan memakai nama tampilan file lampiran (bukan kunci numerik pohon nama) beserta ekstensinya. Batas total 128 MB mencegah penggunaan memori berlebih pada file jahat.
- **Data tanda tangan tertinggal (Medium).** Hapus Anotasi kini juga menghapus entri `/Perms` katalog, sehingga data tanda tangan sertifikasi tidak bertahan setelah pembersihan privasi.
- **Pembersihan audit.** Penghitung anotasi yang mati dihapus; penanganan error pohon nama rusak tetap ada.

## v1.17.0

Dua alat privasi (Hapus Anotasi, Ekstrak Lampiran) plus audit ronde kesembilan.

### Fitur baru

- **Hapus Anotasi.** Hapus semua komentar, sorotan, tautan, dan kolom formulir dari semua halaman sebelum membagikan. Kamus AcroForm juga dihapus.
- **Ekstrak Lampiran.** Simpan file yang tertanam di pohon nama PDF ke penyimpanan aplikasi dengan nama yang disanitasi.

### Peningkatan

- **Katalog alat:** 42 alat; tes katalog dan README EN/ID diperbarui.
- **FAQ privasi README diperbaiki.** Tidak lagi mengklaim aplikasi tanpa izin jaringan: `INTERNET` hanya dipakai cek pembaruan sesuai permintaan.

### Perbaikan bug

- **Back viewer saat dekripsi (Medium).** `BackHandler` terdaftar setelah early return, sehingga tombol back sistem saat spinner kata sandi kosong melewatinya dan membuka viewer lagi dengan payload lama. Dipindah sebelum early return; dinonaktifkan hanya saat dialog kata sandi tampil.
- **Alpha berlebih di PDF hasil filter.** Halaman abu-abu/balik warna tidak lagi membawa soft mask bening yang tidak perlu.
- **Akurasi changelog.** Deskripsi penanganan teks non-Latin di Teks ke PDF diperbaiki (karakter dihapus, bukan diganti `?`).

## v1.16.0

Filter halaman abu-abu dan balik warna, plus audit ronde kedelapan yang memperbaiki bug halaman kosong kritis pada Teks ke PDF.

### Fitur baru

- **PDF Abu-abu.** Alat baru: merender tiap halaman dalam abu-abu pada resolusi standar/tinggi, untuk pembaca e-ink dan cetak. Teks menjadi bagian dari gambar halaman.
- **Balik Warna Halaman.** Alat baru: membalik warna halaman untuk membaca dengan latar gelap atau mencetak negatif. Teks menjadi bagian dari gambar halaman.

### Peningkatan

- **Katalog alat:** 40 alat; tes katalog dan README EN/ID diperbarui; jumlah unit test diperbarui ke 48.

### Perbaikan bug

- **Teks ke PDF menghasilkan halaman kosong (Critical).** `beginText()` membiarkan matriks teks PDF pada identitas, tetapi kode tidak pernah memposisikan baris pertama, sehingga setiap halaman hasil kosong. Baris pertama kini diposisikan secara absolut di margin kiri/atas; baris berikutnya memakai offset relatif. Ditambah unit test yang mengekstrak teks kembali dari PDF hasil.
- **Jebakan tombol kembali sistem di viewer (High).** Tombol back perangkat melewati `onBack` viewer, menyisakan byte plaintext di `ViewerPayload` dan langsung kembali ke viewer. Ditambahkan `BackHandler` di viewer.
- **Teks non-Latin menggagalkan Teks ke PDF (Medium).** Input kini dinormalisasi dengan `winAnsi()` (seperti watermark/fillSign/OCR), sehingga tanda kutip melengkung dan CJK dihapus alih-alih menggagalkan alat.
- **Kata terlalu panjang terpotong (Medium).** Kata yang lebih lebar dari halaman dipotong menjadi potongan-potongan; input kosong kini gagal dengan pesan "Enter some text." alih-alih menghasilkan PDF kosong.
- **Sapuan file temp saat warm-start.** Prefix `filter-` ditambahkan, sehingga force-stop saat grayscale/invert tidak menyisakan salinan PDF plaintext di cache.
- **Sinkron ID/EN:** jumlah test di README Bahasa Indonesia diperbaiki.

## v1.15.0

Teks ke PDF dan Ukur Ulang Halaman, plus audit ronde ketujuh yang memperbaiki tertukarnya peran kata sandi.

### Fitur baru

- **Teks ke PDF.** Alat baru: menulis teks yang diketik/ditempel ke PDF baru dengan pembungkusan baris dan halaman otomatis; pilih A4/A5/Letter/Legal dan ukuran huruf kecil/normal/besar.
- **Ukur Ulang Halaman.** Alat baru: menyesuaikan tiap halaman ke ukuran kertas standar dengan menjaga rasio dan orientasi, memakai form XObject PDF asli (tanpa rasterisasi).

### Peningkatan

- **Katalog alat:** 38 alat; tes katalog dan README EN/ID diperbarui.
- **Petunjuk opsi Bahasa Indonesia:** placeholder opsi teks kini punya versi lokal; label aksesibilitas tombol sematan dilokalisir.

### Perbaikan bug

- **Peran kata sandi tertukar (Medium).** `StandardProtectionPolicy` PDFBox menerima (owner, user) dalam urutan itu; kode sebelumnya memasukkan kata sandi buka sebagai owner. Diperbaiki, dan kata sandi pemilik yang berbeda kini menerapkan batasan yang masuk akal (tanpa ubah, anotasi, isi formulir, susun, atau ekstrak untuk kata sandi buka).
- **Sapuan cache saat mulai.** Prefix `blank-`, `dup-`, `lint-`, `report-`, `compare-` ditambahkan, sehingga force-stop saat alat tersebut tidak menyisakan salinan teks biasa di cache.
- **Placeholder alat teks** dilokalisir untuk Bahasa Indonesia.

## v1.14.0

Buat Bookmark, kata sandi pemilik terpisah, dan audit ronde keenam yang memperbaiki bug kehilangan data pada deteksi halaman kosong.

### Fitur baru

- **Buat Bookmark.** Alat baru: menambahkan outline/bookmark ke PDF dari baris seperti `1=Pendahuluan, 5=Bab Satu`; nomor halaman mulai dari 1 dan baris invalid dilewati. Navigasi bookmark di viewer akan menampilkannya.
- **Kata sandi pemilik.** Alat Set Password kini punya opsi kata sandi pemilik terpisah dari kata sandi buka; default mengikuti kata sandi buka bila kosong.

### Peningkatan

- **Sisa UI lokal.** "Reset", "Warna" dan label aksesibilitas halaman kini lokal di Atur Halaman, Isi & Tanda, Crop, Redaksi, dan viewer.
- **Jumlah unit test** di README diperbarui sesuai suite (46 tes).

### Perbaikan bug

- **Deteksi halaman kosong (High).** Pemeriksaan putih membandingkan seluruh integer RGB 24-bit, sehingga tanda merah/kuning bisa dihitung putih dan halaman dihapus. Sekarang tiap kanal diperiksa terpisah.
- **Render halaman kosong.** Halaman dirender tanpa mengosongkan bitmap ke putih lebih dulu, sehingga halaman kosong vektor tidak terdeteksi dan duplikat putih-vs-transparen terlewat. Ditambahkan `eraseColor(WHITE)` di kedua deteksi.
- **Ukuran bitmap dibatasi.** Tinggi halaman hasil render dibatasi 3200 px di kedua alat, mencegah alokasi besar pada halaman sempit-tinggi.
- **PDF masuk dari aplikasi lain.** Berbagi PDF saat layar alat/viewer terbuka kini langsung dibuka saat kembali ke shell.
- **Page adoption cetak.** PrintOps kini memakai helper `adopt` bersama, bukan salinan lokal.

## v1.13.0

Tiga alat perapian halaman untuk dokumen hasil pindai dan gabung, plus hardening audit.

### Fitur baru

- **Hapus Halaman Kosong.** Mendeteksi halaman benar-benar kosong lewat render resolusi rendah lalu menghapusnya; menulis PDF bersih plus laporan teks nomor halaman yang dihapus. Dibatasi 400 halaman yang dianalisis.
- **Balik Halaman.** Membalik urutan halaman PDF (misal dokumen yang dipindai dari belakang ke depan), dengan unit test yang memeriksa urutan baru dan ukuran halaman.
- **Cari Duplikat** (v1.12.0) dan **Hapus Halaman Kosong** saling melengkapi setelah alur scan + gabung.

### Peningkatan

- **Katalog alat:** 35 alat; tes katalog dan README EN/ID diperbarui. Hapus halaman kosong tidak termasuk langkah workflow (menghasilkan dua keluaran).

### Perbaikan bug

- **Page adoption dibagikan.** Helper internal `adopt` kini dipakai bersama antar operasi engine halaman, sehingga dokumen yang dibalik/dibangun ulang tetap menyimpan resource halaman.

## v1.12.0

Cari Duplikat: alat PDF yang melaporkan halaman identik secara visual (misal halaman pindai yang terulang), plus hardening audit.

### Fitur baru

- **Cari Duplikat.** Alat baru: merender tiap halaman dengan resolusi rendah lalu melakukan hash piksel; mengelompokkan halaman yang secara visual identik dan menulis laporan teks bernomor halaman. Dibatasi 400 halaman dengan ukuran bitmap yang dibatasi, sehingga pekerjaan pindai besar tidak meledakkan memori atau waktu.

### Peningkatan

- **Katalog alat:** 33 alat, tes katalog diperbarui; `cariDuplikat` tidak termasuk langkah workflow (menghasilkan laporan, bukan PDF).

### Perbaikan bug

- **Hardening audit:** tidak ada cacat baru; keselarasan Strings dijamin oleh tes regresi.

## v1.11.0

Alat baru: PDF Lint dan sort berdasarkan ekstensi. Audit lima ronde di seluruh aplikasi.

### Fitur baru

- **PDF Lint.** Alat baru: laporan teks tentang isi PDF - versi, font, enkripsi, producer, dan daftar masalah yang ditemukan (font tidak ter-embed, keanehan yang tersebut). Satu ketukan menghasilkan `.txt`.
- **Urutkan berdasarkan ekstensi.** Opsi baru di menu sort tab Files: sort berdasarkan ekstensi file lalu nama; folder tetap berada di atas. File tanpa ekstensi diurutkan menggunakan nama.
- **Tata letak tab Files diperhalus.** Placeholder sort dan string plural dilokalisikan; memindahkan file ke folder yang sudah punya nama sama muncul error di `moveError`, alih-alih diam-diam membiarkan pemindahannya berhasil. Komentar menjadi mutakhir.

### Peningkatan

- **Tab Files**: opsi sort berdasarkan ekstensi ditambahkan ke menu sort.
- **PDF Lint** dipecahkan `ToolCatalogTest` (32 alat) dan mendapat smoke test JVM khusus pakai font Poppins ter-embed, sehingga font benar-benar dieksekusi pada run unit test JVM.

### Perbaikan bug

- **Tab Files (audit lima)**: komentar sort diperbarui; error baru `moveError` menampilkan `genericError` kalau folder target sudah punya nama sama.
- **ComparePdf load ordering**: pada PDF kedua yang tidak terbaca, dokumen pertama sekarang ditutup dengan benar.
- **Compare excerpt marking**: potongan yang terpotong kini diakhiri dengan penanda `(excerpt truncated)` alih-alih terlihat seperti diff lengkap; pemotongan aman di batas code point.
- **Compare unreadable pages**: halaman yang teksnya tidak bisa dibaca kini dilaporkan pada ringkasan alih-alih dihitung identik.
- **extractImages + temporary file naming**: nama file temp proses-wide unik menghindari tabrakan antar render paralel.
- **String mati dihapus**: `nothingYet`, `batchHint`, `imagesToPdfNote`, `jumpToPage`, `noTextOnPage`, `saveAs`, `savedCopies` dihapus (tak dipakai); tes lokalasi diperkuat dengan asersi bebas variabel di posisi dan nilai kedua bahasa.

## v1.10.0

Dua alat baru, perbaikan lokal teks Bahasa Indonesia, dan audit mendalam kelima di seluruh aplikasi.

### Fitur baru

- **Laporan Dokumen.** Alat baru: laporan teks tentang halaman, ukuran halaman pertama, metadata (judul, penulis, subjek, kata kunci, creator, producer, tanggal), jumlah gambar tertanam dan nilai kolom formulir, dibatasi untuk formulir besar.
- **Hapus Metadata.** Alat baru: menghapus dictionary informasi dokumen dan metadata XMP dalam satu ketukan, untuk berbagi file tanpa detail pribadi. Bisa juga dipakai sebagai langkah dalam Workflow Builder.

### Peningkatan

- **Strings Indonesia diperbaiki.** Pergeseran posisi pos memindahkan label sort, tombol putar semua, notice dekripsi dan seluruh bagian update/sponsor ke field yang salah dalam Bahasa Indonesia. Diperbaiki, dengan tes regresi.
- **Tab Files work di luar thread utama.** Daftar folder Move-to kini dihitung di dispatcher latar dan dibatasi 500 folder; konfirmasi delete dan migrasi penyimpanan sekali tidak memblokir UI.
- **Home recent files di luar thread utama.** Query file terbaru di Home kini berjalan di Dispatchers.IO.
- **Startup dan cache clearing di luar thread utama.** Pembersihan file sementara saat peluncuran dan baris Hapus cache tidak lagi di thread utama.
- **Label thumbnail lokal.** Button thumbnail di viewer kini punya content description Bahasa Indonesia.
- **Compare melaporkan halaman tak terbaca.** Halaman yang teksnya tidak bisa diekstrak dilaporkan, bukan diam-diam dihitung identik. Potongan yang terpotong ditandai eksplisit.

### Perbaikan bug

- **Compare resource leak.** Kegagalan saat membuka PDF kedua tidak lagi membiarkan dokumen pertama terbuka.
- **Parsing rentang tolak teks.** Rentang salah seperti `abc` atau `x-3` kini memunculkan error alih-alih diam-diam memilih halaman 1.
- **Nama file sementara unik.** Counter proses-wide mencegah dua render paralel berkolisi pada path berbasis timestamp.
- **Gambar besar dilaporkan.** Saat Ekstrak Gambar melewati gambar di atas 60 megapiksel, file `skipped-images.txt` disertakan dalam hasil.
- **CTA Bandingkan butuh dua file.** CTA tetap nonaktif sampai dua PDF dipilih, alih-alih gagal setelah diklik.

## v1.9.0

Bandingkan PDF, dan audit ronde ketiga di seluruh aplikasi.

### Fitur baru

- **Bandingkan PDF.** Alat baru: bandingkan dua PDF halaman demi halaman dan dapatkan laporan teks: halaman identik, halaman berubah, dan baris yang tepat berbeda (dibatasi per halaman). Berguna untuk meninjau revisi kontrak dan spesifikasi.

### Peningkatan

- **Walk file terbaru dibatasi.** Membuka kembali PDF terakhir tidak lagi memindai seluruh pohon Download/Pedeef di thread utama; walk dibatasi dan berjalan di background.
- **Penjaga memori Ekstrak Gambar.** Gambar di atas 60 megapiksel dilewati alih-alih berisiko mematikan aplikasi karena out-of-memory pada PDF hasil pindai, dan gambar yang diekstrak ditulis ke ZIP satu per satu alih-alih semuanya ditahan sebelum di-zip.
- **Posisi baca di luar thread utama.** Penyimpanan posisi halaman kini berjalan di coroutine latar.
- **Pintasan diantar saat onboarding.** PDF yang dibagikan saat onboarding belum selesai dibuka begitu aplikasi sampai ke layar utama, alih-alih diabaikan.

### Perbaikan bug

- **Rentang halaman cetak di-clamp.** Akhir rentang yang tidak dibatasi tidak lagi meledakkan daftar indeks; pemilihan kosong melaporkan error alih-alih menghasilkan job kosong.
- **ID pintasan bebas tabrakan.** Pintasan file terbaru memakai path penuh sebagai id, bukan hash 32-bit, jadi dua path tidak pernah jatuh ke pintasan yang sama.
- **Pesan error PDF kosong.** Jumlah halaman tidak valid menampilkan pesan yang dimaksud alih-alih error generik.

## v1.8.0

Ikon aplikasi baru, buka kembali PDF terakhir dari launcher, PDF kosong, dan audit ronde kedua untuk v1.7.0 beserta perbaikannya.

### Fitur baru

- **Ikon aplikasi baru.** Lambang resmi Pedeef (dokumen krem di atas halaman peach di atas ubin gradien terakota), dikemas sebagai ikon adaptif dengan lapisan bertema monokrom untuk Android 13+, plus ikon bulat dan ikon lawas. Lambang yang sama menggantikan karya seni di repo.
- **Buka kembali PDF terakhir.** Tahan ikon aplikasi dan PDF yang terakhir diubah muncul sebagai pintasan (sampai 4); ketuk sekali untuk membukanya langsung ke viewer.
- **PDF kosong.** Alat baru: buat PDF kosong di A4, A5, Letter atau Legal, sampai 200 halaman, tanpa file sumber.

### Peningkatan

- **Race lanjut baca diperbaiki.** Penyimpanan posisi halaman 600ms tidak lagi menimpa posisi hasil pemulihan sebelum gulir lanjut baca selesai, jadi PDF besar atau terenkripsi bisa lanjut baca dengan andal.
- **Pengantaran pintasan di luar shell diperbaiki.** Pintasan yang diklik saat layar lain terbuka (atau onboarding masih tampil) kini langsung membuka layar tujuannya saat aplikasi kembali ke shell, alih-alih diam diabaikan.
- **Ekstrak Gambar lebih ringan dan dibatasi.** Gambar JPEG di-encode ulang sebagai JPEG, bukan membengkak menjadi PNG, dan total data yang diekstrak dibatasi dengan pesan error yang jelas alih-alih tekanan memori.
- **Posisi baca ditulis atomik.** Crash di tengah penyimpanan tidak lagi bisa menghapus posisi baca yang tersimpan.
- **Status loading tab Files.** Tab Files menampilkan spinner pada render pertama alih-alih sekilas menyatakan "tidak ada file".
- **Catatan dekripsi.** Saat PDF terproteksi dibuka tanpa sandi, viewer menampilkan catatan satu baris bahwa salinan yang disimpan dan dibagikan tidak dilindungi sandi.

### Perbaikan bug

- **Jalur cetak dirapikan.** Job cetak dokumen penuh tidak lagi mem-parse PDF sama sekali; rentang halaman parsial divalidasi terhadap jumlah halaman sebelum ditulis ulang.
- **Tampilan jumlah folder.** Folder dengan lebih dari 10.000 file kini menampilkan "10,000+", bukan angka polos.
- **Warna badge versi.** Badge README memakai terakota brand baru.

## v1.7.0

Mode malam, lanjut baca, pintasan launcher, ekstraksi gambar, dan audit mendalam kedua dengan perbaikannya.

### Fitur baru

- **Mode malam.** Penampil dapat membalik warna halaman untuk nyaman membaca di tempat gelap. Aktifkan dari baris atas penampil atau di Pengaturan pada Tampilan.
- **Lanjut baca.** Pedeef mengingat halaman terakhir yang kamu lihat per dokumen dan membukanya di sana.
- **Pintasan launcher.** Tahan ikon aplikasi untuk Pindai, Gabung PDF, Isi & tanda, dan Atur halaman.
- **Ekstrak Gambar.** Alat baru: menyimpan setiap gambar yang tertanam di PDF sebagai PNG, digabung ZIP jika banyak. Ikon lebih kecil dari 48px dilewati.

### Peningkatan

- **Dekripsi di luar thread utama.** Membuka PDF terenkripsi tidak lagi membekukan UI; spinner muncul saat percobaan kata sandi kosong berjalan di background.
- **Perbaikan cetak.** Menyiapkan pekerjaan cetak sekarang berjalan di thread latar, dan salinan sementara berupa teks biasa dihapus saat layanan cetak selesai tanpa menulis (misalnya saat dialog ditutup).
- **Hapus cache benar-benar menghapus.** Baris Hapus cache kini menghapus seluruh direktori cache, termasuk salinan berbagi dan sisa cetak, bukan daftar prefix sebagian.
- **Pemindaian file di luar thread utama.** Tab Files menampilkan dan menghitung dari thread latar; pengurutan berdasarkan tanggal atau ukuran tetap menampilkan folder lebih dulu.
- **Pencarian dan salin anti-crash.** PDF yang rusak tidak lagi membuat crash penampil saat mencari atau menyalin teks halaman; pesan error umum ditampilkan.
- **Penyimpanan di background.** Menyimpan hasil alat dan menyimpan dari penampil kini berjalan di thread latar dengan penanganan error, bukan di thread utama.
- **Sumber versi tunggal.** Versi yang ditampilkan di Pengaturan dan dipakai untuk cek pembaruan kini berasal dari konfigurasi build.
- **Perbandingan tag diperkuat.** Akhiran prarilis seperti `-beta` tidak lagi dianggap sama dengan rilis dasarnya.

### Perbaikan bug

- **String penghitung rusak.** Pesan "file tersimpan" setelah menyimpan hasil alat tidak lagi berisi karakter rusak.
- **Tombol salin dinonaktifkan pada PDF kosong.** Salin teks halaman dinonaktifkan saat dokumen tidak punya halaman, alih-alih meneruskan indeks halaman negatif ke text stripper.
- **Jumlah item folder di thread latar.** Jumlah file folder tidak lagi dihitung sinkron saat render daftar.
- **PDF masuk tidak lagi membekukan peluncuran.** PDF yang dibuka dari aplikasi lain dibaca di thread latar.

## v1.6.0

Cetak dari viewer, simpan ke file langsung dari viewer, dan penguatan audit secara menyeluruh.

### Fitur baru

- **Cetak.** Ikon cetak di viewer mengirim PDF yang sedang dibuka ke dialog cetak sistem; halaman dapat dicetak apa adanya.
- **Simpan ke file dari viewer.** Buka PDF dari aplikasi lain atau Files, periksa dulu, lalu ketuk Simpan untuk menuliskannya ke Download/Pedeef (atau penyimpanan aplikasi) hanya saat kamu ingin menyimpannya.
- **Urutkan file.** Urutkan filemu berdasarkan nama, tanggal atau ukuran di tab Files dengan menu baru.
- **Putar semua.** Di Atur Halaman, satu ketukan memutar setiap halaman 90 derajat; setiap halaman masih bisa diputar terpisah.

### Peningkatan

- **Keamanan rename.** Ganti nama kini menolak menimpa file yang sudah ada, alih-alih diam-diam menghapusnya.
- **Muatan kadaluwarsa dibersihkan.** State viewer (kata sandi, status tersimpan, zoom) tidak lagi bocor antar-PDF; kembali menutup dokumen dengan bersih.
- **Sorotan halaman terputar diperbaiki.** Kotak sorotan hasil pencarian pada halaman dengan /Rotate diputar pada sumbu vertikal sebelum dipetakan, sehingga kotak ada di tempat yang benar.
- **Tata letak viewer tetap.** Bookmark dan indikator halaman tidak lagi ikut bergeser ke samping saat PDF diperbesar.
- **Perhitungan Settings dipindah ke background.** Jumlah file dan ukuran penyimpanan dihitung di thread latar dengan batas 10k entri; input tetap responsif.
- **Bocoran koneksi diperbaiki.** Cek pembaruan memutus koneksi HTTP di setiap jalur.
- **Buka kunci batch.** Beberapa PDF dengan kata sandi yang dipilih berurutan semuanya dibuka sebelum dialog ditutup.
- **File .part dimigrasi dengan bersih.** File .part yang tersisa dari salinan gagal dihapus di awal migrasi.

### Perbaikan bug

- **Sapu cache.** File sementara share dan export dihapus saat aplikasi mulai dan melalui tombol Hapus cache.

## v1.5.0

Membuka PDF dari aplikasi lain, navigasi bookmark di viewer, perbaikan keamanan mendalam, dan audit lengkap.

### Fitur baru

- **PDF handler.** Buka PDF dari aplikasi lain atau Files; Pedeef langsung menampilkannya di viewer dan hanya disimpan ke Download/Pedeef saat kamu menekan tombol Simpan di bilah atas viewer.
- **Navigasi bookmark.** Viewer menampilkan outline bookmark dengan ketukan sekali ke setiap bagian.
- **Preset zoom.** Berganti 100%, 125% dan 150% langsung dari bilah atas viewer.

### Peningkatan

- **Redaksi lebih aman.** Daftar pembersihan tingkat dokumen kini meliputi PageLabels, Dests, file terkait, collection, open action dan piece info; entri AF tingkat halaman juga dihapus.
- **Migrasi lebih aman.** Migrasi ke penyimpanan publik memverifikasi setiap file yang disalin berdasarkan ukuran, rename secara atomik, tidak pernah menimpa file publikmu yang sudah ada, menulis marker .migrated hanya jika semuanya berhasil, dan mengulangi salinan parsial pada peluncuran berikutnya.
- **Berbagi lebih aman.** Share dan ekspor dinonaktifkan untuk folder; pemberian izin folder tidak pernah membocorkan seluruh direktori ke aplikasi lain.
- **Kompresi akurat.** JPEG CMYK, YCCK dan ICCBased tidak lagi di-encode ulang; perubahan warna pada PDF terkompresi dihindari.
- **Penanganan file terkunci.** PDF dengan owner-password saja kini terbuka tanpa prompt kata sandi (kata sandi kosong dicoba lebih dulu).
- **Highlight halaman terputar.** Sorotan pencarian berfungsi pada halaman terputar, tidak lagi dilewatkan.
- **Perbaikan tanda tangan berulang.** Tombol "TTD tersimpan" berfungsi kembali: tanda tangan tersimpan diterima sebagai gambar buatan aplikasi.

### Perbaikan bug

- **Bocor cache polos.** Gambar dan PDF share/eksport sementara dibersihkan saat startup dan baris Hapus cache mencakupnya.
- **Kebersihan manifest.** Flag legacy external storage dihapus; izin baca dibatasi sampai API 29.

## v1.4.0

Sorotan pencarian, file terbaru dan kontrol cache: audit makin dalam.

### Fitur baru

- **Sorotan pencarian di viewer.** Temukan frasa dan setiap halaman berisi frasa itu disorot langsung di halamannya; ketuk ikon kisi untuk thumbnail halaman.
- **File terbaru di Home.** Lima PDF yang paling baru diubah, satu ketukan untuk membuka.

### Peningkatan

- **Scan terbatas.** Penghitungan ukuran dan jumlah file dibatasi 10.000 entri, jadi folder besar tetap ringan.
- **Baris Hapus cache.** Satu ketukan membersihkan file sementara, dengan ukurannya ditampilkan di Pengaturan.

### Perbaikan bug

- **Stabilitas daftar.** Baris Files kini di-key berdasarkan path absolut, jadi berpindah folder tidak membocorkan state ke baris lain.

## v1.3.0

Update besar: perbaikan audit, strip thumbnail di viewer, tanda tangan yang bisa dipakai berulang, dan pengatur ekspor yang lebih baik.

### Fitur baru

- **Thumbnail halaman di viewer.** Ketuk ikon kisi di viewer untuk lompat ke halaman mana pun sekilas; halaman aktif diberi sorotan.

- **Tanda tangan dapat dipakai ulang.** Gambar sekali di Isi & Tanda Tangan; tanda tanganmu tersimpan dan ditawarkan di setiap halaman yang kamu tandatangani.

- **Kualitas ekspor.** PDF ke Gambar kini membiarkanmu memilih ukuran gambar (1600 atau 2400 px) dan kualitas JPEG (75, 90 atau 100).

### Peningkatan

- **Perbaikan audit.** Daftar file membatasi hasil pencarian; penyimpanan gambar tanda tangan dipindah dari cache ke internal files supaya proyek Isi & Tanda Tangan bertahan; pelacakan halaman di viewer memakai snapshot-derived state sehingga tidak recompose pada setiap frame gulir.
- **Lebih banyak tes.** Unit test kini mencakup pemeriksa pembaruan dan penyanitasi nama file.

### Perbaikan bug

- **Gambar Isi & Tanda Tangan bisa rusak.** Referensi tanda tangan dan gambar berada di cache yang mudah hilang; sekarang di internal files aplikasi.

## v1.2.0

Segarkan tampilan menyeluruh: token desain konsisten, tata letak lebih rapi dan animasi halus yang ringan.

### Peningkatan

- **Segarkan visual.** Satu skala jarak, radius dan elevasi seragam di semua layar: Home, Files, Pengaturan, layar alat, dialog dan onboarding.
- **Home lebih rapi.** Ikon aplikasi di samping tulisan Pedeef, kartu pahlawan pemindai dengan gradien halus dan ikon panah, alat terpin animasi, kartu alat konsisten.
- **Pengaturan lebih rapi.** Kartu kepala dengan ikon aplikasi, bagian terkelompok, segmented control selebar penuh. Memperbaiki bug label "Mode" dan "Bahasa" yang terpotong berdiri.
- **Files lebih rapi.** Baris daftar konsisten dengan ikon melingkar berlatar warna, empty state di tengah, gerbang izin penyimpanan lebih baik.
- **Transisi tab halus.** Crossfade antara Home, Files dan Pengaturan; cepat dan ringan.

### Perbaikan bug

- **Label Pengaturan terpotong.** Label Mode dan Bahasa tidak lagi terpotong vertikal pada baris yang sempit.
- **Kontras kartu pemindai.** Teks di kartu pemindai tetap kontras di semua tema.

## v1.1.0

File sekarang tersimpan di folder publik Download/Pedeef, ditambah cek pembaruan dan desain ulang pengaturan.

### Fitur baru

- **Penyimpanan publik.** Semua yang disimpan Pedeef (pindaian, hasil alat) masuk ke `Download/Pedeef`, terlihat di file manager mana pun. Izinkan "All files access" atau tetap pakai penyimpanan dalam aplikasi.
- **Periksa pembaruan.** Ketuk baris versi di Pengaturan untuk memeriksa rilis terbaru di GitHub.
- **Sponsor.** Dukung pengembang lewat GitHub Sponsors, Saweria, SociaBuzz atau BagiBagi.
- **Profil GitHub.** Bagian Tentang di Pengaturan menampilkan tautan ke GitHub pengembang.

### Peningkatan

- **File di Download/Pedeef.** Gerbang izin baru pada tab Files dan migrasi sekali jalan untuk file yang tersimpan dari versi sebelumnya.
- **Pengaturan lebih rapi.** Tata letak didesain ulang: kartu kepala, bagian terkelompok, jarak konsisten.
- **Tab bar disembunyikan di layar detail.** Bar bawah tidak lagi menutupi layar viewer, alat atau pemindai.
- **PDF to Images tersimpan ke ponsel.** Halaman JPEG hasil ekspor mendarat di Download/Pedeef seperti lainnya.
- **Ikon adaptif.** Foreground transparan di dalam zona aman; varian monochrome tetap ada untuk launcher bertema.
- **Gerbang lint.** Build sekarang menjalankan Android lint dengan nol error, lokal dan di CI.

### Perbaikan bug

- **Paksaan potret dihapus.** Pedeef tidak lagi memaksa orientasi potret.

## v1.0.0

Rilis publik pertama. Pedeef membangun ulang gagasan peralatan PDF menjadi aplikasi Android yang sepenuhnya offline dan dwibahasa.

### Fitur baru

- **Peralatan PDF offline.** 26 alat: gabung, selang-seling dan campur, pisah (setiap halaman, rentang, teks, bookmark, dua, ukuran), atur halaman, putar, balik, beberapa halaman per lembar, pangkas dan ubah ukuran, watermark, nomor halaman, redaksi, isi dan tanda tangan, editor detail, kompres, OCR, setel dan hapus kata sandi, PDF ke ZIP, pembuat workflow, dan mode batch.
- **Alat baru.** Gambar ke PDF, PDF ke Gambar, Ekstrak Teks.
- **Pemindai kamera.** Deteksi tepi dan deskew di perangkat (ML Kit Document Scanner), impor galeri sebagai cadangan, lapisan teks OCR opsional.
- **Penampil bawaan.** Gulir, zoom, cari dengan navigasi hasil, salin teks halaman. Berfungsi mulai Android 7.0 ke atas.
- **Pohon file.** Simpan, folder, pencarian, ganti nama, pindah, bagikan, hapus, ekspor salinan. Semuanya tersimpan di penyimpanan privat aplikasi.
- **UI dwibahasa.** English dan Bahasa Indonesia, bisa diganti di Pengaturan.
- **Tiga tema.** Ember, Black Cherry, Arctic Blue, plus warna dinamis di Android 12+.
- **Privasi sejak desain.** Tanpa izin jaringan, tanpa izin penyimpanan luas, allowBackup nonaktif.

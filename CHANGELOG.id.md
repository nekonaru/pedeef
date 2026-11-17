# Changelog

Semua perubahan penting pada Pedeef dicatat di sini. Formatnya mengikuti [Keep a Changelog](https://keepachangelog.com).

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

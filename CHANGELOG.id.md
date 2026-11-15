# Changelog

Semua perubahan penting pada Pedeef dicatat di sini. Formatnya mengikuti [Keep a Changelog](https://keepachangelog.com).

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

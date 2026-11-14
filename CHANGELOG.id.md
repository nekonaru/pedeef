# Changelog

Semua perubahan penting pada Pedeef dicatat di sini. Formatnya mengikuti [Keep a Changelog](https://keepachangelog.com).

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

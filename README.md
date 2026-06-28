# Arsipku

Arsipku adalah aplikasi manajemen arsip surat (Surat Masuk dan Surat Keluar) sederhana yang dikembangkan dengan arsitektur terpisah antara *Backend* (Java murni) dan *Frontend* (Svelte).

🚀 Fitur Utama
- **Manajemen Surat Masuk**: Tambah, Lihat, Update, dan Hapus data surat masuk.
- **Manajemen Surat Keluar**: Tambah, Lihat, Update, dan Hapus data surat keluar.
- **Upload Arsip**: Mendukung unggah dan pratinjau file/dokumen surat.
- **Seed Data**: Tersedia endpoint khusus untuk mengisi data *dummy* secara otomatis untuk keperluan testing.

🛠️ Teknologi yang Digunakan

*Backend*
- **Bahasa**: Java
- **Server**: Bawaan Java (`com.sun.net.httpserver.HttpServer`)
- **Database**: MySQL (JDBC)
- **Port**: 8000

*Frontend*
- **Framework**: Svelte 5
- **Build Tool**: Vite
- **Styling**: Tailwind CSS v4

---

⚙️ Persyaratan Sistem
Pastikan perangkat Anda sudah terinstal:
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) (Minimal versi 11)
- [Node.js & npm](https://nodejs.org/) (Untuk menjalankan frontend Svelte)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/) (XAMPP/MAMP/Native)

---

📖 Panduan Instalasi & Menjalankan Aplikasi

**1. Konfigurasi Database**

1. Buka MySQL / phpMyAdmin.
2. Buat database baru dengan nama `db_arsipku`.
3. Tidak perlu membuat tabel secara manual, backend akan menangani pembuatan kolom jika diperlukan (termasuk fitur upload).
4. Pastikan username MySQL Anda adalah `root` dengan password kosong (atau sesuaikan di file `Backend/src/main/java/com/arsipku/config/Database.java`).

**2. Menjalankan Backend (Java)**

1. Buka folder proyek di IDE favorit Anda (misalnya VS Code atau IntelliJ).
2. Pastikan MySQL sudah berjalan (contoh: nyalakan MySQL di XAMPP).
3. Jalankan file `Main.java` yang berada di `Backend/src/main/java/com/arsipku/Main.java`.
4. Jika berhasil, akan muncul pesan di konsol:
   ```text
   Mencoba menyalakan mesin...
   koneksi Berhasil.
   Sip! Database aman.
   Server API sudah menyala!
   ```
5. Backend berjalan di `http://localhost:8000`.
6. *(Opsional)* Kunjungi `http://localhost:8000/api/seed` melalui browser atau REST Client untuk mengisi data awal (dummy).

**3. Menjalankan Frontend (Svelte)**

1. Buka terminal baru.
2. Masuk ke direktori frontend:
   ```bash
   cd Frotend
   ```
3. Instal semua dependensi:
   ```bash
   npm install
   ```
4. Jalankan *development server*:
   ```bash
   npm run dev
   ```
5. Buka tautan yang muncul di terminal (biasanya `http://localhost:5173`) di browser Anda.

---

📡 Dokumentasi API Singkat
Backend menyediakan beberapa endpoint REST API berikut:

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| `GET` | `/api/test` | Cek status API |
| `GET` | `/api/seed` | Generate *dummy data* ke database |
| `GET` | `/api/surat-masuk?sort=desc` | Mengambil data surat masuk |
| `POST` | `/api/surat-masuk/tambah` | Menambah surat masuk baru |
| `POST` | `/api/surat-masuk/update` | Mengubah data surat masuk |
| `POST` | `/api/surat-masuk/hapus` | Menghapus surat masuk |
| `GET` | `/api/surat-keluar?sort=desc` | Mengambil data surat keluar |
| `POST` | `/api/surat-keluar/tambah` | Menambah surat keluar baru |
| `POST` | `/api/surat-keluar/update` | Mengubah data surat keluar |
| `POST` | `/api/surat-keluar/hapus` | Menghapus surat keluar |
| `GET` | `/api/files/{filename}` | Pratinjau file yang diunggah |

📝 Catatan Tambahan
- File arsip / dokumen yang diunggah akan otomatis tersimpan di folder `uploads/` yang ter-generate di dalam folder utama proyek (root).

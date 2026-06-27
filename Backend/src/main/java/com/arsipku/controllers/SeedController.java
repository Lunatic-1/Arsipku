package com.arsipku.controllers;

import com.arsipku.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

public class SeedController {

    public static String seedData() {
        try {
            Connection conn = Database.getConnection();

            // Cek apakah data sudah ada
            ResultSet rsCheck = conn.createStatement().executeQuery("SELECT COUNT(*) FROM surat_masuk");
            rsCheck.next();
            int count = rsCheck.getInt(1);
            if (count > 0) {
                return "{\"status\":\"info\", \"pesan\":\"Data sudah ada (" + count + " surat masuk). Hapus data lebih dulu jika ingin seed ulang.\"}";
            }

            // ===== INSERT SURAT MASUK =====
            String sqlMasuk = "INSERT INTO surat_masuk (nomor_surat, tanggal_surat, tanggal_diterima, pengirim, perihal, keterangan) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement psMasuk = conn.prepareStatement(sqlMasuk);

            Object[][] dataMasuk = {
                {"001/SM/I/2025",   "2025-01-05", "2025-01-06", "Kementerian Pendidikan RI",      "Pengumuman Seleksi Beasiswa 2025",            "Seleksi dibuka hingga akhir Februari 2025"},
                {"002/SM/I/2025",   "2025-01-12", "2025-01-13", "PT. Maju Jaya Abadi",            "Penawaran Kerjasama Pengadaan Barang",        "Dilampirkan proposal penawaran lengkap"},
                {"003/SM/II/2025",  "2025-02-03", "2025-02-04", "Dinas Sosial Kota",              "Undangan Rapat Koordinasi Bansos",            "Rapat pada 10 Feb 2025 pukul 09.00 WIB"},
                {"004/SM/II/2025",  "2025-02-18", "2025-02-19", "BPJS Ketenagakerjaan",           "Pemberitahuan Klaim Jaminan Karyawan",        "Klaim atas nama karyawan periode Jan 2025"},
                {"005/SM/III/2025", "2025-03-07", "2025-03-08", "Universitas Negeri Jakarta",     "Permohonan Izin Magang Mahasiswa",            "10 mahasiswa prodi Manajemen Bisnis"},
                {"006/SM/III/2025", "2025-03-20", "2025-03-21", "Bank BNI Cabang Utama",          "Notifikasi Jatuh Tempo Pinjaman",             "Jatuh tempo pada 31 Maret 2025"},
                {"007/SM/IV/2025",  "2025-04-02", "2025-04-03", "Kementerian Keuangan RI",        "Surat Edaran Pengelolaan APBN Triwulan II",   "SE No. 12/MK.01/2025 perihal efisiensi"},
                {"008/SM/IV/2025",  "2025-04-15", "2025-04-16", "CV. Sumber Berkah Lestari",      "Faktur Pembelian Alat Tulis Kantor",          "Faktur No. FKT-2025-0412, total Rp 4.750.000"},
                {"009/SM/V/2025",   "2025-05-08", "2025-05-09", "Pemerintah Daerah Kab. Bogor",   "Permohonan Data Statistik Wilayah",           "Data diperlukan untuk penyusunan Renstra 2026"},
                {"010/SM/VI/2025",  "2025-06-10", "2025-06-11", "PT. Telkom Indonesia Tbk",       "Konfirmasi Pembayaran Tagihan Internet",       "Invoice INV-2025-0610, tagihan bulan Mei 2025"},
            };

            for (Object[] row : dataMasuk) {
                psMasuk.setString(1, (String) row[0]);
                psMasuk.setDate(2,   Date.valueOf((String) row[1]));
                psMasuk.setDate(3,   Date.valueOf((String) row[2]));
                psMasuk.setString(4, (String) row[3]);
                psMasuk.setString(5, (String) row[4]);
                psMasuk.setString(6, (String) row[5]);
                psMasuk.executeUpdate();
            }

            // ===== INSERT SURAT KELUAR =====
            String sqlKeluar = "INSERT INTO surat_keluar (nomor_surat, tanggal_surat, tujuan, perihal, keterangan) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psKeluar = conn.prepareStatement(sqlKeluar);

            Object[][] dataKeluar = {
                {"001/SK/I/2025",   "2025-01-08", "Kementerian Pendidikan RI",      "Balasan Seleksi Beasiswa 2025",               "Menyertakan daftar peserta yang diusulkan"},
                {"002/SK/I/2025",   "2025-01-20", "PT. Maju Jaya Abadi",            "Konfirmasi Kerjasama Pengadaan Barang",       "Terlampir MoU yang telah ditandatangani"},
                {"003/SK/II/2025",  "2025-02-10", "Dinas Sosial Kota",              "Konfirmasi Kehadiran Rapat Koordinasi",       "Kami mengutus 2 orang perwakilan"},
                {"004/SK/II/2025",  "2025-02-25", "BPJS Ketenagakerjaan",           "Pengiriman Dokumen Klaim Jaminan",            "Dokumen lengkap terlampir sesuai persyaratan"},
                {"005/SK/III/2025", "2025-03-12", "Universitas Negeri Jakarta",     "Persetujuan Izin Magang Mahasiswa",           "Magang berlangsung April – Juni 2025"},
                {"006/SK/III/2025", "2025-03-28", "Bank BNI Cabang Utama",          "Surat Pernyataan Pelunasan Pinjaman",         "Bukti transfer terlampir, Ref. TRF-20250328"},
                {"007/SK/IV/2025",  "2025-04-08", "Kementerian Keuangan RI",        "Tanggapan atas Surat Edaran APBN Triwulan II","Tindak lanjut SE No. 12/MK.01/2025"},
                {"008/SK/IV/2025",  "2025-04-22", "CV. Sumber Berkah Lestari",      "Konfirmasi Pembayaran Faktur ATK",            "Telah ditransfer 22 April 2025"},
                {"009/SK/V/2025",   "2025-05-15", "Pemerintah Daerah Kab. Bogor",   "Pengiriman Data Statistik Wilayah",           "Data BPS 2024 terlampir format xlsx"},
                {"010/SK/VI/2025",  "2025-06-17", "PT. Telkom Indonesia Tbk",       "Konfirmasi Perpanjangan Layanan Internet",    "Kontrak diperpanjang 1 tahun mulai Juli 2025"},
            };

            for (Object[] row : dataKeluar) {
                psKeluar.setString(1, (String) row[0]);
                psKeluar.setDate(2,   Date.valueOf((String) row[1]));
                psKeluar.setString(3, (String) row[2]);
                psKeluar.setString(4, (String) row[3]);
                psKeluar.setString(5, (String) row[4]);
                psKeluar.executeUpdate();
            }

            return "{\"status\":\"sukses\", \"pesan\":\"Berhasil menambahkan 10 surat masuk dan 10 surat keluar data dummy!\"}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":\"error\", \"pesan\":\"Gagal memasukkan data dummy: " + e.getMessage() + "\"}";
        }
    }
}

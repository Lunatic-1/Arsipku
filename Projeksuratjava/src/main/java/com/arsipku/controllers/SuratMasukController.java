package com.arsipku.controllers;

import com.arsipku.config.Database;
import com.arsipku.model.SuratMasuk;
import com.google.gson.Gson; // Menggunakan kamus JSON

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SuratMasukController {

    // Fungsi untuk mengambil semua data (Read)
    public static String getAll() {
        // Siapkan wadah (list) kosong untuk menampung surat
        List<SuratMasuk> listSurat = new ArrayList<>();
        String sql = "SELECT * FROM surat_masuk ORDER BY id DESC"; // Ambil dari yang terbaru

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            // Looping: Selama ada baris data di MySQL, masukkan ke dalam cetakan Model
            while (rs.next()) {
                SuratMasuk surat = new SuratMasuk();
                surat.setId(rs.getLong("id"));
                surat.setNomorSurat(rs.getString("nomor_surat"));
                surat.setTanggalSurat(rs.getDate("tanggal_surat"));
                surat.setTanggalDiterima(rs.getDate("tanggal_diterima"));
                surat.setPengirim(rs.getString("pengirim"));
                surat.setPerihal(rs.getString("perihal"));
                surat.setKeterangan(rs.getString("keterangan"));
                surat.setFilePath(rs.getString("file_path"));

                listSurat.add(surat); // Masukkan cetakan yang sudah terisi ke dalam wadah
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil data surat: " + e.getMessage());
        }

        // Ubah wadah berisi data tadi menjadi format JSON
        Gson gson = new Gson();
        return gson.toJson(listSurat);
    }
}
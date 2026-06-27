package com.arsipku.controllers;

import com.arsipku.config.Database;
import com.arsipku.model.SuratMasuk;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Tambahan import untuk format tanggal

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SuratMasukController {

    public static String getAll(String sortOrder) {
        List<SuratMasuk> listSurat = new ArrayList<>();
        String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        String sql = "SELECT * FROM surat_masuk ORDER BY tanggal_surat " + order + ", id " + order;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

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

                listSurat.add(surat);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil data surat: " + e.getMessage());
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        return gson.toJson(listSurat);
    }

    public static String insert(String jsonBody) {
        // Pindahkan SEMUA proses ke dalam try-catch agar kalau ada error tidak "socket
        // hang up"
        try {
            // Beri tahu Gson bahwa format tanggal kita adalah Tahun-Bulan-Tanggal
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            SuratMasuk surat = gson.fromJson(jsonBody, SuratMasuk.class);

            String sql = "INSERT INTO surat_masuk (nomor_surat, tanggal_surat, tanggal_diterima, pengirim, perihal, keterangan) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = Database.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, surat.getNomorSurat());
                stmt.setDate(2, surat.getTanggalSurat());
                stmt.setDate(3, surat.getTanggalDiterima());
                stmt.setString(4, surat.getPengirim());
                stmt.setString(5, surat.getPerihal());
                stmt.setString(6, surat.getKeterangan());

                stmt.executeUpdate();

                return "{\"status\":\"sukses\", \"pesan\":\"Data surat masuk berhasil disimpan!\"}";
            }

        } catch (Exception e) {
            // Jika ada format JSON yang salah, tangkap di sini dan beri tahu REST Client
            System.out.println("Gagal insert data surat: " + e.getMessage());
            e.printStackTrace(); // Tampilkan detail error di terminal VS Code
            return "{\"status\":\"error\", \"pesan\":\"Gagal memproses data: " + e.getMessage() + "\"}";
        }
    }

    public static String delete(String jsonBody) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject json = gson.fromJson(jsonBody, com.google.gson.JsonObject.class);
        String nomorSurat = json.get("nomorSurat").getAsString();

        com.google.gson.JsonObject response = new com.google.gson.JsonObject();
        try {
            java.sql.Connection conn = com.arsipku.config.Database.getConnection();
            String sql = "DELETE FROM surat_masuk WHERE nomor_surat = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomorSurat);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.addProperty("status", "sukses");
                response.addProperty("pesan", "Data surat masuk berhasil dihapus!");
            } else {
                response.addProperty("status", "gagal");
                response.addProperty("pesan", "Nomor surat tidak ditemukan.");
            }
        } catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("pesan", "Gagal menghapus database: " + e.getMessage());
        }
        return gson.toJson(response);
    }
}

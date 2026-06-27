package com.arsipku.controllers;

import com.arsipku.config.Database;
import com.arsipku.model.SuratKeluar; // Pastikan ini mengarah ke model SuratKeluar
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SuratKeluarController {

    // Fungsi untuk Read (Ambil Data)
    public static String getAll(String sortOrder) {
        List<SuratKeluar> listSurat = new ArrayList<>();
        String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        String sql = "SELECT * FROM surat_keluar ORDER BY tanggal_surat " + order + ", id " + order;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SuratKeluar surat = new SuratKeluar();
                surat.setId(rs.getLong("id"));
                surat.setNomorSurat(rs.getString("nomor_surat"));
                surat.setTanggalSurat(rs.getDate("tanggal_surat"));
                surat.setTujuan(rs.getString("tujuan")); // Berbeda dengan surat masuk
                surat.setPerihal(rs.getString("perihal"));
                surat.setKeterangan(rs.getString("keterangan"));
                surat.setFilePath(rs.getString("file_path"));

                listSurat.add(surat);
            }
        } catch (Exception e) {
            System.out.println("Gagal ambil data surat keluar: " + e.getMessage());
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        return gson.toJson(listSurat);
    }

    // Fungsi untuk Create (Tambah Data)
    public static String insert(String jsonBody) {
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            SuratKeluar surat = gson.fromJson(jsonBody, SuratKeluar.class);

            // Kolomnya disesuaikan dengan tabel surat_keluar
            String sql = "INSERT INTO surat_keluar (nomor_surat, tanggal_surat, tujuan, perihal, keterangan) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = Database.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, surat.getNomorSurat());
                stmt.setDate(2, surat.getTanggalSurat());
                stmt.setString(3, surat.getTujuan());
                stmt.setString(4, surat.getPerihal());
                stmt.setString(5, surat.getKeterangan());

                stmt.executeUpdate();

                return "{\"status\":\"sukses\", \"pesan\":\"Data surat keluar berhasil disimpan!\"}";
            }
        } catch (Exception e) {
            System.out.println("Gagal insert data surat keluar: " + e.getMessage());
            e.printStackTrace();
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
            String sql = "DELETE FROM surat_keluar WHERE nomor_surat = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomorSurat);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.addProperty("status", "sukses");
                response.addProperty("pesan", "Data surat keluar berhasil dihapus!");
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

    public static String update(String jsonBody) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject json = gson.fromJson(jsonBody, com.google.gson.JsonObject.class);

        String nomorSurat = json.get("nomorSurat").getAsString();
        String tanggalSurat = json.get("tanggalSurat").getAsString();
        String tujuan = json.get("tujuan").getAsString();
        String perihal = json.get("perihal").getAsString();
        String keterangan = json.has("keterangan") ? json.get("keterangan").getAsString() : "";

        com.google.gson.JsonObject response = new com.google.gson.JsonObject();
        try {
            java.sql.Connection conn = com.arsipku.config.Database.getConnection();
            String sql = "UPDATE surat_keluar SET tanggal_surat = ?, tujuan = ?, perihal = ?, keterangan = ? WHERE nomor_surat = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, tanggalSurat);
            pstmt.setString(2, tujuan);
            pstmt.setString(3, perihal);
            pstmt.setString(4, keterangan);
            pstmt.setString(5, nomorSurat);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.addProperty("status", "sukses");
                response.addProperty("pesan", "Data surat keluar berhasil diperbarui!");
            } else {
                response.addProperty("status", "gagal");
                response.addProperty("pesan", "Data tidak ditemukan!");
            }
        } catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("pesan", "Gagal update database: " + e.getMessage());
        }
        return gson.toJson(response);
    }
}
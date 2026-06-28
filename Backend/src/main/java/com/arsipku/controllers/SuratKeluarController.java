package com.arsipku.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SuratKeluarController {

    // Helper: Mengubah Base64 dari Svelte menjadi File Fisik
    private static String saveFile(JsonObject json) {
        if (json.has("fileBase64") && !json.get("fileBase64").isJsonNull()) {
            String base64Data = json.get("fileBase64").getAsString();
            if (!base64Data.isEmpty()) {
                // Ambil nama asli atau gunakan default
                String originalName = json.has("fileName") ? json.get("fileName").getAsString() : "dokumen.pdf";
                // Bikin nama unik biar file tidak saling timpa
                String uniqueName = System.currentTimeMillis() + "_"
                        + originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                String path = "uploads/" + uniqueName;

                try {
                    // Hapus metadata Svelte ("data:application/pdf;base64,...")
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.split(",")[1];
                    }
                    byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Data);
                    java.nio.file.Files.write(java.nio.file.Paths.get(path), decodedBytes);
                    return path; // Kembalikan lokasi file untuk disimpan ke database
                } catch (Exception e) {
                    System.out.println("Gagal menyimpan file fisik: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public static String getAll(String sortOrder) {
        JsonArray array = new JsonArray();
        try {
            Connection conn = com.arsipku.config.Database.getConnection();
            String sql = "SELECT * FROM surat_keluar ORDER BY tanggal_surat " + sortOrder;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nomorSurat", rs.getString("nomor_surat"));
                obj.addProperty("tanggalSurat", rs.getString("tanggal_surat"));
                obj.addProperty("tujuan", rs.getString("tujuan")); // Khusus Surat Keluar
                obj.addProperty("perihal", rs.getString("perihal"));
                obj.addProperty("keterangan", rs.getString("keterangan"));

                // Kirim info path file ke Svelte
                obj.addProperty("filePath", rs.getString("file_path"));
                array.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Gson().toJson(array);
    }

    public static String insert(String jsonBody) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonBody, JsonObject.class);
        JsonObject response = new JsonObject();

        String nomorSurat = json.get("nomorSurat").getAsString();
        String tanggalSurat = json.get("tanggalSurat").getAsString();
        String tujuan = json.get("tujuan").getAsString(); // Khusus Surat Keluar
        String perihal = json.get("perihal").getAsString();
        String keterangan = json.has("keterangan") ? json.get("keterangan").getAsString() : "";

        // Eksekusi pembuatan file
        String filePath = saveFile(json);

        try {
            Connection conn = com.arsipku.config.Database.getConnection();
            String sql = "INSERT INTO surat_keluar (nomor_surat, tanggal_surat, tujuan, perihal, keterangan, file_path) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomorSurat);
            pstmt.setString(2, tanggalSurat);
            pstmt.setString(3, tujuan);
            pstmt.setString(4, perihal);
            pstmt.setString(5, keterangan);
            pstmt.setString(6, filePath); // Simpan path-nya

            pstmt.executeUpdate();
            response.addProperty("status", "sukses");
            response.addProperty("pesan", "Data surat keluar beserta dokumen berhasil disimpan!");
        } catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("pesan", "Gagal menyimpan: " + e.getMessage());
        }
        return gson.toJson(response);
    }

    public static String update(String jsonBody) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonBody, JsonObject.class);
        JsonObject response = new JsonObject();

        String nomorSurat = json.get("nomorSurat").getAsString();
        String tanggalSurat = json.get("tanggalSurat").getAsString();
        String tujuan = json.get("tujuan").getAsString(); // Khusus Surat Keluar
        String perihal = json.get("perihal").getAsString();
        String keterangan = json.has("keterangan") ? json.get("keterangan").getAsString() : "";

        // Cek apakah user meng-upload file baru saat edit
        String newFilePath = saveFile(json);

        try {
            Connection conn = com.arsipku.config.Database.getConnection();
            String sql;

            // Logika cerdas: Kalau ada file baru, update path-nya. Kalau tidak, biarkan
            // file lama.
            if (newFilePath != null) {
                sql = "UPDATE surat_keluar SET tanggal_surat = ?, tujuan = ?, perihal = ?, keterangan = ?, file_path = ? WHERE nomor_surat = ?";
            } else {
                sql = "UPDATE surat_keluar SET tanggal_surat = ?, tujuan = ?, perihal = ?, keterangan = ? WHERE nomor_surat = ?";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tanggalSurat);
            pstmt.setString(2, tujuan);
            pstmt.setString(3, perihal);
            pstmt.setString(4, keterangan);

            if (newFilePath != null) {
                pstmt.setString(5, newFilePath);
                pstmt.setString(6, nomorSurat);
            } else {
                pstmt.setString(5, nomorSurat);
            }

            pstmt.executeUpdate();
            response.addProperty("status", "sukses");
            response.addProperty("pesan", "Data surat keluar berhasil diperbarui!");
        } catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("pesan", "Gagal update: " + e.getMessage());
        }
        return gson.toJson(response);
    }

    public static String delete(String jsonBody) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonBody, JsonObject.class);
        JsonObject response = new JsonObject();
        String nomorSurat = json.get("nomorSurat").getAsString();

        try {
            Connection conn = com.arsipku.config.Database.getConnection();
            String sql = "DELETE FROM surat_keluar WHERE nomor_surat = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomorSurat);
            pstmt.executeUpdate();

            response.addProperty("status", "sukses");
            response.addProperty("pesan", "Data berhasil dihapus!");
        } catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("pesan", "Gagal menghapus: " + e.getMessage());
        }
        return gson.toJson(response);
    }
}
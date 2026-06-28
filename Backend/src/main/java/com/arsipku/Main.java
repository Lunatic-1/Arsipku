package com.arsipku;

import com.arsipku.config.Database;
import com.arsipku.controllers.SuratMasukController;
import com.arsipku.controllers.SuratKeluarController;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.Connection;

public class Main {

    // Helper: ambil nilai query parameter dari URL (misal: ?sort=desc)
    private static String getQueryParam(com.sun.net.httpserver.HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery(); // "sort=desc"
        if (query == null)
            return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equalsIgnoreCase(paramName)) {
                return kv[1];
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Mencoba menyalakan mesin...");

        Connection conn = Database.getConnection();
        if (conn != null) {
            System.out.println("Sip! Database aman.");

            // --- AUTO SETUP: Fitur Upload File ---
            try {
                // 1. Buat folder 'uploads' di dalam proyek jika belum ada
                java.io.File uploadDir = new java.io.File("uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                    System.out.println("Folder 'uploads' berhasil dibuat untuk menyimpan file.");
                }

                // 2. Tambahkan kolom file_path ke tabel MySQL
                java.sql.Statement stmt = conn.createStatement();
                stmt.execute("ALTER TABLE surat_masuk ADD COLUMN IF NOT EXISTS file_path VARCHAR(255)");
                stmt.execute("ALTER TABLE surat_keluar ADD COLUMN IF NOT EXISTS file_path VARCHAR(255)");
                System.out.println("Migrasi kolom file_path berhasil dicek/ditambahkan.");
            } catch (Exception e) {
                System.out.println("Gagal setup folder/database: " + e.getMessage());
            }
            // -------------------------------------
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // 1. Endpoint Test
        server.createContext("/api/test", (exchange -> {
            String response = "Halo! API Aplikasi Surat berjalan dengan lancar.";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }));

        // 1.5 Endpoint Seed Data
        server.createContext("/api/seed", (exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            String jsonResponse = com.arsipku.controllers.SeedController.seedData();

            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }));

        // 2. Endpoint Tambah Data Surat Masuk (POST) - harus didaftarkan SEBELUM
        // /api/surat-masuk
        server.createContext("/api/surat-masuk/tambah", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String jsonResponse = SuratMasukController.insert(body);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(204, -1);
            }
        }));

        // 3. Endpoint Hapus Data Surat Masuk (POST) - harus didaftarkan SEBELUM
        // /api/surat-masuk
        server.createContext("/api/surat-masuk/hapus", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String jsonResponse = SuratMasukController.delete(body);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }));

        // Endpoint Update Surat Masuk (POST)
        server.createContext("/api/surat-masuk/update", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                String jsonResponse = SuratMasukController.update(body);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }));

        // Endpoint Update Surat Keluar (POST)
        server.createContext("/api/surat-keluar/update", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                String jsonResponse = SuratKeluarController.update(body);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }));

        // 4. Endpoint Ambil Data Surat Masuk (GET) - didaftarkan TERAKHIR agar tidak
        // menangkap sub-path
        server.createContext("/api/surat-masuk", (exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            // Baca parameter ?sort=asc atau ?sort=desc (default: desc = terbaru dulu)
            String sortOrder = getQueryParam(exchange, "sort");
            if (sortOrder == null)
                sortOrder = "DESC";

            String jsonResponse = SuratMasukController.getAll(sortOrder);

            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }));

        // 5. Endpoint Tambah Data Surat Keluar (POST) - harus didaftarkan SEBELUM
        // /api/surat-keluar
        server.createContext("/api/surat-keluar/tambah", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String jsonResponse = SuratKeluarController.insert(body);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(204, -1);
            }
        }));

        // 6. Endpoint Hapus Data Surat Keluar (POST) - harus didaftarkan SEBELUM
        // /api/surat-keluar
        server.createContext("/api/surat-keluar/hapus", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                java.io.InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String jsonResponse = SuratKeluarController.delete(body);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }));

        // 7. Endpoint Ambil Data Surat Keluar (GET) - didaftarkan TERAKHIR agar tidak
        // menangkap sub-path
        server.createContext("/api/surat-keluar", (exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            // Baca parameter ?sort=asc atau ?sort=desc (default: desc = terbaru dulu)
            String sortOrder = getQueryParam(exchange, "sort");
            if (sortOrder == null)
                sortOrder = "DESC";

            String jsonResponse = SuratKeluarController.getAll(sortOrder);

            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }));
        // Endpoint untuk mengakses file (Preview)
        server.createContext("/api/files/", (exchange -> {
            String path = exchange.getRequestURI().getPath(); // /api/files/uploads/123_file.pdf
            String filename = path.replace("/api/files/", "");
            java.io.File file = new java.io.File(filename);

            if (file.exists()) {
                exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, file.length());
                java.io.OutputStream os = exchange.getResponseBody();
                java.nio.file.Files.copy(file.toPath(), os);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }));

        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("Server API sudah menyala!");
        System.out.println("Silakan buka: http://localhost:8000/api/surat-masuk");
        System.out.println("=========================================");
    }
}
package com.arsipku; // Pastikan baris 1 ini sesuai dengan kemauan VS Code kamu

import com.arsipku.config.Database;
import com.arsipku.controllers.SuratMasukController; // Jalur baru yang sudah bersih
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Mencoba menyalakan mesin...");

        Connection conn = Database.getConnection();
        if (conn != null) {
            System.out.println("Sip! Database aman.");
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

        // 2. Endpoint Ambil Data Surat Masuk (GET)
        server.createContext("/api/surat-masuk", (exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            // Memanggil fungsi getAll() dari controller yang sudah aman tadi
            String jsonResponse = SuratMasukController.getAll();

            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }));

        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("Server API sudah menyala!");
        System.out.println("Silakan buka: http://localhost:8000/api/surat-masuk");
        System.out.println("=========================================");
    }
}
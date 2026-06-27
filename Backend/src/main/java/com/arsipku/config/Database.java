package com.arsipku.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/db_arsipku";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("koneksi Berhasil.");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Aduh gagal Mas koneksinya !!! " + e.getMessage());
        }
        return conn;
    }
}

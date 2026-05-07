package com.khanh.util;

import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DBconnnection {

    public static Connection getConnection() throws Exception {
        String url = System.getenv("MARIADB_URL");
        String user = System.getenv("MARIADB_USER");
        String password = System.getenv("MARIADB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Check .env file");
        }
        return DriverManager.getConnection(url, user, password);
    }
}

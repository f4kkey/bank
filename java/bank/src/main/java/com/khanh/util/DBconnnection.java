package com.khanh.util;

import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DBconnnection {

    private static final Dotenv dotenv = Dotenv.load();

    public static Connection getConnection() throws Exception {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Check .env file");
        }
        return DriverManager.getConnection(url, user, password);
    }
}

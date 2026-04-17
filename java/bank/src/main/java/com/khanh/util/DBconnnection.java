package com.khanh.util;

import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DBconnnection {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))  // always use the JVM working directory
            .ignoreIfMissing()                           // don't crash if .env is absent
            .load();

    public static Connection getConnection() throws Exception {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                "DB credentials missing. Check .env file in: " + System.getProperty("user.dir"));
        }
        return DriverManager.getConnection(url, user, password);
    }
}
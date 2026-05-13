package com.khanh.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnnection {

    public static Connection getConnection() throws Exception {
        // String url = SecretStore.get("mariadb_url");
        // String user = SecretStore.get("mariadb_user");
        // String password = SecretStore.get("mariadb_password");
        String url = System.getenv("MARIADB_URL");
        String user = System.getenv("MARIADB_USER");
        String password = System.getenv("MARIADB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Check Vault secret path secret/data/bank");
        }
        return DriverManager.getConnection(url, user, password);
    }
}

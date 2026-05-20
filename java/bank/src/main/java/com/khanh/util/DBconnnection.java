package com.khanh.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBconnnection {

    // The connection pool
    private static final HikariDataSource dataSource;

    // Static block initializes the pool once when the class is loaded
    static {
        String url = SecretStore.get("mariadb_url");
        String user = SecretStore.get("mariadb_user");
        String password = SecretStore.get("mariadb_password");

        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Check Vault secret path secret/data/bank");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setConnectionTimeout(5000);
        config.setMaximumPoolSize(60);
        config.setMaxLifetime(600000);
        config.setIdleTimeout(60000);
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Call this inside your application's shutdown hook
     * to safely close the pool and prevent memory leaks.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
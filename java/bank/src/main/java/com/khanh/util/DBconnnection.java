package com.khanh.util;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DBconnnection {

    private static final String ENV_DIR = resolveEnvDir();

    private static String resolveEnvDir() {
        try {
            // Find the directory where the JAR (or compiled classes) lives
            File jarFile = new File(
                    DBconnnection.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = jarFile.isFile() ? jarFile.getParentFile() : jarFile; // jar vs classes/
            // If inside target/, go up to the project root
            if (dir.getName().equals("target") || dir.getParentFile().getName().equals("target")) {
                dir = dir.getParentFile();
                if (dir.getName().equals("target")) {
                    dir = dir.getParentFile();
                }
            }
            System.out.println("[DBconnection] Looking for .env in: " + dir.getAbsolutePath());
            return dir.getAbsolutePath();
        } catch (URISyntaxException e) {
            // Fallback: use the working directory
            return System.getProperty("user.dir");
        }
    }

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(ENV_DIR)
            .ignoreIfMissing()
            .load();

    public static Connection getConnection() throws Exception {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "DB credentials missing. Check .env file in: " + ENV_DIR);
        }
        return DriverManager.getConnection(url, user, password);
    }
}

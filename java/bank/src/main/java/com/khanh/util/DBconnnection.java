package com.app.util

import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DBconnnection {

    public static Connection getConnection() throws Exception{
        String url = Dotenv.load("DB_URL")
        String user = Dotenv.load("DB_USER")
        String password = Dotenv.load("DB_PASSWORD")
        return DriverManager.getConnection(url, user, password)
    }
}
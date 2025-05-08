package com.example.bookapi.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static String URL = "jdbc:mariadb://localhost:3306/book_db";
    private static String USER = "root";
    private static String PASS = "";

    public static void setUrl(String url) {
        URL = url;
    }

    public static void setUser(String user) {
        USER = user;
    }

    public static void setPass(String pass) {
        PASS = pass;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

}

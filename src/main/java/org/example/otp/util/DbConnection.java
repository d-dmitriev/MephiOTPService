package org.example.otp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = new Properties();
                try (InputStream input = DbConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
                    props.load(input);
                }

                String url = props.getProperty("db.url");
                String username = props.getProperty("db.username");
                String password = props.getProperty("db.password");

                Class.forName(props.getProperty("db.driver"));

                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка подключения к БД", e);
        }
        return connection;
    }
}
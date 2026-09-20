package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//manages config settings and key-value data where both keys and values are strings
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() throws SQLException, IOException {
        Properties prop = new Properties();
    
        //db.properties is optional now: it's gitignored, so it won't exist in Docker or CI
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if(input != null) {
                prop.load(input);
            }
        }
    
        String url = getSetting("db.url", "DB_URL", prop);
        String user = getSetting("db.user", "DB_USER", prop);
        String password = getSetting("db.password", "DB_PASSWORD", prop);
    
        if(url == null) {
            throw new FileNotFoundException("No database URL configured. Set the DB_URL environment variable or add db.properties.");
        }
    
        //hands url, user, password to the Driver, opens network connection to Postgres server
        return DriverManager.getConnection(url, user, password);
    }
    
    //priority: system property (tests) > environment variable (Docker) > db.properties file
    private static String getSetting(String propertyName, String envName, Properties fileProps) {
        String value = System.getProperty(propertyName);
        if(value == null) {
            value = System.getenv(envName);
        }
        if(value == null) {
            value = fileProps.getProperty(propertyName);
        }
        return value;
    }
}

package com.jasperstudio.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class JdbcDataAdapter implements DataAdapter {
    private String name;
    private final Map<String, String> properties = new HashMap<>();

    public static final String PROP_DRIVER = "driver";
    public static final String PROP_URL = "url";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_PASSWORD = "password";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    @Override
    public void test() throws Exception {
        String driver = properties.get(PROP_DRIVER);
        String url = properties.get(PROP_URL);
        String user = properties.get(PROP_USERNAME);
        String pass = properties.get(PROP_PASSWORD);

        if (driver != null && !driver.isEmpty()) {
            Class.forName(driver);
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            if (!conn.isValid(5)) {
                throw new Exception("Connection is not valid.");
            }
        }
    }

    public Connection getConnection() throws Exception {
        String driver = properties.get(PROP_DRIVER);
        String url = properties.get(PROP_URL);
        String user = properties.get(PROP_USERNAME);
        String pass = properties.get(PROP_PASSWORD);

        if (driver != null && !driver.isEmpty()) {
            Class.forName(driver);
        }
        return DriverManager.getConnection(url, user, pass);
    }

    @Override
    public String getType() {
        return "JDBC";
    }
}

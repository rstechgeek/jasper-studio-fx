package com.jasperstudio.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JsonDataAdapter implements DataAdapter {
    private String name;
    private final Map<String, String> properties = new HashMap<>();

    public static final String PROP_FILE = "file";
    public static final String PROP_DATE_PATTERN = "datePattern";

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
        if (properties != null)
            this.properties.putAll(properties);
    }

    @Override
    public void test() throws Exception {
        String filePath = properties.get(PROP_FILE);
        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) {
            throw new Exception("JSON File not found: " + filePath);
        }
        // Basic parsing check could be added here
    }

    @Override
    public String getType() {
        return "JSON";
    }
}

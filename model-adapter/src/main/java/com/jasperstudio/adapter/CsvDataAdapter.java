package com.jasperstudio.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CsvDataAdapter implements DataAdapter {
    private String name;
    private final Map<String, String> properties = new HashMap<>();

    public static final String PROP_FILE = "file";
    public static final String PROP_DELIMITER = "delimiter";
    public static final String PROP_HAS_HEADER = "item_header"; // standard naming

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
            throw new Exception("CSV File not found: " + filePath);
        }
    }

    @Override
    public String getType() {
        return "CSV";
    }
}

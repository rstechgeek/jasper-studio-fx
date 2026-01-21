package com.jasperstudio.adapter;

import java.util.Map;

public interface DataAdapter {
    String getName();

    void setName(String name);

    /**
     * Returns a map of properties used to configure this adapter.
     */
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    /**
     * Tests the connection or validity of the adapter.
     * 
     * @throws Exception if test fails.
     */
    void test() throws Exception;

    /**
     * Returns the type of this adapter (e.g., "JDBC", "JSON", "CSV").
     */
    String getType();
}

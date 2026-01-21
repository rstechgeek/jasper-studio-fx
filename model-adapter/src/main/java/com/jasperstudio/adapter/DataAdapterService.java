package com.jasperstudio.adapter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataAdapterService {
    private static DataAdapterService instance;

    private final ObservableList<DataAdapter> adapters = FXCollections.observableArrayList();

    private DataAdapterService() {
        // Load from disk (mock for now)
        loadMockAdapters();
    }

    public static synchronized DataAdapterService getInstance() {
        if (instance == null) {
            instance = new DataAdapterService();
        }
        return instance;
    }

    public ObservableList<DataAdapter> getAdapters() {
        return adapters;
    }

    public void addAdapter(DataAdapter adapter) {
        adapters.add(adapter);
    }

    public void removeAdapter(DataAdapter adapter) {
        adapters.remove(adapter);
    }

    private void loadMockAdapters() {
        // Mock JDBC
        JdbcDataAdapter jdbc = new JdbcDataAdapter();
        jdbc.setName("Sample DB (H2)");
        jdbc.getProperties().put(JdbcDataAdapter.PROP_DRIVER, "org.h2.Driver");
        jdbc.getProperties().put(JdbcDataAdapter.PROP_URL, "jdbc:h2:mem:testdb");
        jdbc.getProperties().put(JdbcDataAdapter.PROP_USERNAME, "sa");
        adapters.add(jdbc);

        // Mock JSON
        JsonDataAdapter json = new JsonDataAdapter();
        json.setName("Orders JSON");
        json.getProperties().put(JsonDataAdapter.PROP_FILE, "/tmp/orders.json");
        adapters.add(json);
    }
}

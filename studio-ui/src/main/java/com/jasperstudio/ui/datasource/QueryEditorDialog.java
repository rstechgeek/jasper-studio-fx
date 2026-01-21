package com.jasperstudio.ui.datasource;

import com.jasperstudio.adapter.DataAdapter;
import com.jasperstudio.adapter.JdbcDataAdapter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryEditorDialog {
    private static final Logger logger = LoggerFactory.getLogger(QueryEditorDialog.class);

    @FXML
    private Label lblActiveAdapter;
    @FXML
    private TextArea txtQuery;
    @FXML
    private TableView<ObservableList<String>> tblPreview;

    // We could add a Fields table here if needed
    @FXML
    private TableView<?> tblFields;

    private DataAdapter currentAdapter;
    private String resultQuery;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Setup initial state
        tblPreview.setPlaceholder(new Label("No Data / No Query Run"));
    }

    public void setAdapter(DataAdapter adapter) {
        this.currentAdapter = adapter;
        if (adapter != null) {
            lblActiveAdapter.setText("Active Adapter: " + adapter.getName() + " (" + adapter.getType() + ")");
        } else {
            lblActiveAdapter.setText("Active Adapter: None");
        }
    }

    public void setQuery(String query) {
        if (query != null) {
            txtQuery.setText(query);
        }
    }

    public String getQuery() {
        return resultQuery;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onReadFields() {
        // Parse metadata (simplified for now)
        onPreview();
    }

    @FXML
    private void onPreview() {
        if (currentAdapter == null) {
            showAlert("No Adapter", "Please select a Data Adapter first.");
            return;
        }

        String sql = txtQuery.getText();
        if (sql == null || sql.trim().isEmpty()) {
            showAlert("Empty Query", "Please enter an SQL query.");
            return;
        }

        if (currentAdapter instanceof JdbcDataAdapter) {
            runJdbcQuery((JdbcDataAdapter) currentAdapter, sql);
        } else {
            showAlert("Not Supported", "Preview only supported for JDBC Adapters in this version.");
        }
    }

    private void runJdbcQuery(JdbcDataAdapter adapter, String sql) {
        tblPreview.getColumns().clear();
        tblPreview.getItems().clear();

        try (Connection conn = adapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // create columns
            for (int i = 1; i <= colCount; i++) {
                final int finalIdx = i - 1;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(finalIdx)));
                tblPreview.getColumns().add(col);
            }

            // load data (limit 50)
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            int count = 0;
            while (rs.next() && count < 50) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
                count++;
            }
            tblPreview.setItems(data);

        } catch (Exception e) {
            logger.error("Query Execution Failed", e);
            showAlert("Execution Failed", "Error: " + e.getMessage());
        }
    }

    @FXML
    private void onOk() {
        resultQuery = txtQuery.getText();
        saved = true;
        close();
    }

    @FXML
    private void onCancel() {
        saved = false;
        close();
    }

    private void close() {
        Stage stage = (Stage) txtQuery.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

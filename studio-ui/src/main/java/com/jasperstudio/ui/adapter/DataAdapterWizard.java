package com.jasperstudio.ui.adapter;

import com.jasperstudio.adapter.DataAdapter;
import com.jasperstudio.adapter.DataAdapterService;
import com.jasperstudio.adapter.JdbcDataAdapter;
import com.jasperstudio.adapter.JsonDataAdapter;
import com.jasperstudio.adapter.CsvDataAdapter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class DataAdapterWizard {

    @FXML
    private javafx.scene.layout.StackPane stepContainer;

    // Step 1
    @FXML
    private VBox stepTypeSelection;
    @FXML
    private ListView<String> listAdapterTypes;

    // Step 2 Pages
    @FXML
    private VBox stepJdbcConfig;
    @FXML
    private VBox stepJsonConfig;
    @FXML
    private VBox stepCsvConfig;

    // JDBC Controls
    @FXML
    private TextField txtJdbcName;
    @FXML
    private ComboBox<String> comboJdbcDriver;
    @FXML
    private TextField txtJdbcUrl;
    @FXML
    private TextField txtJdbcUser;
    @FXML
    private PasswordField txtJdbcPass;
    @FXML
    private Label lblJdbcStatus;

    // JSON Controls
    @FXML
    private TextField txtJsonName;
    @FXML
    private TextField txtJsonFile;
    @FXML
    private TextField txtJsonDatePattern;

    // CSV Controls
    @FXML
    private TextField txtCsvName;
    @FXML
    private TextField txtCsvFile;
    @FXML
    private TextField txtCsvDelimiter;
    @FXML
    private CheckBox chkCsvHeaders;

    // Navigation
    @FXML
    private Button btnBack;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnFinish;

    private int currentStep = 1;
    private String selectedType = null;

    @FXML
    public void initialize() {
        listAdapterTypes.getItems().addAll("Database JDBC Connection", "JSON File", "CSV File");
        listAdapterTypes.getSelectionModel().select(0);

        // JDBC Drivers Pre-fill
        comboJdbcDriver.getItems().addAll(
                "org.h2.Driver",
                "com.mysql.cj.jdbc.Driver",
                "org.postgresql.Driver",
                "oracle.jdbc.OracleDriver");

        updateNavigationState();
    }

    @FXML
    private void onNext() {
        if (currentStep == 1) {
            String selection = listAdapterTypes.getSelectionModel().getSelectedItem();
            if (selection == null)
                return;

            selectedType = selection;
            currentStep = 2;

            stepTypeSelection.setVisible(false);

            if (selection.contains("JDBC")) {
                stepJdbcConfig.setVisible(true);
                txtJdbcName.setText("New JDBC Adapter");
            } else if (selection.contains("JSON")) {
                stepJsonConfig.setVisible(true);
                txtJsonName.setText("New JSON Adapter");
            } else if (selection.contains("CSV")) {
                stepCsvConfig.setVisible(true);
                txtCsvName.setText("New CSV Adapter");
            }
        }
        updateNavigationState();
    }

    @FXML
    private void onBack() {
        if (currentStep == 2) {
            currentStep = 1;
            selectedType = null;

            stepJdbcConfig.setVisible(false);
            stepJsonConfig.setVisible(false);
            stepCsvConfig.setVisible(false);
            stepTypeSelection.setVisible(true);
        }
        updateNavigationState();
    }

    @FXML
    private void testJdbcConnection() {
        try {
            JdbcDataAdapter adapter = new JdbcDataAdapter();
            adapter.getProperties().put(JdbcDataAdapter.PROP_DRIVER, comboJdbcDriver.getValue());
            adapter.getProperties().put(JdbcDataAdapter.PROP_URL, txtJdbcUrl.getText());
            adapter.getProperties().put(JdbcDataAdapter.PROP_USERNAME, txtJdbcUser.getText());
            adapter.getProperties().put(JdbcDataAdapter.PROP_PASSWORD, txtJdbcPass.getText());

            adapter.test();
            lblJdbcStatus.setText("Connection Successful!");
            lblJdbcStatus.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblJdbcStatus.setText("Error: " + e.getMessage());
            lblJdbcStatus.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void browseJsonFile() {
        File file = showFileChooser("Select JSON File");
        if (file != null)
            txtJsonFile.setText(file.getAbsolutePath());
    }

    @FXML
    private void browseCsvFile() {
        File file = showFileChooser("Select CSV File");
        if (file != null)
            txtCsvFile.setText(file.getAbsolutePath());
    }

    private File showFileChooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        return fc.showOpenDialog(stepContainer.getScene().getWindow());
    }

    @FXML
    private void onFinish() {
        DataAdapter adapter = null;

        if (selectedType.contains("JDBC")) {
            JdbcDataAdapter jdbc = new JdbcDataAdapter();
            jdbc.setName(txtJdbcName.getText());
            jdbc.getProperties().put(JdbcDataAdapter.PROP_DRIVER, comboJdbcDriver.getValue());
            jdbc.getProperties().put(JdbcDataAdapter.PROP_URL, txtJdbcUrl.getText());
            jdbc.getProperties().put(JdbcDataAdapter.PROP_USERNAME, txtJdbcUser.getText());
            jdbc.getProperties().put(JdbcDataAdapter.PROP_PASSWORD, txtJdbcPass.getText());
            adapter = jdbc;
        } else if (selectedType.contains("JSON")) {
            JsonDataAdapter json = new JsonDataAdapter();
            json.setName(txtJsonName.getText());
            json.getProperties().put(JsonDataAdapter.PROP_FILE, txtJsonFile.getText());
            json.getProperties().put(JsonDataAdapter.PROP_DATE_PATTERN, txtJsonDatePattern.getText());
            adapter = json;
        } else if (selectedType.contains("CSV")) {
            CsvDataAdapter csv = new CsvDataAdapter();
            csv.setName(txtCsvName.getText());
            csv.getProperties().put(CsvDataAdapter.PROP_FILE, txtCsvFile.getText());
            csv.getProperties().put(CsvDataAdapter.PROP_DELIMITER, txtCsvDelimiter.getText());
            csv.getProperties().put(CsvDataAdapter.PROP_HAS_HEADER, String.valueOf(chkCsvHeaders.isSelected()));
            adapter = csv;
        }

        if (adapter != null) {
            DataAdapterService.getInstance().addAdapter(adapter);
            close();
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) stepContainer.getScene().getWindow();
        stage.close();
    }

    private void updateNavigationState() {
        if (currentStep == 1) {
            btnBack.setDisable(true);
            btnNext.setDisable(false);
            btnFinish.setDisable(true);
        } else {
            btnBack.setDisable(false);
            btnNext.setDisable(true); // Wizard ends at step 2
            btnFinish.setDisable(false);
        }
    }
}

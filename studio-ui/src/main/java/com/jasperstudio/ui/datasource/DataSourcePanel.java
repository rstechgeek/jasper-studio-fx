package com.jasperstudio.ui.datasource;

import com.jasperstudio.adapter.DataAdapter;
import com.jasperstudio.adapter.DataAdapterService;
import com.jasperstudio.adapter.JdbcDataAdapter;
import com.jasperstudio.adapter.JsonDataAdapter;
import com.jasperstudio.adapter.CsvDataAdapter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;

/**
 * Panel to display data source structure and allow dragging fields.
 */
public class DataSourcePanel extends VBox {

    @FXML
    private TreeView<String> treeView;

    @FXML
    private ComboBox<DataAdapter> comboAdapters;

    public DataSourcePanel() {
        loadFXML();

        setupCombo();

        // Initial Mock Data
        treeView.setRoot(new TreeItem<>("No Adapter Selected"));
        treeView.setCellFactory(params -> new FieldTreeCell());
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DataSourcePanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DataSourcePanel.fxml", e);
        }
    }

    private void setupCombo() {
        comboAdapters.setItems(DataAdapterService.getInstance().getAdapters());

        comboAdapters.setConverter(new StringConverter<>() {
            @Override
            public String toString(DataAdapter object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public DataAdapter fromString(String string) {
                return null; // Not needed
            }
        });

        comboAdapters.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            loadMetaData(newVal);
        });
    }

    @FXML
    private void onCreateAdapter() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jasperstudio/ui/adapter/DataAdapterWizard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Data Adapter Configuration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            // In a real app, show Error Dialog
        }
    }

    private com.jasperstudio.designer.DesignerEngine engine;

    public void setDesignerEngine(com.jasperstudio.designer.DesignerEngine engine) {
        this.engine = engine;
        // Optionally bind things
    }

    @FXML
    private void onRunQuery() {
        if (engine == null || engine.getDesign() == null) {
            // alert or ignore
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jasperstudio/ui/datasource/QueryEditorDialog.fxml"));
            Parent root = loader.load();
            QueryEditorDialog controller = loader.getController();

            // Pass State
            DataAdapter activeAdapter = comboAdapters.getValue();
            controller.setAdapter(activeAdapter);
            controller.setQuery(engine.getDesign().getQueryString());

            Stage stage = new Stage();
            stage.setTitle("Query Editor" + (activeAdapter != null ? " - " + activeAdapter.getName() : ""));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Save if needed
            if (controller.isSaved()) {
                engine.setQueryString(controller.getQuery());
                if (activeAdapter != null) {
                    // Update field tree
                    loadMetaData(activeAdapter);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteAdapter() {
        DataAdapter selected = comboAdapters.getValue();
        if (selected == null) {
            return; // nothing selected
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Data Adapter");
        alert.setHeaderText("Delete '" + selected.getName() + "'?");
        alert.setContentText("Are you sure you want to delete this Data Adapter? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DataAdapterService.getInstance().removeAdapter(selected);
                // The combo listener should handle clearing if the selected item is removed
            }
        });
    }

    private void loadMetaData(DataAdapter adapter) {
        if (adapter == null) {
            treeView.setRoot(new TreeItem<>("No Selection"));
            return;
        }

        if (adapter instanceof JdbcDataAdapter) {
            treeView.setRoot(SqlSchemaInspector.inspect((JdbcDataAdapter) adapter));
        } else if (adapter instanceof JsonDataAdapter) {
            TreeItem<String> root = new TreeItem<>(adapter.getName());
            root.getChildren().add(new TreeItem<>("id"));
            root.getChildren().add(new TreeItem<>("name (from JSON)"));
            treeView.setRoot(root);
        } else if (adapter instanceof CsvDataAdapter) {
            TreeItem<String> root = new TreeItem<>(adapter.getName());
            root.getChildren().add(new TreeItem<>("Column 1"));
            root.getChildren().add(new TreeItem<>("Column 2"));
            treeView.setRoot(root);
        }
    }

    private static class FieldTreeCell extends TreeCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);

                // Only allow dragging leaf nodes (fields), not the root or tables
                // For SQL Schema, leafs are columns.
                if (getTreeItem().isLeaf()) {
                    setOnDragDetected(event -> {
                        Dragboard db = startDragAndDrop(TransferMode.COPY);
                        ClipboardContent content = new ClipboardContent();
                        // Format: FIELD:fieldName
                        content.putString("FIELD:" + item);
                        db.setContent(content);

                        // Visual feedback
                        Text t = new Text(item);
                        WritableImage snapshot = t.snapshot(null, null);
                        db.setDragView(snapshot);

                        event.consume();
                    });
                }
            }
        }
    }
}

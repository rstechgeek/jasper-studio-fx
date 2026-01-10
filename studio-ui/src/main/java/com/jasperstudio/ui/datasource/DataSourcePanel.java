package com.jasperstudio.ui.datasource;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Panel to display data source structure and allow dragging fields.
 */
public class DataSourcePanel extends VBox {

    @javafx.fxml.FXML
    private TreeView<String> treeView;

    public DataSourcePanel() {
        loadFXML();

        // Setup Tree Data
        treeView.setRoot(createTreeData());
        treeView.setCellFactory(params -> new FieldTreeCell());
    }

    private void loadFXML() {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("DataSourcePanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load DataSourcePanel.fxml", e);
        }
    }

    private TreeItem<String> createTreeData() {
        TreeItem<String> root = new TreeItem<>("Sample Data (JSON)");
        root.setExpanded(true);

        // Mock Data Fields
        java.util.List<TreeItem<String>> fields = new java.util.ArrayList<>();
        fields.add(new TreeItem<>("id"));
        fields.add(new TreeItem<>("name"));
        fields.add(new TreeItem<>("description"));
        fields.add(new TreeItem<>("price"));
        fields.add(new TreeItem<>("category"));
        fields.add(new TreeItem<>("stock_quantity"));

        root.getChildren().addAll(fields);
        return root;
    }

    // createTreeView removed as logic moved above

    // Old createTreeView logic replaced

    private static class FieldTreeCell extends TreeCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);

                // Only allow dragging leaf nodes (fields), not the root
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

package com.jasperstudio.ui.palette;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Tools Palette.
 */
public class PaletteView extends VBox {

    public enum ToolType {
        STATIC_TEXT,
        TEXT_FIELD,
        RECTANGLE,
        ELLIPSE,
        LINE,
        IMAGE,
        FRAME,
        BREAK,
        SUBREPORT,
        CHART,
        CROSSTAB,
        BARCODE
    }

    @FXML
    private ListView<ToolType> toolList;

    public PaletteView() {
        loadFXML();
        // init is done by FXML, but we need to set data
        ObservableList<ToolType> tools = FXCollections.observableArrayList(ToolType.values());
        toolList.setItems(tools);

        toolList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ToolType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.name());
                    org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon();
                    switch (item) {
                        case STATIC_TEXT:
                            icon.setIconLiteral("fas-font");
                            break;
                        case TEXT_FIELD:
                            icon.setIconLiteral("fas-database");
                            break;
                        case RECTANGLE:
                            icon.setIconLiteral("far-square");
                            break;
                        case ELLIPSE:
                            icon.setIconLiteral("far-circle");
                            break;
                        case LINE:
                            icon.setIconLiteral("fas-pen");
                            break;
                        case IMAGE:
                            icon.setIconLiteral("far-image");
                            break;
                        case FRAME:
                            icon.setIconLiteral("far-object-group");
                            break;
                        case BREAK:
                            icon.setIconLiteral("fas-cut");
                            break;
                        case SUBREPORT:
                            icon.setIconLiteral("far-file-alt");
                            break;
                        case CHART:
                            icon.setIconLiteral("fas-chart-bar");
                            break;
                        case CROSSTAB:
                            icon.setIconLiteral("fas-table");
                            break;
                        case BARCODE:
                            icon.setIconLiteral("fas-barcode");
                            break;
                        default:
                            icon.setIconLiteral("fas-question");
                    }
                    icon.setIconSize(16);
                    setGraphic(icon);
                }
            }
        });

        setupDragNet();
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PaletteView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load PaletteView.fxml", e);
        }
    }

    private void setupDragNet() {
        toolList.setOnDragDetected(event -> {
            ToolType selected = toolList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dragboard db = toolList.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(selected.name());
                db.setContent(content);

                // Visual Feedback (Ghost Image)
                // We can snapshot the cell that originated the event, or just create a text
                // image.
                // For simplicity/robustness, we'll create a simple Text node snapshot
                Text textNode = new Text(selected.name());
                new Scene(new javafx.scene.layout.StackPane(textNode)); // simplified formatting
                db.setDragView(textNode.snapshot(null, null));

                event.consume();
            }
        });
    }
}

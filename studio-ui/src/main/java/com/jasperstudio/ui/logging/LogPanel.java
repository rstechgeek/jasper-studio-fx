package com.jasperstudio.ui.logging;

import com.jasperstudio.descriptor.LogEntry;
import com.jasperstudio.designer.DesignerEngine;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class LogPanel extends TitledPane {

    private final DesignerEngine engine;
    private final TableView<LogEntry> logTable;
    private final TextArea detailsArea;

    public LogPanel(DesignerEngine engine) {
        this.engine = engine;
        this.setText("Error Log");
        this.setCollapsible(true);
        this.setExpanded(true);

        BorderPane content = new BorderPane();

        logTable = new TableView<>();
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LogEntry, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        timeCol.setPrefWidth(80);
        timeCol.setMaxWidth(80);
        timeCol.setMinWidth(80);

        TableColumn<LogEntry, String> msgCol = new TableColumn<>("Message");
        msgCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));

        logTable.getColumns().addAll(timeCol, msgCol);
        logTable.setItems(engine.getErrorLogs());

        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(100);

        logTable.getSelectionModel().selectedItemProperty().addListener((obs, old, entry) -> {
            if (entry != null) {
                detailsArea.setText(entry.getDescription());
            } else {
                detailsArea.clear();
            }
        });

        VBox vbox = new VBox(logTable, detailsArea);
        content.setCenter(vbox);

        this.setContent(content);

        // Auto-expand if new error arrives
        engine.getErrorLogs().addListener((javafx.collections.ListChangeListener<LogEntry>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    setExpanded(true);
                    logTable.scrollTo(0);
                    logTable.getSelectionModel().select(0);
                }
            }
        });
    }
}

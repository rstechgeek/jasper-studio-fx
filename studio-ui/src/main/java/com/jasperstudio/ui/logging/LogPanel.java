package com.jasperstudio.ui.logging;

import com.jasperstudio.descriptor.LogEntry;
import com.jasperstudio.designer.DesignerEngine;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.time.format.DateTimeFormatter;

public class LogPanel extends BorderPane {

    private DesignerEngine engine;
    private final TextArea errorLogArea;
    private javafx.collections.ListChangeListener<LogEntry> logListener;

    public LogPanel(DesignerEngine engine) {
        // Header
        javafx.scene.control.Label header = new javafx.scene.control.Label("Problems");
        header.getStyleClass().add("sidebar-header");
        header.setMaxWidth(Double.MAX_VALUE);
        this.setTop(header);

        errorLogArea = new TextArea();
        errorLogArea.setEditable(false);
        errorLogArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px;");

        this.setCenter(errorLogArea);

        this.logListener = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (LogEntry entry : c.getAddedSubList()) {
                        appendLog(entry);
                    }
                    // Auto-scroll logic if needed, but TextArea does it somewhat.
                    // To force scroll to bottom:
                    errorLogArea.selectPositionCaret(errorLogArea.getLength());
                    errorLogArea.deselect();
                }
            }
        };

        setDesignerEngine(engine);
    }

    public void setDesignerEngine(DesignerEngine newEngine) {
        if (this.engine != null) {
            this.engine.getErrorLogs().removeListener(logListener);
        }

        this.engine = newEngine;
        errorLogArea.clear();

        if (this.engine != null) {
            // Initial Population
            for (LogEntry entry : this.engine.getErrorLogs()) {
                appendLog(entry);
            }
            // Listen for new errors
            this.engine.getErrorLogs().addListener(logListener);
        }
    }

    private void appendLog(LogEntry entry) {
        String timestamp = entry.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ").append(entry.getMessage()).append("\n");
        if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
            sb.append(entry.getDescription()).append("\n");
        }
        sb.append("--------------------------------------------------\n");

        errorLogArea.appendText(sb.toString());
    }
}

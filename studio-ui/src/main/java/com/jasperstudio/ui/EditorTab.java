package com.jasperstudio.ui;

import com.jasperstudio.designer.DesignerEngine;
import com.jasperstudio.ui.canvas.ReportCanvas;
import javafx.scene.control.Tab;

import java.io.File;

/**
 * A Tab that contains a specific report design session.
 */
public class EditorTab extends Tab {

    private final DesignerEngine engine;
    private File file;

    public EditorTab(String title, DesignerEngine engine) {
        super(title);
        this.engine = engine;
        this.setContent(new ReportCanvas(engine));
    }

    public DesignerEngine getEngine() {
        return engine;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        if (file != null) {
            setText(file.getName());
            setTooltip(new javafx.scene.control.Tooltip(file.getAbsolutePath()));
        }
    }
}

package com.jasperstudio.designer;

import com.jasperstudio.model.JasperDesignModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Controller logic for the Designer.
 * Manages the current report vs selection state.
 */
public class DesignerEngine {

    public enum ViewMode {
        DESIGN, SOURCE, PREVIEW
    }

    private static final Logger logger = LoggerFactory.getLogger(DesignerEngine.class);

    private final javafx.collections.ObservableList<com.jasperstudio.descriptor.LogEntry> errorLogs = javafx.collections.FXCollections
            .observableArrayList();

    private String internalClipboard = null;

    private final ObjectProperty<JasperDesignModel> currentDesign = new SimpleObjectProperty<>();
    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
    private final javafx.beans.property.IntegerProperty gridSize = new javafx.beans.property.SimpleIntegerProperty(10);
    private final javafx.beans.property.BooleanProperty snapToGrid = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showGrid = new javafx.beans.property.SimpleBooleanProperty(
            true);
    private final javafx.beans.property.BooleanProperty showRulers = new javafx.beans.property.SimpleBooleanProperty(
            true);

    private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.DESIGN);
    private final javafx.beans.property.StringProperty xmlSource = new javafx.beans.property.SimpleStringProperty("");
    // Start, End index of selection
    private final ObjectProperty<javafx.util.Pair<Integer, Integer>> sourceSelection = new SimpleObjectProperty<>();
    // New View Properties
    private final javafx.beans.property.BooleanProperty snapToGuides = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty snapToGeometry = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showSpreadsheetTags = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showJSONTags = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showCSVTags = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showXLSTags = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty highlightRenderGrid = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showPDF508Tags = new javafx.beans.property.SimpleBooleanProperty(
            false);
    private final javafx.beans.property.BooleanProperty showErrorsForElements = new javafx.beans.property.SimpleBooleanProperty(
            false);

    private final com.jasperstudio.model.JrxmlService jrxmlService = new com.jasperstudio.model.JrxmlService();
    private final com.jasperstudio.descriptor.HistoryManager historyManager = new com.jasperstudio.descriptor.HistoryManager();

    public void executeCommand(com.jasperstudio.descriptor.Command cmd) {
        historyManager.execute(cmd);
    }

    public com.jasperstudio.descriptor.HistoryManager getHistoryManager() {
        return historyManager;
    }

    private void createBand(net.sf.jasperreports.engine.design.JasperDesign design, String type) {
        net.sf.jasperreports.engine.design.JRDesignBand band = new net.sf.jasperreports.engine.design.JRDesignBand();
        band.setHeight(50); // Default height

        switch (type) {
            case "Title":
                design.setTitle(band);
                break;
            case "Page Header":
                design.setPageHeader(band);
                break;
            case "Column Header":
                design.setColumnHeader(band);
                break;
            case "Detail":
                ((net.sf.jasperreports.engine.design.JRDesignSection) design.getDetailSection()).addBand(band);
                break;
            case "Column Footer":
                design.setColumnFooter(band);
                break;
            case "Page Footer":
                design.setPageFooter(band);
                break;
            case "Summary":
                design.setSummary(band);
                break;
            case "Last Page Footer":
                design.setLastPageFooter(band);
                break;
            case "No Data":
                design.setNoData(band);
                break;
            case "Background":
                design.setBackground(band);
                break;
        }
    }

    public DesignerEngine() {
        newDesign();
    }

    public void newDesign() {
        try {
            net.sf.jasperreports.engine.design.JasperDesign design = new net.sf.jasperreports.engine.design.JasperDesign();
            design.setName("NewReport");
            design.setPageWidth(595); // A4
            design.setPageHeight(842);
            design.setColumnWidth(555);
            design.setLeftMargin(0);
            design.setRightMargin(0);
            design.setTopMargin(0);
            design.setBottomMargin(0);

            // Ensure all standard bands exist
            createBand(design, "Title");
            createBand(design, "Page Header");
            createBand(design, "Column Header");
            createBand(design, "Detail");
            createBand(design, "Column Footer");
            createBand(design, "Page Footer");
            createBand(design, "Summary");
            // Optional bands: Last Page Footer, No Data, Background are not created by
            // default

            setDesign(new com.jasperstudio.model.JasperDesignModel(design));
            logger.info("New design created successfully.");
        } catch (Exception e) {
            logger.error("Failed to create new design", e);
        }
    }

    public void addBand(String type) {
        if (getDesign() == null)
            return;
        try {
            net.sf.jasperreports.engine.design.JasperDesign jd = getDesign().getDesign();
            boolean exists = false;
            switch (type) {
                case "Title":
                    exists = jd.getTitle() != null;
                    break;
                case "Page Header":
                    exists = jd.getPageHeader() != null;
                    break;
                case "Column Header":
                    exists = jd.getColumnHeader() != null;
                    break;
                case "Detail":
                    exists = jd.getDetailSection() != null &&
                            jd.getDetailSection().getBands() != null &&
                            jd.getDetailSection().getBands().length > 0;
                    break;
                case "Column Footer":
                    exists = jd.getColumnFooter() != null;
                    break;
                case "Page Footer":
                    exists = jd.getPageFooter() != null;
                    break;
                case "Last Page Footer":
                    exists = jd.getLastPageFooter() != null;
                    break;
                case "Summary":
                    exists = jd.getSummary() != null;
                    break;
                case "No Data":
                    exists = jd.getNoData() != null;
                    break;
                case "Background":
                    exists = jd.getBackground() != null;
                    break;
            }

            if (exists)
                return;

            createBand(jd, type);
            if (getDesign() != null) {
                getDesign().sync();
                // Select the new band
                com.jasperstudio.model.BandModel newBand = getDesign().getBand(type);
                if (newBand != null) {
                    setSelection(newBand);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to add band: {}", type, e);
        }
    }

    public void deleteBand(String type) {
        if (getDesign() == null)
            return;
        net.sf.jasperreports.engine.design.JasperDesign jd = getDesign().getDesign();
        switch (type) {
            case "Title":
                jd.setTitle(null);
                break;
            case "Page Header":
                jd.setPageHeader(null);
                break;
            case "Column Header":
                jd.setColumnHeader(null);
                break;
            case "Detail":
                if (jd.getDetailSection() instanceof net.sf.jasperreports.engine.design.JRDesignSection) {
                    net.sf.jasperreports.engine.design.JRDesignSection section = (net.sf.jasperreports.engine.design.JRDesignSection) jd
                            .getDetailSection();
                    // Clear all detail bands for now as our model assumes one
                    for (net.sf.jasperreports.engine.JRBand b : section.getBands()) {
                        section.removeBand(b);
                    }
                }
                break;
            case "Column Footer":
                jd.setColumnFooter(null);
                break;
            case "Page Footer":
                jd.setPageFooter(null);
                break;
            case "Last Page Footer":
                jd.setLastPageFooter(null);
                break;
            case "Summary":
                jd.setSummary(null);
                break;
            case "No Data":
                jd.setNoData(null);
                break;
            case "Background":
                jd.setBackground(null);
                break;
        }
        if (getDesign() != null)
            getDesign().sync();
    }

    public void openDesign(java.io.File file) throws Exception {
        var model = jrxmlService.load(file);
        setDesign(model);
    }

    public void saveDesign(java.io.File file) throws Exception {
        if (getDesign() != null) {
            jrxmlService.save(getDesign(), file);
        }
    }

    public void setDesign(JasperDesignModel design) {
        this.currentDesign.set(design);
        if (design != null) {
            setSelection(design);
        }
    }

    public JasperDesignModel getDesign() {
        return currentDesign.get();
    }

    public void setQueryString(String query) {
        if (getDesign() != null) {
            getDesign().setQueryString(query);
            // Optional: Trigger field extraction later
        }
    }

    public ObjectProperty<JasperDesignModel> currentDesignProperty() {
        return currentDesign;
    }

    public DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public javafx.beans.property.IntegerProperty gridSizeProperty() {
        return gridSize;
    }

    public javafx.beans.property.BooleanProperty snapToGridProperty() {
        return snapToGrid;
    }

    public void zoomIn() {
        double z = zoomFactor.get() + 0.1;
        if (z > 4.0)
            z = 4.0;
        zoomFactor.set(Math.round(z * 10.0) / 10.0);
    }

    public void zoomOut() {
        double z = zoomFactor.get() - 0.1;
        if (z < 0.2)
            z = 0.2;
        zoomFactor.set(Math.round(z * 10.0) / 10.0);
    }

    public javafx.beans.property.BooleanProperty showGridProperty() {
        return showGrid;
    }

    public javafx.beans.property.BooleanProperty showRulersProperty() {
        return showRulers;
    }

    // View Property Accessors
    public javafx.beans.property.BooleanProperty snapToGuidesProperty() {
        return snapToGuides;
    }

    public javafx.beans.property.BooleanProperty snapToGeometryProperty() {
        return snapToGeometry;
    }

    public javafx.beans.property.BooleanProperty showSpreadsheetTagsProperty() {
        return showSpreadsheetTags;
    }

    public javafx.beans.property.BooleanProperty showJSONTagsProperty() {
        return showJSONTags;
    }

    public javafx.beans.property.BooleanProperty showCSVTagsProperty() {
        return showCSVTags;
    }

    public javafx.beans.property.BooleanProperty showXLSTagsProperty() {
        return showXLSTags;
    }

    public javafx.beans.property.BooleanProperty highlightRenderGridProperty() {
        return highlightRenderGrid;
    }

    public javafx.beans.property.BooleanProperty showPDF508TagsProperty() {
        return showPDF508Tags;
    }

    public javafx.beans.property.BooleanProperty showErrorsForElementsProperty() {
        return showErrorsForElements;
    }

    // Selection State
    private final javafx.beans.property.ObjectProperty<Object> selection = new javafx.beans.property.SimpleObjectProperty<>();

    public javafx.beans.property.ObjectProperty<Object> selectionProperty() {
        return selection;
    }

    public Object getSelection() {
        return selection.get();
    }

    // Helper for ElementModel specific access if needed, but safer to check type
    public com.jasperstudio.model.ElementModel getSelectedElement() {
        if (selection.get() instanceof com.jasperstudio.model.ElementModel) {
            return (com.jasperstudio.model.ElementModel) selection.get();
        }
        return null;
    }

    public void setSelection(Object item) {
        this.selection.set(item);
    }

    public void clearSelection() {
        this.selection.set(null);
    }

    public javafx.collections.ObservableList<com.jasperstudio.descriptor.LogEntry> getErrorLogs() {
        return errorLogs;
    }

    public void logError(String message, Throwable t) {
        logger.error(message, t);
        String desc = t != null ? t.getMessage() : "";
        if (t != null) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            desc = sw.toString();
        }
        final String finalDesc = desc;
        javafx.application.Platform.runLater(() -> {
            errorLogs.add(0, new com.jasperstudio.descriptor.LogEntry(message, finalDesc));
        });
    }

    // View Mode Accessors
    public ObjectProperty<ViewMode> viewModeProperty() {
        return viewMode;
    }

    public void setViewMode(ViewMode mode) {
        this.viewMode.set(mode);
    }

    public ViewMode getViewMode() {
        return viewMode.get();
    }

    public javafx.beans.property.StringProperty xmlSourceProperty() {
        return xmlSource;
    }

    public void setXmlSource(String xml) {
        this.xmlSource.set(xml);
    }

    public String getXmlSource() {
        return xmlSource.get();
    }

    public ObjectProperty<javafx.util.Pair<Integer, Integer>> sourceSelectionProperty() {
        return sourceSelection;
    }

    public void setSourceSelection(int start, int end) {
        this.sourceSelection.set(new javafx.util.Pair<>(start, end));
    }

    // -- Logic --

    /**
     * Handles an object dropped onto the canvas.
     * 
     * @param typeString The string identifier from Palette (e.g., "STATIC_TEXT")
     * @param x          The relative X on the page
     * @param y          The relative Y on the page
     */
    /**
     * Handles an object dropped onto the canvas (Root).
     */
    public void handleDrop(String typeString, double x, double y) {
        handleDrop(typeString, x, y, (com.jasperstudio.model.ElementModel) null);
    }

    public void handleDrop(String typeString, double x, double y, com.jasperstudio.model.BandModel band) {
        if (getDesign() == null)
            return;
        try {
            createAndAddElement(typeString, x, y, band);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles an object dropped into a container.
     */
    public void handleDrop(String typeString, double x, double y, com.jasperstudio.model.ElementModel parentContainer) {
        if (getDesign() == null)
            return;

        try {
            net.sf.jasperreports.engine.design.JRDesignElement jrElement = null;

            if ("STATIC_TEXT".equals(typeString)) {
                var text = new net.sf.jasperreports.engine.design.JRDesignStaticText();
                text.setText("Static Text");
                text.setWidth(100);
                text.setHeight(30);
                jrElement = text;
            } else if ("TEXT_FIELD".equals(typeString)) {
                var field = new net.sf.jasperreports.engine.design.JRDesignTextField();
                field.setExpression(new net.sf.jasperreports.engine.design.JRDesignExpression("$F{Field}"));
                field.setWidth(100);
                field.setHeight(30);
                jrElement = field;
            } else if ("RECTANGLE".equals(typeString)) {
                var rect = new net.sf.jasperreports.engine.design.JRDesignRectangle();
                rect.setWidth(100);
                rect.setHeight(50);
                jrElement = rect;
            } else if ("ELLIPSE".equals(typeString)) {
                var ellipse = new net.sf.jasperreports.engine.design.JRDesignEllipse(null);
                ellipse.setWidth(100);
                ellipse.setHeight(50);
                jrElement = ellipse;
            } else if ("LINE".equals(typeString)) {
                var line = new net.sf.jasperreports.engine.design.JRDesignLine();
                line.setWidth(100);
                line.setHeight(1);
                jrElement = line;
            } else if ("IMAGE".equals(typeString)) {
                var img = new net.sf.jasperreports.engine.design.JRDesignImage(null);
                img.setWidth(60);
                img.setHeight(60);
                jrElement = img;
            } else if ("FRAME".equals(typeString)) {
                var frame = new net.sf.jasperreports.engine.design.JRDesignFrame();
                frame.setWidth(200);
                frame.setHeight(100);
                frame.getLineBox().setPadding(0);
                jrElement = frame;
            } else if ("BREAK".equals(typeString)) {
                var brk = new net.sf.jasperreports.engine.design.JRDesignBreak();
                brk.setType(net.sf.jasperreports.engine.type.BreakTypeEnum.PAGE);
                brk.setWidth(100);
                brk.setHeight(5);
                jrElement = brk;
            } else if ("SUBREPORT".equals(typeString)) {
                var sub = new net.sf.jasperreports.engine.design.JRDesignSubreport(null);
                sub.setExpression(
                        new net.sf.jasperreports.engine.design.JRDesignExpression("\"repo:subreport.jrxml\""));
                sub.setWidth(200);
                sub.setHeight(100);
                jrElement = sub;
            } else if ("CHART".equals(typeString)) {
                // Placeholder Chart (Pie) - Simplified for compilation
                var chart = new net.sf.jasperreports.engine.design.JRDesignRectangle();
                chart.getPropertiesMap().setProperty("com.jasperstudio.component.type", "CHART");
                chart.setWidth(200);
                chart.setHeight(150);
                jrElement = chart;
            } else if ("CROSSTAB".equals(typeString)) {
                var crosstab = new net.sf.jasperreports.crosstabs.design.JRDesignCrosstab(null);
                crosstab.setWidth(200);
                crosstab.setHeight(100);
                jrElement = crosstab;
            } else if ("BARCODE".equals(typeString)) {
                // Placeholder Barcode (Component Element wrapper would be better but complex
                // for now, use Image placeholder)
                var img = new net.sf.jasperreports.engine.design.JRDesignImage(null);
                img.setWidth(100);
                img.setHeight(50);
                // Mark as barcode via property? For now just visual placeholder
                img.getPropertiesMap().setProperty("com.jasperstudio.component.type", "BARCODE");
                jrElement = img;
            }

            if (jrElement != null) {
                jrElement.setX((int) x);
                jrElement.setY((int) y);
                jrElement.setUUID(java.util.UUID.randomUUID());

                com.jasperstudio.model.ElementModel model = new com.jasperstudio.model.ElementModel(jrElement);

                if (parentContainer != null
                        && parentContainer.getElement() instanceof net.sf.jasperreports.engine.design.JRDesignFrame) {
                    ((net.sf.jasperreports.engine.design.JRDesignFrame) parentContainer.getElement())
                            .addElement(jrElement);
                    // Note: We need to register this new model with the adapter or ensure the UI
                    // refreshes
                    // For Phase 1, we rely on the implementation plan's verification steps which
                    // implies visual updates.
                    // The Canvas will need to be smart enough to observe frame children or we force
                    // a refresh.
                } else {
                    getDesign().addElement(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAndAddElement(String typeString, double x, double y, com.jasperstudio.model.BandModel band) {
        net.sf.jasperreports.engine.design.JRDesignElement jrElement = null;

        if ("STATIC_TEXT".equals(typeString)) {
            var text = new net.sf.jasperreports.engine.design.JRDesignStaticText();
            text.setText("Static Text");
            text.setWidth(100);
            text.setHeight(30);
            jrElement = text;
        } else if ("TEXT_FIELD".equals(typeString)) {
            var field = new net.sf.jasperreports.engine.design.JRDesignTextField();
            field.setExpression(new net.sf.jasperreports.engine.design.JRDesignExpression("$F{Field}"));
            field.setWidth(100);
            field.setHeight(30);
            jrElement = field;
        } else if ("RECTANGLE".equals(typeString)) {
            var rect = new net.sf.jasperreports.engine.design.JRDesignRectangle();
            rect.setWidth(100);
            rect.setHeight(50);
            jrElement = rect;
        } else if ("ELLIPSE".equals(typeString)) {
            var ellipse = new net.sf.jasperreports.engine.design.JRDesignEllipse(null);
            ellipse.setWidth(100);
            ellipse.setHeight(50);
            jrElement = ellipse;
        } else if ("LINE".equals(typeString)) {
            var line = new net.sf.jasperreports.engine.design.JRDesignLine();
            line.setWidth(100);
            line.setHeight(1);
            jrElement = line;
        } else if ("IMAGE".equals(typeString)) {
            var img = new net.sf.jasperreports.engine.design.JRDesignImage(null);
            img.setWidth(60);
            img.setHeight(60);
            jrElement = img;
        } else if ("FRAME".equals(typeString)) {
            var frame = new net.sf.jasperreports.engine.design.JRDesignFrame();
            frame.setWidth(200);
            frame.setHeight(100);
            frame.getLineBox().setPadding(0);
            jrElement = frame;
        } else if ("BREAK".equals(typeString)) {
            var brk = new net.sf.jasperreports.engine.design.JRDesignBreak();
            brk.setType(net.sf.jasperreports.engine.type.BreakTypeEnum.PAGE);
            brk.setWidth(100);
            brk.setHeight(5);
            jrElement = brk;
        } else if ("SUBREPORT".equals(typeString)) {
            var sub = new net.sf.jasperreports.engine.design.JRDesignSubreport(null);
            sub.setExpression(new net.sf.jasperreports.engine.design.JRDesignExpression("\"repo:subreport.jrxml\""));
            sub.setWidth(200);
            sub.setHeight(100);
            jrElement = sub;
        } else if ("CHART".equals(typeString)) {
            var chart = new net.sf.jasperreports.engine.design.JRDesignRectangle();
            chart.getPropertiesMap().setProperty("com.jasperstudio.component.type", "CHART");
            chart.setWidth(200);
            chart.setHeight(150);
            jrElement = chart;
        } else if ("CROSSTAB".equals(typeString)) {
            var crosstab = new net.sf.jasperreports.crosstabs.design.JRDesignCrosstab(null);
            crosstab.setWidth(200);
            crosstab.setHeight(100);
            jrElement = crosstab;
        } else if ("BARCODE".equals(typeString)) {
            var img = new net.sf.jasperreports.engine.design.JRDesignImage(null);
            img.setWidth(100);
            img.setHeight(50);
            img.getPropertiesMap().setProperty("com.jasperstudio.component.type", "BARCODE");
            jrElement = img;
        }

        if (jrElement != null) {
            jrElement.setX((int) x);
            jrElement.setY((int) y);
            jrElement.setUUID(java.util.UUID.randomUUID());

            com.jasperstudio.model.ElementModel model = new com.jasperstudio.model.ElementModel(jrElement);
            if (band != null) {
                band.addElement(model);
                // Auto-resize band if element exceeds height
                int requiredHeight = jrElement.getY() + jrElement.getHeight();
                if (requiredHeight > band.getHeight()) {
                    band.setHeight(requiredHeight);
                }
            }
            setSelection(model);
        }
    }

    public void moveElementToBand(com.jasperstudio.model.ElementModel element,
            com.jasperstudio.model.BandModel targetBand, int newY) {
        if (element == null || targetBand == null)
            return;

        // Find current parent band
        com.jasperstudio.model.BandModel currentBand = null;
        for (com.jasperstudio.model.BandModel b : getDesign().getBands()) {
            if (b.getElements().contains(element)) {
                currentBand = b;
                break;
            }
        }

        if (currentBand != null && currentBand != targetBand) {
            currentBand.removeElement(element);
            targetBand.addElement(element);
        }

        // Update Y and check resize even if same band (moved within band)
        element.setY(newY);

        int requiredHeight = newY + (int) element.getHeight();
        if (requiredHeight > targetBand.getHeight()) {
            targetBand.setHeight(requiredHeight);
        }
    }

    /**
     * Groups the current selection into a new Frame.
     */
    public void groupSelection() {
        com.jasperstudio.model.ElementModel selectedModel = getSelectedElement();
        if (selectedModel == null)
            return;

        net.sf.jasperreports.engine.design.JRDesignElement selectedJR = selectedModel.getElement();

        // Find Parent
        net.sf.jasperreports.engine.JRElementGroup parentGroup = null;
        net.sf.jasperreports.engine.design.JRDesignBand title = (net.sf.jasperreports.engine.design.JRDesignBand) getDesign()
                .getDesign().getTitle();

        // Check if directly in Title
        if (title.getChildren().contains(selectedJR)) {
            parentGroup = title;
        } else {
            // Recursive search in Frames
            parentGroup = findParentFrame(title, selectedJR);
        }

        if (parentGroup == null) {
            logger.warn("Could not find parent for selection: {}", selectedJR);
            return;
        }

        try {
            // Create Frame
            net.sf.jasperreports.engine.design.JRDesignFrame newFrame = new net.sf.jasperreports.engine.design.JRDesignFrame();
            newFrame.setX(selectedJR.getX());
            newFrame.setY(selectedJR.getY());
            newFrame.setWidth(selectedJR.getWidth());
            newFrame.setHeight(selectedJR.getHeight());
            newFrame.setUUID(java.util.UUID.randomUUID());

            // Move element
            if (parentGroup instanceof net.sf.jasperreports.engine.design.JRDesignElementGroup) {
                ((net.sf.jasperreports.engine.design.JRDesignElementGroup) parentGroup).removeElement(selectedJR);
                ((net.sf.jasperreports.engine.design.JRDesignElementGroup) parentGroup).addElement(newFrame);
            } else if (parentGroup instanceof net.sf.jasperreports.engine.design.JRDesignFrame) {
                ((net.sf.jasperreports.engine.design.JRDesignFrame) parentGroup).removeElement(selectedJR);
                ((net.sf.jasperreports.engine.design.JRDesignFrame) parentGroup).addElement(newFrame);
            }

            selectedJR.setX(0);
            selectedJR.setY(0);
            newFrame.addElement(selectedJR);

            // Update Model Wrapper if Top-Level
            if (parentGroup == title) {
                // Remove old model from observable list (ReportCanvas handles removal now)
                getDesign().getElements().remove(selectedModel);
                // Add new model
                getDesign().addElement(new com.jasperstudio.model.ElementModel(newFrame));
            } else {
                // Nested change: Force Refresh
                com.jasperstudio.model.JasperDesignModel d = getDesign();
                setDesign(null);
                setDesign(d);
            }

            // Select the new frame (Best effort)
            if (parentGroup == title) {
                for (com.jasperstudio.model.ElementModel em : getDesign().getElements()) {
                    if (em.getElement() == newFrame) {
                        setSelection(em);
                        break;
                    }
                }
            } else {
                clearSelection();
            }

        } catch (Exception e) {
            logger.error("Failed to group selection", e);
        }
    }

    private net.sf.jasperreports.engine.JRElementGroup findParentFrame(
            net.sf.jasperreports.engine.JRElementGroup container,
            net.sf.jasperreports.engine.design.JRDesignElement target) {

        for (net.sf.jasperreports.engine.JRChild child : container.getChildren()) {
            if (child instanceof net.sf.jasperreports.engine.design.JRDesignFrame) {
                net.sf.jasperreports.engine.design.JRDesignFrame frame = (net.sf.jasperreports.engine.design.JRDesignFrame) child;
                if (frame.getChildren().contains(target)) {
                    return frame;
                }
                // Recurse
                net.sf.jasperreports.engine.JRElementGroup found = findParentFrame(frame, target);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    public void deleteSelection() {
        com.jasperstudio.model.ElementModel selected = getSelectedElement();
        if (selected == null)
            return;

        if (getDesign() == null)
            return;

        boolean removed = false;

        // 1. Check Bands
        for (com.jasperstudio.model.BandModel band : getDesign().getBands()) {
            if (band.getElements().contains(selected)) {
                band.removeElement(selected);
                removed = true;
                break;
            }
        }

        // 2. Check Recursive Frames
        if (!removed) {
            net.sf.jasperreports.engine.JRElementGroup parent = findParentFrame(getDesign().getDesign().getTitle(),
                    selected.getElement());
            if (parent instanceof net.sf.jasperreports.engine.design.JRDesignElementGroup) {
                ((net.sf.jasperreports.engine.design.JRDesignElementGroup) parent).removeElement(selected.getElement());
                removed = true;
                // Force sync/refresh for now as we don't have deep model events yet
                getDesign().sync();
            } else if (parent instanceof net.sf.jasperreports.engine.design.JRDesignFrame) {
                ((net.sf.jasperreports.engine.design.JRDesignFrame) parent).removeElement(selected.getElement());
                removed = true;
                getDesign().sync();
            }
        }

        if (removed) {
            clearSelection();
            logger.info("Deleted selection: {}", selected);
        } else {
            logger.warn("Could not find parent band for selection to delete: {}", selected);
        }
    }

    public void selectAll() {
        logger.info("Select All triggered (Multi-selection not yet supported)");
    }

    public void copy() {
        com.jasperstudio.model.ElementModel selected = getSelectedElement();
        if (selected == null)
            return;

        try {
            this.internalClipboard = jrxmlService.serializeElement(selected.getElement());
            logger.info("Copied element to clipboard");
        } catch (Exception e) {
            logError("Failed to copy element", e);
        }
    }

    public void cut() {
        copy();
        deleteSelection();
    }

    public void paste() {
        if (this.internalClipboard == null)
            return;

        try {
            net.sf.jasperreports.engine.design.JRDesignElement newElement = jrxmlService
                    .deserializeElement(this.internalClipboard);
            if (newElement != null) {
                // Offset
                newElement.setX(newElement.getX() + 10);
                newElement.setY(newElement.getY() + 10);
                newElement.setUUID(java.util.UUID.randomUUID());

                // Add to current band or parent of original selection?
                // Logic: If selection exists, try to add to its parent. Else add to Detail or
                // Title.
                // Simple logic: Add to Detail if exists, else Title.
                // Better: Get currently selected band? We don't track "selected band"
                // explicitly well yet,
                // but we can default to Title or find the band of the currently selected
                // element.

                com.jasperstudio.model.BandModel targetBand = null;
                com.jasperstudio.model.ElementModel selected = getSelectedElement();

                if (selected != null) {
                    // Find generic parent band of selection
                    for (com.jasperstudio.model.BandModel b : getDesign().getBands()) {
                        if (b.getElements().contains(selected)) {
                            targetBand = b;
                            break;
                        }
                    }
                }

                if (targetBand == null) {
                    // Default to Detail or Title
                    if (getDesign().getBand("Detail") != null)
                        targetBand = getDesign().getBand("Detail");
                    else
                        targetBand = getDesign().getBand("Title");
                }

                if (targetBand != null) {
                    com.jasperstudio.model.ElementModel model = new com.jasperstudio.model.ElementModel(newElement);
                    targetBand.addElement(model);
                    // Ensure band height accommodates
                    if (newElement.getY() + newElement.getHeight() > targetBand.getHeight()) {
                        targetBand.setHeight(newElement.getY() + newElement.getHeight());
                    }
                    setSelection(model);
                    logger.info("Pasted element");
                }
            }
        } catch (Exception e) {
            logError("Failed to paste element", e);
        }
    }
}

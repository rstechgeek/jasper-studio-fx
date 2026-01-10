package com.jasperstudio.ui.canvas;

import com.jasperstudio.descriptor.MoveElementCommand;
import com.jasperstudio.descriptor.ResizeElementCommand;
import com.jasperstudio.designer.DesignerEngine;
import com.jasperstudio.model.BandModel;
import com.jasperstudio.model.ElementModel;
import com.jasperstudio.model.JasperDesignModel;
import com.jasperstudio.model.JrxmlService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.crosstabs.design.JRDesignCrosstab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * The visualization of the Report Page.
 */
public class ReportCanvas extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(ReportCanvas.class);

    private final DesignerEngine engine;

    @FXML private StackPane workspaceArea;
    @FXML private Group pageGroup;
    @FXML private StackPane pagePane;
    @FXML private Pane gridLayer;
    @FXML private Pane contentLayer;
    @FXML private Pane adornerLayer;
    @FXML private RulerControl hRuler;
    @FXML private RulerControl vRuler;
    @FXML private ScrollPane internalScrollPane;

    @FXML private Button btnDesign;
    @FXML private Button btnSource;
    @FXML private Button btnPreview;

    @FXML private TextArea sourceEditor;
    @FXML private ScrollPane previewContainer;
    @FXML private ImageView previewImage;
    @FXML private HBox topBox;


    public ReportCanvas(DesignerEngine engine) {
        this.engine = engine;
        loadFXML();
        initUI(); // Post-load init
        setupListeners();
        setupViewModes();
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportCanvas.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            logger.error("Failed to load ReportCanvas.fxml", e);
            throw new RuntimeException("Failed to load ReportCanvas.fxml", e);
        }
    }

    private void initUI() {
        logger.debug("Initializing ReportCanvas UI...");
        // FXML handles structure. We just need to do runtime bindings.

        // Disable FitToWidth/Height to allow content to be larger than viewport
        // Actually, FXML has fitToWidth=true set, let's verify if we need to change it
        // based on original logic.
        // Original logic:
        // internalScrollPane.setFitToWidth(true);
        // internalScrollPane.setFitToHeight(true);
        // So FXML defaults are correct.

        // hRuler width binding
        hRuler.prefWidthProperty().bind(internalScrollPane.widthProperty());

        // Scroll listeners setup (already in setupListeners part, but let's confirm
        // placement)

        // Initial centering
        Platform.runLater(() -> {
            internalScrollPane.setHvalue(0.5);
            internalScrollPane.setVvalue(0.5);
            updateWorkspaceSize();
        });

        // Bind Rulers Visibility
        hRuler.visibleProperty().bind(engine.showRulersProperty());
        hRuler.managedProperty().bind(engine.showRulersProperty());
        vRuler.visibleProperty().bind(engine.showRulersProperty());
        vRuler.managedProperty().bind(engine.showRulersProperty());

        // Pass-through events for overlays
        adornerLayer.setPickOnBounds(false);
        gridLayer.setPickOnBounds(false);

        // Top Box visibility
        topBox.visibleProperty().bind(engine.showRulersProperty());
        topBox.managedProperty().bind(engine.showRulersProperty());

        // Listen to Scroll to update Rulers
        internalScrollPane.hvalueProperty().addListener(o -> updateRulers());
        internalScrollPane.vvalueProperty().addListener(o -> updateRulers());
        internalScrollPane.widthProperty().addListener(o -> updateRulers());
        internalScrollPane.heightProperty().addListener(o -> updateRulers());
        internalScrollPane.viewportBoundsProperty().addListener(o -> updateRulers());

        updateRulers();

        ChangeListener<Number> sizeListener = (obs, old, val) -> updateWorkspaceSize();
        engine.zoomFactorProperty().addListener(sizeListener);
        pagePane.widthProperty().addListener(sizeListener);
        pagePane.heightProperty().addListener(sizeListener);

        // CSS Classes
        internalScrollPane.getStyleClass().add("canvas-background");
        workspaceArea.getStyleClass().add("canvas-background");
        pagePane.getStyleClass().add("page-shadow");
    }

    private void setupViewModes() {
        btnDesign.setOnAction(e -> showDesign());
        btnSource.setOnAction(e -> showSource());
        btnPreview.setOnAction(e -> showPreview());
    }

    // Switch to Design Mode
    private void showDesign() {
        if (!sourceEditor.isVisible())
            return; // Already in design or preview?

        try {
            String xml = sourceEditor.getText();
            JrxmlService service = new JrxmlService();
            JasperDesignModel model = service.loadFromString(xml);
            engine.setDesign(model);

            // UI Switch
            sourceEditor.setVisible(false);
            sourceEditor.setManaged(false);
            workspaceArea.setVisible(true);
            workspaceArea.setManaged(true);
            previewContainer.setVisible(false);
            previewContainer.setManaged(false);

            setCenter(internalScrollPane);
            setLeft(vRuler);
            // setTop(topBox); // Ruler/Tools

            // Refresh
            updateWorkspaceSize();

            btnDesign.setDisable(true);
            btnSource.setDisable(false);
            btnPreview.setDisable(false);

        } catch (Exception ex) {
            engine.logError("Failed to switch to Design", ex);
            // Optionally show alert
        }
    }

    // Switch to Source Mode
    private void showSource() {
        if (sourceEditor.isVisible())
            return;

        if (engine.getDesign() != null) {
            try {
                JrxmlService service = new JrxmlService();
                String xml = service.saveToString(engine.getDesign());
                sourceEditor.setText(xml);

                // UI Switch
                workspaceArea.setVisible(false);
                workspaceArea.setManaged(false);
                previewContainer.setVisible(false);
                previewContainer.setManaged(false);

                sourceEditor.setVisible(true);
                sourceEditor.setManaged(true);

                setCenter(sourceEditor);
                setLeft(null);
                // setTop(null);

                btnSource.setDisable(true);
                btnDesign.setDisable(false);
                btnPreview.setDisable(false);

            } catch (Exception ex) {
                engine.logError("Failed to generate Source", ex);
            }
        }
    }

    // Switch to Preview Mode (Updated logic)
    private void showPreview() {
        if (previewContainer.isVisible())
            return;

        // Ensure we capture latest from Source if coming from Source?
        // Or assume Source -> Design -> Preview flow?
        // Let's assume if in Source mode, parse first?
        if (sourceEditor.isVisible()) {
            try {
                String xml = sourceEditor.getText();
                JrxmlService service = new JrxmlService();
                JasperDesignModel model = service.loadFromString(xml);
                engine.setDesign(model);
            } catch (Exception e) {
                engine.logError("Cannot preview: Invalid Source", e);
                return;
            }
        }

        if (engine.getDesign() == null)
            return;

        // UI Switch
        workspaceArea.setVisible(false);
        workspaceArea.setManaged(false);
        sourceEditor.setVisible(false);
        sourceEditor.setManaged(false);

        previewContainer.setVisible(true);
        previewContainer.setManaged(true);

        setCenter(previewContainer);
        setLeft(null);
        // setTop(null);

        btnPreview.setDisable(true);
        btnSource.setDisable(false);
        btnDesign.setDisable(false);

        // Run Preview Logic (Async)
        new Thread(() -> {
            try {
                JasperDesign jd = engine.getDesign().getDesign();
                JasperReport jr = JasperCompileManager.compileReport(jd);
                JasperPrint jp = JasperFillManager.fillReport(jr, new HashMap<>(), new JREmptyDataSource());

                Image fxImage = null;
                if (!jp.getPages().isEmpty()) {
                    BufferedImage bim = (BufferedImage) JasperPrintManager.printPageToImage(jp, 0, 2.0f);
                    fxImage = SwingFXUtils.toFXImage(bim, null);
                }

                final Image fimg = fxImage;
                Platform.runLater(() -> previewImage.setImage(fimg));

            } catch (Exception ex) {
                engine.logError("Preview Generation Failed", ex);
            }
        }).start();
    }

    private void updateWorkspaceSize() {
        double zoom = engine.zoomFactorProperty().get();
        double w = pagePane.getWidth() * zoom;
        double h = pagePane.getHeight() * zoom;

        // Add some padding/margin to the "scrollable" area so it looks nice
        double padding = 10 * zoom;

        workspaceArea.setMinWidth(w + padding);
        workspaceArea.setMinHeight(h + padding);
    }

    private void updateRulers() {
        if (!engine.showRulersProperty().get())
            return;

        double viewportW = internalScrollPane.getViewportBounds().getWidth();
        double hVal = internalScrollPane.getHvalue();

        // With fitToWidth, workspaceArea width is either ViewportW or MinWidth.
        double contentW = workspaceArea.getWidth();

        // Scroll offset
        // The scrollable range is (contentW - viewportW).
        // scrollX is the pixel amount hidden to the left.
        double scrollX = (contentW - viewportW) * hVal;

        double viewportH = internalScrollPane.getViewportBounds().getHeight();
        double contentH = workspaceArea.getHeight();
        double scrollY = (contentH - viewportH) * internalScrollPane.getVvalue();

        // Page Position in Workspace (Centered)
        double zoom = engine.zoomFactorProperty().get();
        double pageW = pagePane.getWidth() * zoom;
        double pageH = pagePane.getHeight() * zoom;

        // WorkspaceArea aligns children to Center by default.
        // So Page is at (contentW - pageW) / 2
        double pageXInWs = (contentW - pageW) / 2;
        double pageYInWs = (contentH - pageH) / 2;

        hRuler.setOffset(scrollX - pageXInWs);
        vRuler.setOffset(scrollY - pageYInWs);
        hRuler.setZoom(zoom);
        vRuler.setZoom(zoom);
    }

    private void setupListeners() {
        engine.currentDesignProperty().addListener((obs, oldVal, newDesign) -> {
            if (newDesign != null) {
                bindToDesign(newDesign);
            }
        });

        if (engine.getDesign() != null) {
            bindToDesign(engine.getDesign());
        }

        engine.zoomFactorProperty().addListener((obs, oldVal, newZoom) -> {
            // Scale pagePane (inside pageGroup) so pageGroup grows
            pagePane.setScaleX(newZoom.doubleValue());
            pagePane.setScaleY(newZoom.doubleValue());

            // workspace min size updated by listener above
            Platform.runLater(this::updateRulers);
        });

        // Initial Zoom Apply
        double z = engine.zoomFactorProperty().get();
        pagePane.setScaleX(z);
        pagePane.setScaleY(z);
        updateWorkspaceSize();

        engine.snapToGridProperty().addListener((obs, old, newVal) -> redrawGrid());
        engine.gridSizeProperty().addListener((obs, old, newVal) -> redrawGrid());
        engine.showGridProperty().addListener((obs, old, newVal) -> redrawGrid());
        engine.showRulersProperty().addListener((o, old, newVal) -> {
            updateRulers();
            requestLayout();
        });

        // Initial grid draw
        redrawGrid();
    }

    // Grid Visuals
    private final Canvas gridCanvas = new Canvas();

    private void redrawGrid() {
        if (!gridLayer.getChildren().contains(gridCanvas)) {
            gridLayer.getChildren().add(0, gridCanvas);
            gridCanvas.widthProperty().bind(gridLayer.widthProperty());
            gridCanvas.heightProperty().bind(gridLayer.heightProperty());
            gridCanvas.setMouseTransparent(true);

            gridLayer.widthProperty().addListener(o -> drawGridLines());
            gridLayer.heightProperty().addListener(o -> drawGridLines());
        }
        drawGridLines();
    }

    private void drawGridLines() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (!engine.showGridProperty().get()) {
            return;
        }

        double w = gridCanvas.getWidth();
        double h = gridCanvas.getHeight();
        int step = engine.gridSizeProperty().get();
        if (step < 5)
            step = 5;

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (double x = 0; x < w; x += step) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y < h; y += step) {
            gc.strokeLine(0, y, w, y);
        }
    }

    // Adorner Group
    private final Group selectionAdorner = new Group();

    private void updateSelectionVisual(Object selection) {
        adornerLayer.getChildren().clear();

        if (selection instanceof ElementModel model) {
            renderElementSelection(model);
        } else if (selection instanceof BandModel band) {
            renderBandSelection(band);
        }
    }

    // Helper to find node for band
    private Node findNodeForBand(BandModel band) {
        if (contentLayer.getChildren().isEmpty())
            return null;
        if (contentLayer.getChildren().get(0) instanceof VBox container) {
            for (Node n : container.getChildren()) {
                if (band.equals(n.getUserData()))
                    return n;
            }
        }
        return null;
    }

    private void renderBandSelection(BandModel band) {
        Node bandNode = findNodeForBand(band);
        if (bandNode != null) {
            // Coordinate transformation: BandPane -> VBox -> ContentLayer
            // AdornerLayer is sibling to ContentLayer.
            // Both are children of PagePane (StackPane).
            // However, ContentLayer translation might be 0,0.

            // Simplest is using localToScene -> sceneToLocal
            Bounds nodeBounds = bandNode.localToScene(bandNode.getLayoutBounds());
            Bounds adornerBounds = adornerLayer.sceneToLocal(nodeBounds);

            Rectangle selectionRect = new Rectangle();
            selectionRect.setX(adornerBounds.getMinX());
            selectionRect.setY(adornerBounds.getMinY());
            selectionRect.setWidth(adornerBounds.getWidth());
            selectionRect.setHeight(adornerBounds.getHeight());
            selectionRect.setFill(Color.rgb(0, 0, 255, 0.05));
            selectionRect.setStroke(Color.BLUE);
            selectionRect.setStrokeWidth(2);

            adornerLayer.getChildren().add(selectionRect);
        }
    }

    private Node findNodeForModel(ElementModel model) {
        if (contentLayer.getChildren().isEmpty())
            return null;
        if (contentLayer.getChildren().get(0) instanceof VBox container) {
            return findNodeRecursive(container, model);
        }
        return null;
    }

    private Node findNodeRecursive(Parent parent, ElementModel model) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (model.equals(child.getUserData()))
                return child;
            if (child instanceof Parent) {
                Node found = findNodeRecursive((Parent) child, model);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private void renderElement(ElementModel model) {
        renderElement(model, contentLayer, null);
    }

    private void renderElement(ElementModel model, Pane parentContainer, ElementModel parentModel) {
        Node visual = createVisualForElement(model);

        if (visual != null) {
            visual.setUserData(model);
            visual.layoutXProperty().bind(model.xProperty());
            visual.layoutYProperty().bind(model.yProperty());

            if (visual instanceof javafx.scene.control.Control) {
                ((javafx.scene.control.Control) visual).prefWidthProperty().bind(model.widthProperty());
                ((javafx.scene.control.Control) visual).prefHeightProperty().bind(model.heightProperty());
            } else if (visual instanceof Pane) {
                ((Pane) visual).prefWidthProperty().bind(model.widthProperty());
                ((Pane) visual).prefHeightProperty().bind(model.heightProperty());
            }

            makeInteractive(visual, model);
            parentContainer.getChildren().add(visual);
        }
    }

    private Node createVisualForElement(ElementModel model) {
        JRDesignElement jr = model.getElement();

        switch (jr) {
            case JRDesignStaticText jrDesignStaticText -> {
                Label label = new Label(jrDesignStaticText.getText());
                label.setStyle(
                        "-fx-border-color: #ddd; -fx-background-color: transparent; -fx-padding: 2; -fx-alignment: center-left;");
                return label;
            }
            case JRDesignTextField jrDesignTextField -> {
                Label tf = new Label("$F{...}");
                JRExpression minExpr = jrDesignTextField.getExpression();
                if (minExpr != null)
                    tf.setText(minExpr.getText());
                tf.setStyle(
                        "-fx-border-color: #ccc; -fx-background-color: #f0f8ff; -fx-padding: 2; -fx-text-fill: #0066cc;");
                return tf;
            }
            case JRDesignFrame frame -> {
                Pane framePane = new Pane();
                framePane.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: rgba(0,0,0,0.05);");
                setupDropTarget(framePane, model);
                JRElement[] childrenArray = frame.getElements();
                if (childrenArray != null) {
                    for (JRElement child : childrenArray) {
                        if (child instanceof JRDesignElement) {
                            renderElement(new ElementModel((JRDesignElement) child), framePane, model);
                        }
                    }
                }
                return framePane;
            }
            case JRDesignRectangle ignored -> {
                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(1);
                rect.widthProperty().bind(model.widthProperty());
                rect.heightProperty().bind(model.heightProperty());

                // Check if it's a CHART placeholder
                if (jr.getPropertiesMap().containsProperty("com.jasperstudio.component.type") &&
                        "CHART".equals(jr.getPropertiesMap().getProperty("com.jasperstudio.component.type"))) {
                    StackPane chartPlaceholder = new StackPane();
                    chartPlaceholder.setStyle("-fx-background-color: #fce8b2; -fx-border-color: #f1c40f;");
                    chartPlaceholder.getChildren().add(new Label("CHART (Placeholder)"));
                    // Bind size
                    chartPlaceholder.prefWidthProperty().bind(model.widthProperty());
                    chartPlaceholder.prefHeightProperty().bind(model.heightProperty());
                    return chartPlaceholder;
                }
                return rect;
            }
            case JRDesignEllipse ignored -> {
                Ellipse ellipse = new Ellipse();
                ellipse.setFill(Color.WHITE);
                ellipse.setStroke(Color.BLACK);
                ellipse.centerXProperty().bind(model.widthProperty().divide(2));
                ellipse.centerYProperty().bind(model.heightProperty().divide(2));
                ellipse.radiusXProperty().bind(model.widthProperty().divide(2));
                ellipse.radiusYProperty().bind(model.heightProperty().divide(2));
                return ellipse;
            }
            case JRDesignLine ignored -> {
                Line line = new Line();
                line.setStroke(Color.BLACK);
                line.setStartX(0);
                line.setStartY(0);
                line.endXProperty().bind(model.widthProperty());
                line.endYProperty().bind(model.heightProperty());
                return line;
            }
            case JRDesignBreak ignored -> {
                Line brk = new Line();
                brk.setStyle("-fx-stroke: red; -fx-stroke-dash-array: 5 5;");
                brk.setStartX(0);
                brk.setStartY(0);
                brk.endXProperty().bind(model.widthProperty());
                brk.setEndY(0);
                return brk;
            }
            case JRDesignImage ignored -> {
                StackPane imgPlaceholder = new StackPane();
                imgPlaceholder.setStyle("-fx-background-color: #eee; -fx-border-color: #666;");

                String labelText = "IMG";
                // Check if it's a barcode
                if (jr.getPropertiesMap().containsProperty("com.jasperstudio.component.type") &&
                        "BARCODE".equals(jr.getPropertiesMap().getProperty("com.jasperstudio.component.type"))) {
                    labelText = "BARCODE";
                    imgPlaceholder.setStyle("-fx-background-color: #fff; -fx-border-color: #000;");
                }
                imgPlaceholder.getChildren().add(new Label(labelText));
                return imgPlaceholder;
            }
            case JRDesignSubreport ignored -> {
                StackPane subPlaceholder = new StackPane();
                subPlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #666; -fx-border-style: dashed;");
                subPlaceholder.getChildren().add(new Label("SUBREPORT"));
                return subPlaceholder;
            }
            case JRDesignChart ignored -> {
                StackPane chartPlaceholder = new StackPane();
                chartPlaceholder.setStyle("-fx-background-color: #fce8b2; -fx-border-color: #f1c40f;");
                chartPlaceholder.getChildren().add(new Label("CHART (Pie)"));
                return chartPlaceholder;
            }
            case JRDesignCrosstab ignored -> {
                StackPane xtabPlaceholder = new StackPane();
                xtabPlaceholder
                        .setStyle("-fx-background-color: #d1c4e9; -fx-border-color: #673ab7; -fx-border-style: dotted;");
                xtabPlaceholder.getChildren().add(new Label("CROSSTAB"));
                return xtabPlaceholder;
            }
            case null, default -> {
                Rectangle placeholder = new Rectangle();
                placeholder.setStyle("-fx-fill: gray;");
                placeholder.widthProperty().bind(model.widthProperty());
                placeholder.heightProperty().bind(model.heightProperty());
                return placeholder;
            }
        }
    }

    private void bindToDesign(JasperDesignModel design) {
        pagePane.prefWidthProperty().bind(design.pageWidthProperty());
        pagePane.prefHeightProperty().bind(design.pageHeightProperty());

        // We will now use a VBox to stack bands inside the page
        // But we need layers (grid, content, adorner) to overlay properly.
        // So:
        // PagePane (StackPane)
        // -> GridLayer (Pane)
        // -> ContentLayer (VBox now? No, content layer needs to be absolute for drags
        // across bands?
        // Actually, standard behavior is elements belong to a band.
        // So, we create a VBox of BandViews.

        // Let's restructure:
        // contentLayer will contain a VBox.
        VBox bandsContainer = new VBox();
        bandsContainer.setPrefWidth(design.getPageWidth());

        // Bind VBox width
        design.pageWidthProperty().addListener((o, old, v) -> bandsContainer.setPrefWidth(v.doubleValue()));

        contentLayer.getChildren().clear();
        contentLayer.getChildren().add(bandsContainer);

        // Margins
        Rectangle marginRect = new Rectangle();
        marginRect.setStyle("-fx-stroke: #ccc; -fx-stroke-dash-array: 5 5; -fx-fill: transparent;");
        marginRect.setMouseTransparent(true);
        marginRect.xProperty().bind(design.leftMarginProperty());
        marginRect.yProperty().bind(design.topMarginProperty());
        marginRect.widthProperty().bind(design.pageWidthProperty().subtract(design.leftMarginProperty())
                .subtract(design.rightMarginProperty()));
        marginRect.heightProperty().bind(design.pageHeightProperty().subtract(design.topMarginProperty())
                .subtract(design.bottomMarginProperty()));

        gridLayer.getChildren().removeIf(n -> n instanceof Rectangle);
        gridLayer.getChildren().add(marginRect);

        // Render Bands
        for (BandModel band : design.getBands()) {
            renderBand(band, bandsContainer);
        }

        // Listen for band changes (e.g. optional bands added/removed via sync)
        design.getBands().addListener((ListChangeListener<BandModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (BandModel rem : c.getRemoved()) {
                        bandsContainer.getChildren().removeIf(n -> n.getUserData() == rem);
                    }
                }
                if (c.wasAdded()) {
                    // For sync() pattern (clear/add-all), append is fine.
                    // We rely on the order of additions matching the desired display order.
                    for (BandModel am : c.getAddedSubList()) {
                        renderBand(am, bandsContainer);
                    }
                }
            }
        });

        // Listen for new bands? (Usually fixed structure, but if we add dynamic bands
        // later)

        engine.selectionProperty().addListener((obs, oldVal, newVal) -> updateSelectionVisual(newVal));

        pagePane.setOnMousePressed(e -> engine.clearSelection());

        // Drag Handler on Page (redirect to bands?)
        // For now, simple logic
    }

    private void renderBand(BandModel band, VBox container) {
        Pane bandPane = new Pane();
        bandPane.setUserData(band);
        bandPane.getStyleClass().add("band-pane");
        bandPane.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
        bandPane.setPrefWidth(container.getPrefWidth());
        bandPane.setPrefHeight(band.getHeight());

        // Bind height
        band.heightProperty().addListener((o, old, v) -> bandPane.setPrefHeight(v.doubleValue()));
        container.prefWidthProperty().addListener((o, old, v) -> bandPane.setPrefWidth(v.doubleValue()));

        // Label for Band Name
        Label bandLabel = new Label(band.getType());
        bandLabel.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-background-color: rgba(255,255,255,0.7); -fx-padding: 2 5; -fx-background-radius: 4;");

        // Center the label
        // We need to wait for label width to be known, or use bindings
        bandLabel.layoutXProperty().bind(bandPane.widthProperty().subtract(bandLabel.widthProperty()).divide(2));
        bandLabel.layoutYProperty().bind(bandPane.heightProperty().subtract(bandLabel.heightProperty()).divide(2));

        bandPane.getChildren().add(bandLabel);

        container.getChildren().add(bandPane);

        // Render Elements in this Band
        for (ElementModel em : band.getElements()) {
            renderElement(em, bandPane, null);
        }

        band.getElements()
                .addListener((ListChangeListener<ElementModel>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (ElementModel em : c.getAddedSubList()) {
                                renderElement(em, bandPane, null);
                            }
                        }
                        if (c.wasRemoved()) {
                            for (ElementModel em : c.getRemoved()) {
                                bandPane.getChildren().removeIf(n -> n.getUserData() == em);
                            }
                        }
                    }
                });

        // Drop into this band
        setupDropTarget(bandPane, band);
    }

    private void setupDropTarget(Node target, BandModel bandModel) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        target.setOnDragDropped(event -> {
            var db = event.getDragboard();
            if (db.hasString()) {
                logger.debug("Dropped '{}' onto band", db.getString());
                try {
                    engine.handleDrop(db.getString(), event.getX(), event.getY(), bandModel);
                    event.setDropCompleted(true);
                } catch (Exception ex) {
                    logger.error("Drop failed", ex);
                    event.setDropCompleted(false);
                }
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private void setupDropTarget(Node target, ElementModel containerModel) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        target.setOnDragDropped(event -> {
            var db = event.getDragboard();
            if (db.hasString()) {
                engine.handleDrop(db.getString(), event.getX(), event.getY(), containerModel);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private double snap(double value) {
        if (engine.snapToGridProperty().get()) {
            int grid = engine.gridSizeProperty().get();
            return Math.round(value / grid) * grid;
        }
        return value;
    }

    private void makeInteractive(Node node, ElementModel model) {
        node.setOnMousePressed(e -> {
            // Only select if not already selected (optimization)
            if (engine.getSelection() != model) {
                engine.setSelection(model);
            }
            e.consume();
            final double startX = e.getSceneX();
            final double startY = e.getSceneY();
            // Capture start state
            final int initialX = (int) model.getX();
            final int initialY = (int) model.getY();
            // Capture zoom at start of drag
            final double zoom = engine.zoomFactorProperty().get();

            node.setOnMouseDragged(dragEvent -> {
                // Adjust delta by zoom factor
                double deltaX = (dragEvent.getSceneX() - startX) / zoom;
                double deltaY = (dragEvent.getSceneY() - startY) / zoom;

                double rawX = initialX + deltaX;
                double rawY = initialY + deltaY;

                model.setX((int) snap(rawX));
                model.setY((int) snap(rawY));
                dragEvent.consume();
            });

            node.setOnMouseReleased(eRelease -> {
                boolean bandChanged = false;

                // Find band under mouse to support moving across bands
                handleElementDrop(node, model, eRelease, initialX, initialY);
            });
        });
    }

    private void handleElementDrop(Node node, ElementModel model, MouseEvent eRelease, int initialX, int initialY) {
        boolean bandChanged = false;
        Point2D scenePoint = new Point2D(eRelease.getSceneX(), eRelease.getSceneY());

        if (!contentLayer.getChildren().isEmpty() && contentLayer.getChildren().get(0) instanceof VBox container) {
            for (Node bandNode : container.getChildren()) {
                Bounds bounds = bandNode.localToScene(bandNode.getBoundsInLocal());
                if (bounds.contains(scenePoint)) {
                    // Found target band
                    if (bandNode.getUserData() instanceof BandModel targetBand) {

                        // Check if we are staying in the same band
                        if (targetBand.getElements().contains(model)) {
                            if (initialX != model.getX() || initialY != model.getY()) {
                                MoveElementCommand cmd = new MoveElementCommand(
                                        model, initialX, initialY, (int) model.getX(), (int) model.getY());
                                engine.executeCommand(cmd);
                            }
                        } else {
                            // Changed band
                            Point2D nodeScene = node.localToScene(0, 0);
                            Point2D nodeInBand = bandNode.sceneToLocal(nodeScene);

                            int newRelY = (int) nodeInBand.getY();
                            if (newRelY < 0) newRelY = 0;

                            engine.moveElementToBand(model, targetBand, newRelY);
                        }
                        bandChanged = true;
                    }
                    break;
                }
            }
        }

        if (!bandChanged) {
            // Fallback: If X/Y changed, commit it as move.
            if (initialX != model.getX() || initialY != model.getY()) {
                MoveElementCommand cmd = new MoveElementCommand(
                        model, initialX, initialY, (int) model.getX(), (int) model.getY());
                engine.executeCommand(cmd);
            }
        }
    }

    private void renderElementSelection(ElementModel model) {
        Node node = findNodeForModel(model);
        if (node == null)
            return;

        selectionAdorner.getChildren().clear();

        Rectangle border = new Rectangle();
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.BLUE);
        border.setStrokeWidth(1);
        border.getStrokeDashArray().addAll(5.0, 5.0);
        border.setMouseTransparent(true);

        selectionAdorner.getChildren().add(border);

        Runnable updateBounds = () -> {
            if (node.getScene() == null)
                return;
            Bounds nodeBounds = node.localToScene(node.getLayoutBounds());
            Bounds adornerBounds = adornerLayer.sceneToLocal(nodeBounds);
            if (adornerBounds != null) {
                border.setX(adornerBounds.getMinX());
                border.setY(adornerBounds.getMinY());
                border.setWidth(adornerBounds.getWidth());
                border.setHeight(adornerBounds.getHeight());
            }
        };

        // Update initial
        Platform.runLater(updateBounds);

        // Listeners
        ChangeListener<Number> changeListener = (o, old, v) -> Platform.runLater(updateBounds);
        model.xProperty().addListener(changeListener);
        model.yProperty().addListener(changeListener);
        model.widthProperty().addListener(changeListener);
        model.heightProperty().addListener(changeListener);
        // Also listen to zoom changes as they affect scene coords
        engine.zoomFactorProperty().addListener(changeListener);

        // Resize Handles
        makeHandle(model, border, Pos.TOP_LEFT);
        makeHandle(model, border, Pos.TOP_CENTER);
        makeHandle(model, border, Pos.TOP_RIGHT);
        makeHandle(model, border, Pos.CENTER_LEFT);
        makeHandle(model, border, Pos.CENTER_RIGHT);
        makeHandle(model, border, Pos.BOTTOM_LEFT);
        makeHandle(model, border, Pos.BOTTOM_CENTER);
        makeHandle(model, border, Pos.BOTTOM_RIGHT);

        adornerLayer.getChildren().add(selectionAdorner);
    }

    private void makeHandle(ElementModel model, Rectangle border, Pos pos) {
        double msgSize = 8; // Increased size for better hit target
        Rectangle handle = new Rectangle(msgSize, msgSize);
        handle.setStyle("-fx-fill: white; -fx-stroke: #0096C9; -fx-stroke-width: 1;");

        // Ensure handle is always on top and catches events
        handle.setViewOrder(-1);

        // Bind handle position to border
        handle.xProperty().bind(Bindings.createDoubleBinding(() -> switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> border.getX() - msgSize / 2;
            case TOP_CENTER, BOTTOM_CENTER -> border.getX() + border.getWidth() / 2 - msgSize / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> border.getX() + border.getWidth() - msgSize / 2;
            default -> 0.0;
        }, border.xProperty(), border.widthProperty()));

        handle.yProperty().bind(Bindings.createDoubleBinding(() -> switch (pos) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> border.getY() - msgSize / 2;
            case CENTER_LEFT, CENTER_RIGHT -> border.getY() + border.getHeight() / 2 - msgSize / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> border.getY() + border.getHeight() - msgSize / 2;
            default -> 0.0;
        }, border.yProperty(), border.heightProperty()));

        // Cursor
        Cursor cursor = switch (pos) {
            case TOP_LEFT -> Cursor.NW_RESIZE;
            case TOP_CENTER -> Cursor.N_RESIZE;
            case TOP_RIGHT -> Cursor.NE_RESIZE;
            case CENTER_RIGHT -> Cursor.E_RESIZE;
            case BOTTOM_RIGHT -> Cursor.SE_RESIZE;
            case BOTTOM_CENTER -> Cursor.S_RESIZE;
            case BOTTOM_LEFT -> Cursor.SW_RESIZE;
            case CENTER_LEFT -> Cursor.W_RESIZE;
            default -> Cursor.DEFAULT;
        };
        handle.setCursor(cursor);

        // Force cursor update on hover
        handle.setOnMouseEntered(e -> handle.setCursor(cursor));

        handle.setOnMousePressed(e -> {
            e.consume();
            final double startX = e.getSceneX();
            final double startY = e.getSceneY();
            final double initialX = model.getX();
            final double initialY = model.getY();
            final double initialW = model.getWidth();
            final double initialH = model.getHeight();
            final double zoom = engine.zoomFactorProperty().get();

            handle.setOnMouseDragged(dragEvent -> {
                double deltaX = (dragEvent.getSceneX() - startX) / zoom;
                double deltaY = (dragEvent.getSceneY() - startY) / zoom;

                double newX = initialX;
                double newY = initialY;
                double newW = initialW;
                double newH = initialH;

                switch (pos) {
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        double attemptX = snap(initialX + deltaX);
                        double diffX = attemptX - initialX;
                        // Adjust width inverse to movement
                        if (initialW - diffX >= 10) {
                            newX = attemptX;
                            newW = initialW - diffX;
                        }
                        break;
                    case TOP_RIGHT:
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                        double attemptW = snap(initialW + deltaX);
                        if (attemptW >= 10)
                            newW = attemptW;
                        break;
                }

                switch (pos) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT:
                        double attemptY = snap(initialY + deltaY);
                        double diffY = attemptY - initialY;
                        if (initialH - diffY >= 10) {
                            newY = attemptY;
                            newH = initialH - diffY;
                        }
                        break;
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        double attemptH = snap(initialH + deltaY);
                        if (attemptH >= 10)
                            newH = attemptH;
                        break;
                }

                if (newX != model.getX())
                    model.setX((int) newX);
                if (newY != model.getY())
                    model.setY((int) newY);
                if (newW != model.getWidth())
                    model.setWidth((int) newW);
                if (newH != model.getHeight())
                    model.setHeight((int) newH);

                dragEvent.consume();
            });

            handle.setOnMouseReleased(relEvent -> {
                int endX = (int) model.getX();
                int endY = (int) model.getY();
                int endW = (int) model.getWidth();
                int endH = (int) model.getHeight();

                if (initialX != endX || initialY != endY || initialW != endW || initialH != endH) {
                    ResizeElementCommand cmd = new ResizeElementCommand(
                            model, (int)initialX, (int)initialY, (int)initialW, (int)initialH, endX, endY, endW, endH);
                    engine.executeCommand(cmd);
                }

                handle.setOnMouseDragged(null);
                handle.setOnMouseReleased(null);
                relEvent.consume();
            });
        });

        selectionAdorner.getChildren().add(handle);
    }
}

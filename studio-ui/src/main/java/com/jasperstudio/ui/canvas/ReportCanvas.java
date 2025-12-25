package com.jasperstudio.ui.canvas;

import com.jasperstudio.designer.DesignerEngine;
import com.jasperstudio.model.JasperDesignModel;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The visualization of the Report Page.
 */
public class ReportCanvas extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(ReportCanvas.class);

    private final DesignerEngine engine;

    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane workspaceArea;
    @javafx.fxml.FXML
    private javafx.scene.Group pageGroup;
    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane pagePane;
    @javafx.fxml.FXML
    private javafx.scene.layout.Pane gridLayer;
    @javafx.fxml.FXML
    private javafx.scene.layout.Pane contentLayer;
    @javafx.fxml.FXML
    private javafx.scene.layout.Pane adornerLayer;
    @javafx.fxml.FXML
    private RulerControl hRuler;
    @javafx.fxml.FXML
    private RulerControl vRuler;
    @javafx.fxml.FXML
    private ScrollPane internalScrollPane;

    @javafx.fxml.FXML
    private javafx.scene.control.Button btnDesign;
    @javafx.fxml.FXML
    private javafx.scene.control.Button btnSource;
    @javafx.fxml.FXML
    private javafx.scene.control.Button btnPreview;

    @javafx.fxml.FXML
    private javafx.scene.control.TextArea sourceEditor;
    @javafx.fxml.FXML
    private ScrollPane previewContainer;
    @javafx.fxml.FXML
    private javafx.scene.image.ImageView previewImage;
    @javafx.fxml.FXML
    private javafx.scene.layout.HBox topBox;

    public ReportCanvas(DesignerEngine engine) {
        this.engine = engine;
        loadFXML();
        initUI(); // Post-load init
        setupListeners();
        setupViewModes();
    }

    private void loadFXML() {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ReportCanvas.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
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
        javafx.application.Platform.runLater(() -> {
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

        javafx.beans.value.ChangeListener<Number> sizeListener = (obs, old, val) -> updateWorkspaceSize();
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
            com.jasperstudio.model.JrxmlService service = new com.jasperstudio.model.JrxmlService();
            com.jasperstudio.model.JasperDesignModel model = service.loadFromString(xml);
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
                com.jasperstudio.model.JrxmlService service = new com.jasperstudio.model.JrxmlService();
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
                com.jasperstudio.model.JrxmlService service = new com.jasperstudio.model.JrxmlService();
                com.jasperstudio.model.JasperDesignModel model = service.loadFromString(xml);
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
                net.sf.jasperreports.engine.design.JasperDesign jd = engine.getDesign().getDesign();
                net.sf.jasperreports.engine.JasperReport jr = net.sf.jasperreports.engine.JasperCompileManager
                        .compileReport(jd);
                net.sf.jasperreports.engine.JasperPrint jp = net.sf.jasperreports.engine.JasperFillManager
                        .fillReport(jr, new java.util.HashMap<>(), new net.sf.jasperreports.engine.JREmptyDataSource());

                javafx.scene.image.Image fxImage = null;
                if (!jp.getPages().isEmpty()) {
                    java.awt.image.BufferedImage bim = (java.awt.image.BufferedImage) net.sf.jasperreports.engine.JasperPrintManager
                            .printPageToImage(jp, 0, 2.0f);
                    fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(bim, null);
                }

                final javafx.scene.image.Image fimg = fxImage;
                javafx.application.Platform.runLater(() -> previewImage.setImage(fimg));

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
            javafx.application.Platform.runLater(this::updateRulers);
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
    private final javafx.scene.canvas.Canvas gridCanvas = new javafx.scene.canvas.Canvas();

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
        var gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (!engine.showGridProperty().get()) {
            return;
        }

        double w = gridCanvas.getWidth();
        double h = gridCanvas.getHeight();
        int step = engine.gridSizeProperty().get();
        if (step < 5)
            step = 5;

        gc.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
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

    private void updateSelectionVisual(com.jasperstudio.model.ElementModel model) {
        adornerLayer.getChildren().remove(selectionAdorner);
        selectionAdorner.getChildren().clear();

        if (model != null) {
            javafx.scene.Node visualNode = findNodeForModel(contentLayer, model);

            var border = new javafx.scene.shape.Rectangle();
            border.setStyle(
                    "-fx-fill: transparent; -fx-stroke: #0096C9; -fx-stroke-width: 1; -fx-stroke-dash-array: 2 2;");
            border.setMouseTransparent(true);

            if (visualNode != null) {
                Runnable updateBounds = () -> {
                    if (visualNode.getScene() == null)
                        return;
                    javafx.geometry.Bounds bounds = visualNode.localToScene(visualNode.getBoundsInLocal());
                    javafx.geometry.Bounds adornerBounds = adornerLayer.sceneToLocal(bounds);
                    if (adornerBounds != null) {
                        border.setX(adornerBounds.getMinX());
                        border.setY(adornerBounds.getMinY());
                        border.setWidth(adornerBounds.getWidth());
                        border.setHeight(adornerBounds.getHeight());
                    }
                };

                javafx.application.Platform.runLater(updateBounds);
                model.xProperty().addListener(o -> javafx.application.Platform.runLater(updateBounds));
                model.yProperty().addListener(o -> javafx.application.Platform.runLater(updateBounds));
                model.widthProperty().addListener(o -> javafx.application.Platform.runLater(updateBounds));
                model.heightProperty().addListener(o -> javafx.application.Platform.runLater(updateBounds));

                selectionAdorner.getChildren().add(border);

                createHandle(model, border, javafx.geometry.Pos.TOP_LEFT);
                createHandle(model, border, javafx.geometry.Pos.TOP_CENTER);
                createHandle(model, border, javafx.geometry.Pos.TOP_RIGHT);
                createHandle(model, border, javafx.geometry.Pos.CENTER_RIGHT);
                createHandle(model, border, javafx.geometry.Pos.BOTTOM_RIGHT);
                createHandle(model, border, javafx.geometry.Pos.BOTTOM_CENTER);
                createHandle(model, border, javafx.geometry.Pos.BOTTOM_LEFT);
                createHandle(model, border, javafx.geometry.Pos.CENTER_LEFT);

            } else {
                border.xProperty().bind(model.xProperty());
                border.yProperty().bind(model.yProperty());
                border.widthProperty().bind(model.widthProperty());
                border.heightProperty().bind(model.heightProperty());
                selectionAdorner.getChildren().add(border);

                createHandle(model, null, javafx.geometry.Pos.TOP_LEFT);
                createHandle(model, null, javafx.geometry.Pos.TOP_CENTER);
                createHandle(model, null, javafx.geometry.Pos.TOP_RIGHT);
                createHandle(model, null, javafx.geometry.Pos.CENTER_RIGHT);
                createHandle(model, null, javafx.geometry.Pos.BOTTOM_RIGHT);
                createHandle(model, null, javafx.geometry.Pos.BOTTOM_CENTER);
                createHandle(model, null, javafx.geometry.Pos.BOTTOM_LEFT);
                createHandle(model, null, javafx.geometry.Pos.CENTER_LEFT);
            }

            adornerLayer.getChildren().add(selectionAdorner);
        }
    }

    private javafx.scene.Node findNodeForModel(javafx.scene.Parent root, com.jasperstudio.model.ElementModel model) {
        for (javafx.scene.Node node : root.getChildrenUnmodifiable()) {
            if (node.getUserData() != null) {
                if (node.getUserData() instanceof com.jasperstudio.model.ElementModel) {
                    com.jasperstudio.model.ElementModel em = (com.jasperstudio.model.ElementModel) node.getUserData();
                    if (em.getElement() == model.getElement()) {
                        return node;
                    }
                }
            }
            if (node instanceof javafx.scene.Parent) {
                javafx.scene.Node found = findNodeForModel((javafx.scene.Parent) node, model);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private void createHandle(com.jasperstudio.model.ElementModel model, javafx.scene.shape.Rectangle linkedBorder,
            javafx.geometry.Pos pos) {
        double msgSize = 6;
        var handle = new javafx.scene.shape.Rectangle(msgSize, msgSize);
        handle.setStyle("-fx-fill: white; -fx-stroke: #0096C9; -fx-stroke-width: 1;");

        if (linkedBorder != null) {
            handle.layoutXProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                switch (pos) {
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        return linkedBorder.getX() - msgSize / 2;
                    case TOP_CENTER:
                    case BOTTOM_CENTER:
                        return linkedBorder.getX() + linkedBorder.getWidth() / 2 - msgSize / 2;
                    case TOP_RIGHT:
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                        return linkedBorder.getX() + linkedBorder.getWidth() - msgSize / 2;
                    default:
                        return 0.0;
                }
            }, linkedBorder.xProperty(), linkedBorder.widthProperty()));

            handle.layoutYProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                switch (pos) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT:
                        return linkedBorder.getY() - msgSize / 2;
                    case CENTER_LEFT:
                    case CENTER_RIGHT:
                        return linkedBorder.getY() + linkedBorder.getHeight() / 2 - msgSize / 2;
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        return linkedBorder.getY() + linkedBorder.getHeight() - msgSize / 2;
                    default:
                        return 0.0;
                }
            }, linkedBorder.yProperty(), linkedBorder.heightProperty()));
        } else {
            handle.layoutXProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                switch (pos) {
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        return model.getX() - msgSize / 2;
                    case TOP_CENTER:
                    case BOTTOM_CENTER:
                        return model.getX() + model.getWidth() / 2 - msgSize / 2;
                    case TOP_RIGHT:
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                        return model.getX() + model.getWidth() - msgSize / 2;
                    default:
                        return 0.0;
                }
            }, model.xProperty(), model.widthProperty()));

            handle.layoutYProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                switch (pos) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT:
                        return model.getY() - msgSize / 2;
                    case CENTER_LEFT:
                    case CENTER_RIGHT:
                        return model.getY() + model.getHeight() / 2 - msgSize / 2;
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        return model.getY() + model.getHeight() - msgSize / 2;
                    default:
                        return 0.0;
                }
            }, model.yProperty(), model.heightProperty()));
        }

        switch (pos) {
            case TOP_LEFT:
                handle.setCursor(javafx.scene.Cursor.NW_RESIZE);
                break;
            case TOP_CENTER:
                handle.setCursor(javafx.scene.Cursor.N_RESIZE);
                break;
            case TOP_RIGHT:
                handle.setCursor(javafx.scene.Cursor.NE_RESIZE);
                break;
            case CENTER_RIGHT:
                handle.setCursor(javafx.scene.Cursor.E_RESIZE);
                break;
            case BOTTOM_RIGHT:
                handle.setCursor(javafx.scene.Cursor.SE_RESIZE);
                break;
            case BOTTOM_CENTER:
                handle.setCursor(javafx.scene.Cursor.S_RESIZE);
                break;
            case BOTTOM_LEFT:
                handle.setCursor(javafx.scene.Cursor.SW_RESIZE);
                break;
            case CENTER_LEFT:
                handle.setCursor(javafx.scene.Cursor.W_RESIZE);
                break;
            default:
                handle.setCursor(javafx.scene.Cursor.DEFAULT);
        }

        handle.setOnMousePressed(e -> {
            e.consume();
            final double startX = e.getSceneX();
            final double startY = e.getSceneY();
            final int startMX = (int) model.getX();
            final int startMY = (int) model.getY();
            final int startW = (int) model.getWidth();
            final int startH = (int) model.getHeight();

            handle.setOnMouseDragged(dragEvent -> {
                double deltaX = dragEvent.getSceneX() - startX;
                double deltaY = dragEvent.getSceneY() - startY;

                double newX = startMX, newY = startMY, newW = startW, newH = startH;

                switch (pos) {
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        double snX = snap(startMX + deltaX);
                        newX = snX;
                        newW -= (snX - startMX);
                        break;
                    case TOP_RIGHT:
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                        newW = snap(startW + deltaX);
                        break;
                    default:
                        break;
                }

                switch (pos) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT:
                        double snY = snap(startMY + deltaY);
                        newY = snY;
                        newH -= (snY - startMY);
                        break;
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        newH = snap(startH + deltaY);
                        break;
                    default:
                        break;
                }

                if (newW < 10)
                    newW = 10;
                if (newH < 10)
                    newH = 10;

                model.setX((int) newX);
                model.setY((int) newY);
                model.setWidth((int) newW);
                model.setHeight((int) newH);

                dragEvent.consume();
            });

            handle.setOnMouseReleased(relEvent -> {
                // Determine final state
                int endX = (int) model.getX();
                int endY = (int) model.getY();
                int endW = (int) model.getWidth();
                int endH = (int) model.getHeight();

                if (startMX != endX || startMY != endY || startW != endW || startH != endH) {
                    com.jasperstudio.descriptor.ResizeElementCommand cmd = new com.jasperstudio.descriptor.ResizeElementCommand(
                            model, startMX, startMY, startW, startH, endX, endY, endW, endH);
                    engine.executeCommand(cmd); // Pushes to stack (and redundant execute)
                }

                // Unbind listeners to prevent leaks/conflicts?
                // In standard JavaFX DnD inside IsPressed, it handles itself usually, but
                // explicitly clearing is safer.
                handle.setOnMouseDragged(null);
                handle.setOnMouseReleased(null);
                relEvent.consume();
            });
        });

        selectionAdorner.getChildren().add(handle);
    }

    private void updateSelectionVisual(Object selection) {
        adornerLayer.getChildren().clear();

        if (selection instanceof com.jasperstudio.model.ElementModel) {
            com.jasperstudio.model.ElementModel model = (com.jasperstudio.model.ElementModel) selection;
            renderElementSelection(model);
        } else if (selection instanceof com.jasperstudio.model.BandModel) {
            com.jasperstudio.model.BandModel band = (com.jasperstudio.model.BandModel) selection;
            renderBandSelection(band);
        }
    }

    // Helper to find node for band
    private javafx.scene.Node findNodeForBand(com.jasperstudio.model.BandModel band) {
        if (contentLayer.getChildren().isEmpty())
            return null;
        if (contentLayer.getChildren().get(0) instanceof javafx.scene.layout.VBox) {
            javafx.scene.layout.VBox container = (javafx.scene.layout.VBox) contentLayer.getChildren().get(0);
            for (javafx.scene.Node n : container.getChildren()) {
                if (band.equals(n.getUserData()))
                    return n;
            }
        }
        return null;
    }

    private void renderBandSelection(com.jasperstudio.model.BandModel band) {
        javafx.scene.Node bandNode = findNodeForBand(band);
        if (bandNode != null) {
            // Coordinate transformation: BandPane -> VBox -> ContentLayer
            // AdornerLayer is sibling to ContentLayer.
            // Both are children of PagePane (StackPane).
            // However, ContentLayer translation might be 0,0.

            // Simplest is using localToScene -> sceneToLocal
            javafx.geometry.Bounds nodeBounds = bandNode.localToScene(bandNode.getLayoutBounds());
            javafx.geometry.Bounds adornerBounds = adornerLayer.sceneToLocal(nodeBounds);

            Rectangle selectionRect = new Rectangle();
            selectionRect.setX(adornerBounds.getMinX());
            selectionRect.setY(adornerBounds.getMinY());
            selectionRect.setWidth(adornerBounds.getWidth());
            selectionRect.setHeight(adornerBounds.getHeight());
            selectionRect.setFill(javafx.scene.paint.Color.rgb(0, 0, 255, 0.05));
            selectionRect.setStroke(javafx.scene.paint.Color.BLUE);
            selectionRect.setStrokeWidth(2);

            adornerLayer.getChildren().add(selectionRect);
        }
    }

    private javafx.scene.Node findNodeForModel(com.jasperstudio.model.ElementModel model) {
        if (contentLayer.getChildren().isEmpty())
            return null;
        if (contentLayer.getChildren().get(0) instanceof javafx.scene.layout.VBox) {
            javafx.scene.layout.VBox container = (javafx.scene.layout.VBox) contentLayer.getChildren().get(0);
            return findNodeRecursive(container, model);
        }
        return null;
    }

    private javafx.scene.Node findNodeRecursive(javafx.scene.Parent parent, com.jasperstudio.model.ElementModel model) {
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (model.equals(child.getUserData()))
                return child;
            if (child instanceof javafx.scene.Parent) {
                javafx.scene.Node found = findNodeRecursive((javafx.scene.Parent) child, model);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private void renderElement(com.jasperstudio.model.ElementModel model) {
        renderElement(model, contentLayer, null);
    }

    private void renderElement(com.jasperstudio.model.ElementModel model, Pane parentContainer,
            com.jasperstudio.model.ElementModel parentModel) {
        javafx.scene.Node visual = null;
        net.sf.jasperreports.engine.design.JRDesignElement jr = model.getElement();

        if (jr instanceof net.sf.jasperreports.engine.design.JRDesignStaticText) {
            var label = new javafx.scene.control.Label(
                    ((net.sf.jasperreports.engine.design.JRDesignStaticText) jr).getText());
            label.setStyle(
                    "-fx-border-color: #ddd; -fx-background-color: transparent; -fx-padding: 2; -fx-alignment: center-left;");
            visual = label;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignTextField) {
            var tf = new javafx.scene.control.Label("$F{...}");
            var minExpr = ((net.sf.jasperreports.engine.design.JRDesignTextField) jr).getExpression();
            if (minExpr != null)
                tf.setText(minExpr.getText());
            tf.setStyle(
                    "-fx-border-color: #ccc; -fx-background-color: #f0f8ff; -fx-padding: 2; -fx-text-fill: #0066cc;");
            visual = tf;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignFrame) {
            var framePane = new javafx.scene.layout.Pane();
            framePane.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: rgba(0,0,0,0.05);");
            setupDropTarget(framePane, model);
            net.sf.jasperreports.engine.design.JRDesignFrame frame = (net.sf.jasperreports.engine.design.JRDesignFrame) jr;
            net.sf.jasperreports.engine.JRElement[] childrenArray = frame.getElements();
            if (childrenArray != null) {
                for (net.sf.jasperreports.engine.JRElement child : childrenArray) {
                    if (child instanceof net.sf.jasperreports.engine.design.JRDesignElement) {
                        renderElement(new com.jasperstudio.model.ElementModel(
                                (net.sf.jasperreports.engine.design.JRDesignElement) child), framePane, model);
                    }
                }
            }
            visual = framePane;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignRectangle) {
            var rect = new javafx.scene.shape.Rectangle();
            rect.setFill(javafx.scene.paint.Color.WHITE);
            rect.setStroke(javafx.scene.paint.Color.BLACK);
            rect.setStrokeWidth(1);
            rect.widthProperty().bind(model.widthProperty());
            rect.heightProperty().bind(model.heightProperty());
            visual = rect;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignEllipse) {
            var ellipse = new javafx.scene.shape.Ellipse();
            ellipse.setFill(javafx.scene.paint.Color.WHITE);
            ellipse.setStroke(javafx.scene.paint.Color.BLACK);
            ellipse.centerXProperty().bind(model.widthProperty().divide(2));
            ellipse.centerYProperty().bind(model.heightProperty().divide(2));
            ellipse.radiusXProperty().bind(model.widthProperty().divide(2));
            ellipse.radiusYProperty().bind(model.heightProperty().divide(2));
            visual = ellipse;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignLine) {
            var line = new javafx.scene.shape.Line();
            line.setStroke(javafx.scene.paint.Color.BLACK);
            line.setStartX(0);
            line.setStartY(0);
            line.endXProperty().bind(model.widthProperty());
            line.endYProperty().bind(model.heightProperty());
            visual = line;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignBreak) {
            var brk = new javafx.scene.shape.Line();
            brk.setStyle("-fx-stroke: red; -fx-stroke-dash-array: 5 5;");
            brk.setStartX(0);
            brk.setStartY(0);
            brk.endXProperty().bind(model.widthProperty());
            brk.setEndY(0);
            visual = brk;
        } else if (jr instanceof net.sf.jasperreports.engine.design.JRDesignImage) {
            var imgPlaceholder = new javafx.scene.layout.StackPane();
            imgPlaceholder.setStyle("-fx-background-color: #eee; -fx-border-color: #666;");
            imgPlaceholder.getChildren().add(new javafx.scene.control.Label("IMG"));
            visual = imgPlaceholder;
        } else {
            var placeholder = new javafx.scene.shape.Rectangle();
            placeholder.setStyle("-fx-fill: gray;");
            placeholder.widthProperty().bind(model.widthProperty());
            placeholder.heightProperty().bind(model.heightProperty());
            visual = placeholder;
        }

        if (visual != null) {
            visual.setUserData(model);
            visual.layoutXProperty().bind(model.xProperty());
            visual.layoutYProperty().bind(model.yProperty());
            if (visual instanceof javafx.scene.control.Control) {
                ((javafx.scene.control.Control) visual).prefWidthProperty().bind(model.widthProperty());
                ((javafx.scene.control.Control) visual).prefHeightProperty().bind(model.heightProperty());
            } else if (visual instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) visual).prefWidthProperty().bind(model.widthProperty());
                ((javafx.scene.layout.Pane) visual).prefHeightProperty().bind(model.heightProperty());
            }
            makeInteractive(visual, model);
            parentContainer.getChildren().add(visual);
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
        javafx.scene.layout.VBox bandsContainer = new javafx.scene.layout.VBox();
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
        for (com.jasperstudio.model.BandModel band : design.getBands()) {
            renderBand(band, bandsContainer);
        }

        // Listen for band changes (e.g. optional bands added/removed via sync)
        design.getBands().addListener((javafx.collections.ListChangeListener<com.jasperstudio.model.BandModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (com.jasperstudio.model.BandModel rem : c.getRemoved()) {
                        bandsContainer.getChildren().removeIf(n -> n.getUserData() == rem);
                    }
                }
                if (c.wasAdded()) {
                    // For sync() pattern (clear/add-all), append is fine.
                    // We rely on the order of additions matching the desired display order.
                    for (com.jasperstudio.model.BandModel am : c.getAddedSubList()) {
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

    private void renderBand(com.jasperstudio.model.BandModel band, javafx.scene.layout.VBox container) {
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
        javafx.scene.control.Label bandLabel = new javafx.scene.control.Label(band.getType());
        bandLabel.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-background-color: rgba(255,255,255,0.7); -fx-padding: 2 5; -fx-background-radius: 4;");

        // Center the label
        // We need to wait for label width to be known, or use bindings
        bandLabel.layoutXProperty().bind(bandPane.widthProperty().subtract(bandLabel.widthProperty()).divide(2));
        bandLabel.layoutYProperty().bind(bandPane.heightProperty().subtract(bandLabel.heightProperty()).divide(2));

        bandPane.getChildren().add(bandLabel);

        container.getChildren().add(bandPane);

        // Render Elements in this Band
        for (com.jasperstudio.model.ElementModel em : band.getElements()) {
            renderElement(em, bandPane, null);
        }

        band.getElements()
                .addListener((javafx.collections.ListChangeListener<com.jasperstudio.model.ElementModel>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (com.jasperstudio.model.ElementModel em : c.getAddedSubList()) {
                                renderElement(em, bandPane, null);
                            }
                        }
                        if (c.wasRemoved()) {
                            for (com.jasperstudio.model.ElementModel em : c.getRemoved()) {
                                bandPane.getChildren().removeIf(n -> n.getUserData() == em);
                            }
                        }
                    }
                });

        // Drop into this band
        setupDropTarget(bandPane, band);
    }

    private void setupDropTarget(javafx.scene.Node target, com.jasperstudio.model.BandModel bandModel) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
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

    private void setupDropTarget(javafx.scene.Node target, com.jasperstudio.model.ElementModel containerModel) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
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

    private void makeInteractive(javafx.scene.Node node, com.jasperstudio.model.ElementModel model) {
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
                javafx.geometry.Point2D scenePoint = new javafx.geometry.Point2D(eRelease.getSceneX(),
                        eRelease.getSceneY());

                if (contentLayer.getChildren().size() > 0
                        && contentLayer.getChildren().get(0) instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.VBox container = (javafx.scene.layout.VBox) contentLayer.getChildren().get(0);
                    for (javafx.scene.Node bandNode : container.getChildren()) {
                        javafx.geometry.Bounds bounds = bandNode.localToScene(bandNode.getBoundsInLocal());
                        if (bounds.contains(scenePoint)) {
                            // Found target band
                            if (bandNode.getUserData() instanceof com.jasperstudio.model.BandModel) {
                                com.jasperstudio.model.BandModel targetBand = (com.jasperstudio.model.BandModel) bandNode
                                        .getUserData();

                                // Check if we are staying in the same band
                                if (targetBand.getElements().contains(model)) {
                                    // Same band move -> Use Undoable Command

                                    // Calculate Y relative to band (should match model.y if properly synced, but
                                    // let's be precise)
                                    // Actually model.y is relative to band in our model?
                                    // Yes, ElementModel stores relative coordinates.
                                    // During drag we updated model.x/y assuming they are relative to *visual
                                    // parent*, which IS the band pane.
                                    // So model.getX/Y are already correct relative coordinates.

                                    if (initialX != model.getX() || initialY != model.getY()) {
                                        com.jasperstudio.descriptor.MoveElementCommand cmd = new com.jasperstudio.descriptor.MoveElementCommand(
                                                model, initialX, initialY, (int) model.getX(), (int) model.getY());
                                        engine.executeCommand(cmd);
                                    }

                                } else {
                                    // Changed band -> Handled by engine (TODO: Make undoable)
                                    // Calculate Y relative to new band
                                    javafx.geometry.Point2D nodeScene = node.localToScene(0, 0);
                                    javafx.geometry.Point2D nodeInBand = bandNode.sceneToLocal(nodeScene);

                                    int newRelY = (int) nodeInBand.getY();
                                    if (newRelY < 0)
                                        newRelY = 0;

                                    engine.moveElementToBand(model, targetBand, newRelY);
                                }
                                bandChanged = true;
                            }
                            break;
                        }
                    }
                }

                if (!bandChanged) {
                    // Dropped outside any band or logic failed?
                    // If we just moved within current band but 'contains' check failed for some
                    // reason?
                    // Fallback: If X/Y changed, commit it as move.
                    if (initialX != model.getX() || initialY != model.getY()) {
                        com.jasperstudio.descriptor.MoveElementCommand cmd = new com.jasperstudio.descriptor.MoveElementCommand(
                                model, initialX, initialY, (int) model.getX(), (int) model.getY());
                        engine.executeCommand(cmd);
                    }
                }
            });
        });
    }

    private void renderElementSelection(com.jasperstudio.model.ElementModel model) {
        javafx.scene.Node node = findNodeForModel(model);
        if (node == null)
            return;

        selectionAdorner.getChildren().clear();

        Rectangle border = new Rectangle();
        border.setFill(javafx.scene.paint.Color.TRANSPARENT);
        border.setStroke(javafx.scene.paint.Color.BLUE);
        border.setStrokeWidth(1);
        border.getStrokeDashArray().addAll(5.0, 5.0);
        border.setMouseTransparent(true);

        selectionAdorner.getChildren().add(border);

        Runnable updateBounds = () -> {
            if (node.getScene() == null)
                return;
            javafx.geometry.Bounds nodeBounds = node.localToScene(node.getLayoutBounds());
            javafx.geometry.Bounds adornerBounds = adornerLayer.sceneToLocal(nodeBounds);
            if (adornerBounds != null) {
                border.setX(adornerBounds.getMinX());
                border.setY(adornerBounds.getMinY());
                border.setWidth(adornerBounds.getWidth());
                border.setHeight(adornerBounds.getHeight());
            }
        };

        // Update initial
        javafx.application.Platform.runLater(updateBounds);

        // Listeners
        javafx.beans.value.ChangeListener<Number> changeListener = (o, old, v) -> javafx.application.Platform
                .runLater(updateBounds);
        model.xProperty().addListener(changeListener);
        model.yProperty().addListener(changeListener);
        model.widthProperty().addListener(changeListener);
        model.heightProperty().addListener(changeListener);
        // Also listen to zoom changes as they affect scene coords
        engine.zoomFactorProperty().addListener(changeListener);

        // Resize Handles
        makeHandle(model, border, javafx.geometry.Pos.TOP_LEFT);
        makeHandle(model, border, javafx.geometry.Pos.TOP_CENTER);
        makeHandle(model, border, javafx.geometry.Pos.TOP_RIGHT);
        makeHandle(model, border, javafx.geometry.Pos.CENTER_LEFT);
        makeHandle(model, border, javafx.geometry.Pos.CENTER_RIGHT);
        makeHandle(model, border, javafx.geometry.Pos.BOTTOM_LEFT);
        makeHandle(model, border, javafx.geometry.Pos.BOTTOM_CENTER);
        makeHandle(model, border, javafx.geometry.Pos.BOTTOM_RIGHT);

        adornerLayer.getChildren().add(selectionAdorner);
    }

    private void makeHandle(com.jasperstudio.model.ElementModel model, javafx.scene.shape.Rectangle border,
            javafx.geometry.Pos pos) {
        double msgSize = 8; // Increased size for better hit target
        Rectangle handle = new Rectangle(msgSize, msgSize);
        handle.setStyle("-fx-fill: white; -fx-stroke: #0096C9; -fx-stroke-width: 1;");

        // Ensure handle is always on top and catches events
        handle.setViewOrder(-1);

        // Bind handle position to border
        handle.xProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
            switch (pos) {
                case TOP_LEFT:
                case CENTER_LEFT:
                case BOTTOM_LEFT:
                    return border.getX() - msgSize / 2;
                case TOP_CENTER:
                case BOTTOM_CENTER:
                    return border.getX() + border.getWidth() / 2 - msgSize / 2;
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BOTTOM_RIGHT:
                    return border.getX() + border.getWidth() - msgSize / 2;
                default:
                    return 0.0;
            }
        }, border.xProperty(), border.widthProperty()));

        handle.yProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
            switch (pos) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    return border.getY() - msgSize / 2;
                case CENTER_LEFT:
                case CENTER_RIGHT:
                    return border.getY() + border.getHeight() / 2 - msgSize / 2;
                case BOTTOM_LEFT:
                case BOTTOM_CENTER:
                case BOTTOM_RIGHT:
                    return border.getY() + border.getHeight() - msgSize / 2;
                default:
                    return 0.0;
            }
        }, border.yProperty(), border.heightProperty()));

        // Cursor
        javafx.scene.Cursor cursor;
        switch (pos) {
            case TOP_LEFT:
                cursor = javafx.scene.Cursor.NW_RESIZE;
                break;
            case TOP_CENTER:
                cursor = javafx.scene.Cursor.N_RESIZE;
                break;
            case TOP_RIGHT:
                cursor = javafx.scene.Cursor.NE_RESIZE;
                break;
            case CENTER_RIGHT:
                cursor = javafx.scene.Cursor.E_RESIZE;
                break;
            case BOTTOM_RIGHT:
                cursor = javafx.scene.Cursor.SE_RESIZE;
                break;
            case BOTTOM_CENTER:
                cursor = javafx.scene.Cursor.S_RESIZE;
                break;
            case BOTTOM_LEFT:
                cursor = javafx.scene.Cursor.SW_RESIZE;
                break;
            case CENTER_LEFT:
                cursor = javafx.scene.Cursor.W_RESIZE;
                break;
            default:
                cursor = javafx.scene.Cursor.DEFAULT;
        }
        handle.setCursor(cursor);

        // Force cursor update on hover
        handle.setOnMouseEntered(e -> handle.setCursor(cursor));

        handle.setOnMousePressed(e -> {
            e.consume();
            System.out.println("Handle Pressed: " + pos);
            final double startX = e.getSceneX();
            final double startY = e.getSceneY();
            final double initialX = model.getX();
            final double initialY = model.getY();
            final double initialW = model.getWidth();
            final double initialH = model.getHeight();
            final double zoom = engine.zoomFactorProperty().get();

            handle.setOnMouseDragged(dragEvent -> {
                System.out.println("Handle Dragged: " + pos);
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

                System.out.println("Resize: " + newX + "," + newY + " " + newW + "x" + newH);

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
        });

        selectionAdorner.getChildren().add(handle);
    }
}

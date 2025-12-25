package com.jasperstudio.ui;

import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main shell of the application.
 * Replaces Eclipse WorkbenchWindow.
 */
public class MainWorkspace extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(MainWorkspace.class);

    private final com.jasperstudio.designer.DesignerEngine designerEngine;

    @javafx.fxml.FXML
    private javafx.scene.control.SplitPane mainSplit;
    @javafx.fxml.FXML
    private javafx.scene.control.SplitPane leftSidebar;

    @javafx.fxml.FXML
    private javafx.scene.control.TabPane leftUpperTabPane;
    @javafx.fxml.FXML
    private javafx.scene.control.TabPane leftLowerTabPane;

    @javafx.fxml.FXML
    private javafx.scene.control.Tab tabPalette;
    @javafx.fxml.FXML
    private javafx.scene.control.Tab tabData;
    @javafx.fxml.FXML
    private javafx.scene.control.Tab tabOutline;

    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane paletteContainer;
    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane dataContainer;
    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane outlineContainer;

    // Right Sidebar
    @javafx.fxml.FXML
    private javafx.scene.control.SplitPane rightSidebar;
    @javafx.fxml.FXML
    private javafx.scene.control.TabPane rightUpperTabPane;
    @javafx.fxml.FXML
    private javafx.scene.control.TabPane rightLowerTabPane;
    @javafx.fxml.FXML
    private javafx.scene.control.Tab tabProperties;
    @javafx.fxml.FXML
    private javafx.scene.control.Tab tabAssistant;

    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane propertiesContainer;
    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane assistantContainer;

    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane canvasContainer;
    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane logContainer;

    @javafx.fxml.FXML
    private javafx.scene.control.Button btnUndo;
    @javafx.fxml.FXML
    private javafx.scene.control.Button btnRedo;

    // Menu Items
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowPalette;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowData;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowOutline;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowProperties;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowAssistant;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowGrid;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuShowRulers;
    @javafx.fxml.FXML
    private javafx.scene.control.CheckMenuItem menuSnapToGrid;

    @javafx.fxml.FXML
    private javafx.scene.control.RadioMenuItem menuStyleLight;
    @javafx.fxml.FXML
    private javafx.scene.control.RadioMenuItem menuStyleDark;
    @javafx.fxml.FXML
    private javafx.scene.control.RadioMenuItem menuStyleGlass;

    public MainWorkspace() {
        this.designerEngine = new com.jasperstudio.designer.DesignerEngine();
        loadFXML();
        initSubViews();
        setupBindings();
        setupViewActions();
        setupThemeActions(); // Added call
    }

    private void loadFXML() {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("MainWorkspace.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
            logger.error("Failed to load MainWorkspace.fxml", e);
            throw new RuntimeException("Failed to load MainWorkspace.fxml", e);
        }
    }

    private void initSubViews() {
        // Instantiate and inject sub-views
        paletteContainer.getChildren().add(new com.jasperstudio.ui.palette.PaletteView());
        dataContainer.getChildren().add(new com.jasperstudio.ui.datasource.DataSourcePanel());
        outlineContainer.getChildren().add(new com.jasperstudio.ui.outline.OutlinePanel(designerEngine));

        canvasContainer.getChildren().add(new com.jasperstudio.ui.canvas.ReportCanvas(designerEngine));
        propertiesContainer.getChildren().add(new com.jasperstudio.ui.properties.PropertiesPanel(designerEngine));
        assistantContainer.getChildren().add(new com.jasperstudio.ui.assistant.AssistantPanel(designerEngine));

        // Log Panel
        com.jasperstudio.ui.logging.LogPanel logPanel = new com.jasperstudio.ui.logging.LogPanel(designerEngine);
        logContainer.getChildren().add(logPanel);
    }

    private void setupBindings() {

        if (menuShowGrid != null) {
            menuShowGrid.selectedProperty().bindBidirectional(designerEngine.showGridProperty());
        }
        if (btnUndo != null) {
            btnUndo.setOnAction(e -> designerEngine.getHistoryManager().undo());
            btnUndo.disableProperty().bind(designerEngine.getHistoryManager().canUndoProperty().not());
        }
        if (btnRedo != null) {
            btnRedo.setOnAction(e -> designerEngine.getHistoryManager().redo());
            btnRedo.disableProperty().bind(designerEngine.getHistoryManager().canRedoProperty().not());
        }
        if (menuShowRulers != null) {
            menuShowRulers.selectedProperty().bindBidirectional(designerEngine.showRulersProperty());
        }
        if (menuSnapToGrid != null) {
            menuSnapToGrid.selectedProperty().bindBidirectional(designerEngine.snapToGridProperty());
        }
    }

    private void setupViewActions() {
        // Right Sidebar Toggle
        javafx.beans.value.ChangeListener<Boolean> rightUpdate = (obs, old, val) -> updateRightSidebar();
        if (menuShowProperties != null)
            menuShowProperties.selectedProperty().addListener(rightUpdate);
        if (menuShowAssistant != null)
            menuShowAssistant.selectedProperty().addListener(rightUpdate);

        // Initial Right Sidebar state
        updateRightSidebar();

        // Left Sidebar Toggling
        javafx.beans.value.ChangeListener<Boolean> leftUpdate = (obs, old, val) -> updateLeftSidebar();

        if (menuShowPalette != null)
            menuShowPalette.selectedProperty().addListener(leftUpdate);
        if (menuShowData != null)
            menuShowData.selectedProperty().addListener(leftUpdate);
        if (menuShowOutline != null)
            menuShowOutline.selectedProperty().addListener(leftUpdate);

        // Initial Left Sidebar state
        if (menuShowPalette != null)
            updateLeftSidebar();
    }

    private void updateRightSidebar() {
        updateTabPane(rightUpperTabPane, menuShowProperties, tabProperties);
        updateTabPane(rightLowerTabPane, menuShowAssistant, tabAssistant);

        if (rightSidebar != null && rightUpperTabPane != null && rightLowerTabPane != null) {
            updateSplitPaneItem(rightSidebar, rightUpperTabPane, !rightUpperTabPane.getTabs().isEmpty());
            updateSplitPaneItem(rightSidebar, rightLowerTabPane, !rightLowerTabPane.getTabs().isEmpty());

            boolean showRight = !rightSidebar.getItems().isEmpty();
            boolean hasRight = mainSplit.getItems().contains(rightSidebar);

            if (showRight && !hasRight) {
                mainSplit.getItems().add(rightSidebar);
                mainSplit.setDividerPositions(0.2, 0.8);
            } else if (!showRight && hasRight) {
                mainSplit.getItems().remove(rightSidebar);
            }
        }
    }

    private void updateLeftSidebar() {
        // Upper Tabs
        updateTabPane(leftUpperTabPane, menuShowPalette, tabPalette);
        updateTabPane(leftUpperTabPane, menuShowData, tabData);

        // Lower Tab
        updateTabPane(leftLowerTabPane, menuShowOutline, tabOutline);

        // Sidebar Sections
        if (leftSidebar != null && leftUpperTabPane != null && leftLowerTabPane != null) {
            updateSplitPaneItem(leftSidebar, leftUpperTabPane, !leftUpperTabPane.getTabs().isEmpty());
            updateSplitPaneItem(leftSidebar, leftLowerTabPane, !leftLowerTabPane.getTabs().isEmpty());

            // Main Sidebar
            boolean showLeft = !leftSidebar.getItems().isEmpty();
            boolean hasLeft = mainSplit.getItems().contains(leftSidebar);

            if (showLeft && !hasLeft) {
                mainSplit.getItems().add(0, leftSidebar);
                mainSplit.setDividerPositions(0.2, 0.8);
            } else if (!showLeft && hasLeft) {
                mainSplit.getItems().remove(leftSidebar);
            }
        }
    }

    private void updateTheme(String type) {
        if (getScene() == null)
            return;
        getScene().getStylesheets().clear();

        String css = "css/light.css"; // default
        if ("DARK".equals(type)) {
            css = "css/dark.css";
        } else if ("GLASS".equals(type)) {
            css = "css/glass.css";
        }

        getScene().getStylesheets().add(getClass().getResource(css).toExternalForm());

        // Sync menu state if called programmatically
        if ("DARK".equals(type) && menuStyleDark != null)
            menuStyleDark.setSelected(true);
        else if ("GLASS".equals(type) && menuStyleGlass != null)
            menuStyleGlass.setSelected(true);
        else if ("LIGHT".equals(type) && menuStyleLight != null)
            menuStyleLight.setSelected(true);
    }

    private void setupThemeActions() {
        javafx.scene.control.ToggleGroup themeGroup = new javafx.scene.control.ToggleGroup();

        if (menuStyleLight != null) {
            menuStyleLight.setToggleGroup(themeGroup);
            menuStyleLight.setOnAction(e -> updateTheme("LIGHT"));
        }
        if (menuStyleDark != null) {
            menuStyleDark.setToggleGroup(themeGroup);
            menuStyleDark.setOnAction(e -> updateTheme("DARK"));
        }
        if (menuStyleGlass != null) {
            menuStyleGlass.setToggleGroup(themeGroup);
            menuStyleGlass.setOnAction(e -> updateTheme("GLASS"));
        }

        // Wait for scene to apply initial theme
        this.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                // Default to Light if nothing selected, or check selected
                if (menuStyleDark != null && menuStyleDark.isSelected())
                    updateTheme("DARK");
                else if (menuStyleGlass != null && menuStyleGlass.isSelected())
                    updateTheme("GLASS");
                else
                    updateTheme("LIGHT");
            }
        });
    }

    private void updateTabPane(javafx.scene.control.TabPane pane, javafx.scene.control.CheckMenuItem menu,
            javafx.scene.control.Tab tab) {
        if (pane == null || menu == null || tab == null)
            return;
        boolean show = menu.isSelected();
        boolean has = pane.getTabs().contains(tab);
        if (show && !has) {
            pane.getTabs().add(tab);
        } else if (!show && has) {
            pane.getTabs().remove(tab);
        }
    }

    private void updateSplitPaneItem(javafx.scene.control.SplitPane split, javafx.scene.Node item, boolean show) {
        if (split == null || item == null)
            return;
        boolean has = split.getItems().contains(item);
        if (show && !has) {
            split.getItems().add(item);
        } else if (!show && has) {
            split.getItems().remove(item);
        }
    }

    @javafx.fxml.FXML
    private void onNew() {
        designerEngine.newDesign();
    }

    @javafx.fxml.FXML
    private void onOpen() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Open Report Design");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JRXML Files", "*.jrxml"));
        java.io.File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                designerEngine.openDesign(file);
                logger.info("Opened design: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to open design", ex);
            }
        }
    }

    @javafx.fxml.FXML
    private void onSave() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Report Design");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JRXML Files", "*.jrxml"));
        java.io.File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                designerEngine.saveDesign(file);
                logger.info("Saved design to: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to save design", ex);
            }
        }
    }

    @javafx.fxml.FXML
    private void onExit() {
        System.exit(0);
    }

    @javafx.fxml.FXML
    private void onZoomIn() {
        designerEngine.zoomIn();
    }

    @javafx.fxml.FXML
    private void onZoomOut() {
        designerEngine.zoomOut();
    }
}

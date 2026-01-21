package com.jasperstudio.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;

/**
 * The main shell of the application.
 * Replaces Eclipse WorkbenchWindow.
 */
public class MainWorkspace extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(MainWorkspace.class);

    private com.jasperstudio.designer.DesignerEngine currentEngine;

    // Layout Containers
    @FXML
    private SplitPane mainSplit; // Vertical: Center(Split) | Bottom
    @FXML
    private SplitPane centerSplit; // Horizontal: Left | Canvas | Right
    @FXML
    private SplitPane leftSidebarContainer; // Holds Palette/Outline/Data

    @FXML
    private StackPane canvasContainer;
    @FXML
    private SplitPane rightSidebarContainer; // Holds Propeties/Assistant

    @FXML
    private StackPane bottomContainer; // Holds Problems/Terminal

    // Activity Bar Buttons
    @FXML
    private ToggleButton btnActivityPalette;
    @FXML
    private ToggleButton btnActivityOutline;
    @FXML
    private ToggleButton btnActivityData;

    @FXML
    private ToggleButton btnActivityProblems;
    @FXML
    private ToggleButton btnActivityProperties;
    @FXML
    private ToggleButton btnActivityAssistant;

    // Content Panels (initialized in code, attached to containers)
    private StackPane paletteContainer;
    private StackPane dataContainer;
    private StackPane outlineContainer;

    @FXML
    private StackPane propertiesContainer;
    @FXML
    private StackPane assistantContainer;
    @FXML
    private StackPane logContainer;

    // Edit Menu
    @FXML
    private MenuItem menuUndo;
    @FXML
    private MenuItem menuRedo;
    @FXML
    private MenuItem menuCut;
    @FXML
    private MenuItem menuCopy;
    @FXML
    private MenuItem menuPaste;
    @FXML
    private MenuItem menuDelete;
    @FXML
    private MenuItem menuSelectAll;

    // View Menu Items
    @FXML
    private CheckMenuItem menuShowGrid;
    @FXML
    private CheckMenuItem menuShowRulers;
    @FXML
    private CheckMenuItem menuSnapToGrid;
    @FXML
    private CheckMenuItem menuSnapToGuides;
    @FXML
    private CheckMenuItem menuSnapToGeometry;
    @FXML
    private CheckMenuItem menuShowSpreadsheetTags;
    @FXML
    private CheckMenuItem menuShowJSONTags;
    @FXML
    private CheckMenuItem menuShowCSVTags;
    @FXML
    private CheckMenuItem menuShowXLSTags;
    @FXML
    private CheckMenuItem menuHighlightRenderGrid;
    @FXML
    private CheckMenuItem menuShowPDF508Tags;
    @FXML
    private CheckMenuItem menuShowErrorsForElements;

    @FXML
    private RadioMenuItem menuStyleLight;
    @FXML
    private RadioMenuItem menuStyleDark;
    @FXML
    private RadioMenuItem menuStyleGlass;

    // Side Panel Views
    private com.jasperstudio.ui.palette.PaletteView paletteView;
    private com.jasperstudio.ui.datasource.DataSourcePanel dataSourcePanel;
    private com.jasperstudio.ui.outline.OutlinePanel outlinePanel;
    private com.jasperstudio.ui.properties.PropertiesPanel propertiesPanel;
    private com.jasperstudio.ui.assistant.AssistantPanel assistantPanel;
    private com.jasperstudio.ui.logging.LogPanel logPanel;

    private TabPane editorTabPane;

    public MainWorkspace() {
        loadFXML();
        initSubViews(); // initialize panels content
        setupBindings();
        setupThemeActions();
        setupActivityBar();

        // Initial Empty Tab or New Design?
        // Let's create one initial tab
        onNew();
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWorkspace.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            logger.error("Failed to load MainWorkspace.fxml", e);
            throw new RuntimeException("Failed to load MainWorkspace.fxml", e);
        }
    }

    private void initSubViews() {
        // Create containers for left panels
        paletteContainer = new StackPane();
        dataContainer = new StackPane();
        outlineContainer = new StackPane();

        // Instantiate Side Views
        paletteView = new com.jasperstudio.ui.palette.PaletteView();
        dataSourcePanel = new com.jasperstudio.ui.datasource.DataSourcePanel();
        // Outline, Properties, Assistant, Log need an engine. We start with null or
        // handle it.
        // We'll pass null initially or create them and set engine later.
        // NOTE: The panels expect an engine in constructor currently, or I need to
        // modify them to accept null?
        // My previous refactors KEPT the constructor argument but I can pass null if I
        // verified they handle it.
        // Let's pass a dummy or null. OutlinePanel uses setDesignerEngine in
        // constructor.
        // If I pass null, it should be fine as I added null checks.

        outlinePanel = new com.jasperstudio.ui.outline.OutlinePanel(null);
        propertiesPanel = new com.jasperstudio.ui.properties.PropertiesPanel(null);
        assistantPanel = new com.jasperstudio.ui.assistant.AssistantPanel(null);
        logPanel = new com.jasperstudio.ui.logging.LogPanel(null);

        addToContainer(paletteContainer, paletteView);
        addToContainer(dataContainer, dataSourcePanel);
        addToContainer(outlineContainer, outlinePanel);

        // TabPane for Canvas
        editorTabPane = new TabPane();
        editorTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        addToContainer(canvasContainer, editorTabPane);

        // Tab Selection Listener
        editorTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab instanceof EditorTab) {
                updateCurrentEngine(((EditorTab) newTab).getEngine());
            } else {
                updateCurrentEngine(null);
            }
        });

        addToContainer(propertiesContainer, propertiesPanel);
        addToContainer(assistantContainer, assistantPanel);

        addToContainer(logContainer, logPanel);
    }

    private void addToContainer(StackPane container, Node node) {
        if (container != null) {
            container.getChildren().add(node);
        }
    }

    private void setupBindings() {
        // Initial setup binding is empty as it depends on updateCurrentEngine
        // We will call updateCurrentEngine(null) initially effectively.
    }

    private void updateCurrentEngine(com.jasperstudio.designer.DesignerEngine newEngine) {
        // Unbind Old
        if (this.currentEngine != null) {
            unbindEngine(this.currentEngine);
        }

        this.currentEngine = newEngine;

        // Update Sub Panels
        if (outlinePanel != null)
            outlinePanel.setDesignerEngine(newEngine);
        if (propertiesPanel != null)
            propertiesPanel.setDesignerEngine(newEngine);
        if (assistantPanel != null)
            assistantPanel.setDesignerEngine(newEngine);
        if (logPanel != null)
            logPanel.setDesignerEngine(newEngine);
        if (dataSourcePanel != null)
            dataSourcePanel.setDesignerEngine(newEngine);

        // Bind New
        if (this.currentEngine != null) {
            bindEngine(this.currentEngine);
        } else {
            // Disable actions?
            disableActions(true);
        }
    }

    private void unbindEngine(com.jasperstudio.designer.DesignerEngine engine) {
        if (engine == null)
            return;

        // Remove bindings for menu items
        // Since bindBidirectional was used, we need to unbind bidirectionally.
        // CheckMenuItem.selectedProperty().unbindBidirectional(property)

        unbindProp(menuShowGrid, engine.showGridProperty());
        unbindProp(menuShowRulers, engine.showRulersProperty());
        unbindProp(menuSnapToGrid, engine.snapToGridProperty());
        unbindProp(menuSnapToGuides, engine.snapToGuidesProperty());
        unbindProp(menuSnapToGeometry, engine.snapToGeometryProperty());
        unbindProp(menuShowSpreadsheetTags, engine.showSpreadsheetTagsProperty());
        unbindProp(menuShowJSONTags, engine.showJSONTagsProperty());
        unbindProp(menuShowCSVTags, engine.showCSVTagsProperty());
        unbindProp(menuShowXLSTags, engine.showXLSTagsProperty());
        unbindProp(menuHighlightRenderGrid, engine.highlightRenderGridProperty());
        unbindProp(menuShowPDF508Tags, engine.showPDF508TagsProperty());
        unbindProp(menuShowErrorsForElements, engine.showErrorsForElementsProperty());

        // Actions are typically set via setOnAction calling a lambda capturing
        // 'currentEngine' (which is this.currentEngine field?),
        // BUT my previous code setOnAction(e ->
        // designerEngine.getHistoryManager().undo()).
        // If I change designerEngine to currentEngine, and currentEngine reference
        // changes, the lambda might be stale if it captured the field value?
        // No, if lambda uses 'this.currentEngine', it uses the current value of the
        // field.
        // HOWEVER, 'setupAction' binds disableProperty. We need to unbind that.

        menuUndo.disableProperty().unbind();
        menuRedo.disableProperty().unbind();
    }

    private void bindEngine(com.jasperstudio.designer.DesignerEngine engine) {
        if (engine == null)
            return;

        bindBidirectional(menuShowGrid, engine.showGridProperty());
        bindBidirectional(menuShowRulers, engine.showRulersProperty());
        bindBidirectional(menuSnapToGrid, engine.snapToGridProperty());
        bindBidirectional(menuSnapToGuides, engine.snapToGuidesProperty());
        bindBidirectional(menuSnapToGeometry, engine.snapToGeometryProperty());
        bindBidirectional(menuShowSpreadsheetTags, engine.showSpreadsheetTagsProperty());
        bindBidirectional(menuShowJSONTags, engine.showJSONTagsProperty());
        bindBidirectional(menuShowCSVTags, engine.showCSVTagsProperty());
        bindBidirectional(menuShowXLSTags, engine.showXLSTagsProperty());
        bindBidirectional(menuHighlightRenderGrid, engine.highlightRenderGridProperty());
        bindBidirectional(menuShowPDF508Tags, engine.showPDF508TagsProperty());
        bindBidirectional(menuShowErrorsForElements, engine.showErrorsForElementsProperty());

        // History bindings
        menuUndo.disableProperty().bind(engine.getHistoryManager().canUndoProperty().not());
        menuRedo.disableProperty().bind(engine.getHistoryManager().canRedoProperty().not());

        // Define Actions (using current engine)
        // Note: We don't need to re-set onAction if we use 'this.currentEngine' in the
        // lambda,
        // BUT we do need to re-bind disable properties.
        // Actions wrapper:
        disableActions(false);
    }

    private void disableActions(boolean disable) {
        // Enable/Disable non-bound actions if no engine
        menuCut.setDisable(disable);
        menuCopy.setDisable(disable);
        menuPaste.setDisable(disable);
        menuDelete.setDisable(disable);
        menuSelectAll.setDisable(disable);

        if (disable) {
            menuUndo.setDisable(true);
            menuRedo.setDisable(true);
        }
    }

    private void unbindProp(CheckMenuItem item, BooleanProperty prop) {
        if (item != null && prop != null) {
            item.selectedProperty().unbindBidirectional(prop);
        }
    }

    private void setupActivityBar() {
        // Clear initial items (managed by buttons)
        if (leftSidebarContainer != null)
            leftSidebarContainer.getItems().clear();
        if (rightSidebarContainer != null)
            rightSidebarContainer.getItems().clear();

        // Left Sidebar Handlers
        setupSidebarToggle(btnActivityPalette, leftSidebarContainer, paletteContainer, 0);
        setupSidebarToggle(btnActivityOutline, leftSidebarContainer, outlineContainer, 0);
        setupSidebarToggle(btnActivityData, leftSidebarContainer, dataContainer, 0);

        // Right Sidebar Handlers
        setupSidebarToggle(btnActivityProperties, rightSidebarContainer, propertiesContainer, -1);
        setupSidebarToggle(btnActivityAssistant, rightSidebarContainer, assistantContainer, -1);

        // Bottom
        btnActivityProblems.selectedProperty().addListener((obs, old, isSelected) -> {
            toggleSplitItem(mainSplit, bottomContainer, isSelected, 1);
        });
    }

    private void setupSidebarToggle(ToggleButton btn, SplitPane sidebar, Node panel, int centerSplitIndex) {
        if (btn == null)
            return;

        // Listener
        btn.selectedProperty().addListener((obs, old, isSelected) -> {
            updateSidebarState(sidebar, panel, isSelected, centerSplitIndex);
        });

        // Initial State
        if (btn.isSelected()) {
            updateSidebarState(sidebar, panel, true, centerSplitIndex);
        }
    }

    private void updateSidebarState(SplitPane sidebar, Node panel, boolean showPanel, int centerSplitIndex) {
        if (sidebar == null || panel == null)
            return;

        // 1. Toggle panel in sidebar
        toggleSplitItem(sidebar, panel, showPanel, -1);

        // 2. Toggle sidebar in center split based on whether it has items
        boolean hasItems = !sidebar.getItems().isEmpty();
        toggleSplitItem(centerSplit, sidebar, hasItems, centerSplitIndex);
    }

    private void toggleSplitItem(SplitPane split, Node item, boolean show, int targetIndex) {
        if (split == null || item == null)
            return;

        boolean isPresent = split.getItems().contains(item);

        if (show && !isPresent) {
            if (targetIndex >= 0 && targetIndex <= split.getItems().size()) {
                split.getItems().add(targetIndex, item);
            } else {
                split.getItems().add(item);
            }
            if (split == centerSplit) {
                // Adjust divider if strictly needed, or let user adjust
                if (split.getItems().size() > 1) {
                    split.setDividerPositions(0.2, 0.8);
                }
            } else if (split == mainSplit) {
                split.setDividerPositions(0.8);
            }
        } else if (!show && isPresent) {
            split.getItems().remove(item);
        }
    }

    private void bindBidirectional(CheckMenuItem item, BooleanProperty property) {
        if (item != null && property != null) {
            item.selectedProperty().bindBidirectional(property);
        }
    }

    private void setupAction(MenuItem item, Runnable action) {
        setupAction(item, action, null);
    }

    private void setupAction(MenuItem item, Runnable action, ObservableValue<Boolean> disableProperty) {
        if (item != null) {
            item.setOnAction(e -> action.run());
            if (disableProperty != null) {
                item.disableProperty().bind(disableProperty);
            }
        }
    }

    private void updateTheme(String type) {
        if (getScene() == null)
            return;
        getScene().getStylesheets().clear();

        String workspaceCss = getClass().getResource("MainWorkspace.css").toExternalForm();
        getScene().getStylesheets().add(workspaceCss);

        String css = "css/light.css";
        if ("DARK".equals(type)) {
            css = "css/dark.css";
        } else if ("GLASS".equals(type)) {
            css = "css/glass.css";
        }

        getScene().getStylesheets().add(getClass().getResource(css).toExternalForm());

        if ("DARK".equals(type) && menuStyleDark != null)
            menuStyleDark.setSelected(true);
        else if ("GLASS".equals(type) && menuStyleGlass != null)
            menuStyleGlass.setSelected(true);
        else if ("LIGHT".equals(type) && menuStyleLight != null)
            menuStyleLight.setSelected(true);
    }

    private void setupThemeActions() {
        ToggleGroup themeGroup = new ToggleGroup();
        setupThemeItem(menuStyleLight, "LIGHT", themeGroup);
        setupThemeItem(menuStyleDark, "DARK", themeGroup);
        setupThemeItem(menuStyleGlass, "GLASS", themeGroup);

        this.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                if (menuStyleDark != null && menuStyleDark.isSelected())
                    updateTheme("DARK");
                else if (menuStyleGlass != null && menuStyleGlass.isSelected())
                    updateTheme("GLASS");
                else
                    updateTheme("LIGHT");

                setupShortcuts();
                setupActions();
            }
        });
    }

    private void setupThemeItem(RadioMenuItem item, String theme, ToggleGroup group) {
        if (item != null) {
            item.setToggleGroup(group);
            item.setOnAction(e -> updateTheme(theme));
        }
    }

    @FXML
    private void onNew() {
        com.jasperstudio.designer.DesignerEngine engine = new com.jasperstudio.designer.DesignerEngine();
        EditorTab tab = new EditorTab("NewReport", engine);
        editorTabPane.getTabs().add(tab);
        editorTabPane.getSelectionModel().select(tab);
        // Focus canvas
        tab.getContent().requestFocus();
    }

    @FXML
    private void onOpen() {
        File file = showFileChooser("Open Report Design", false);
        if (file != null) {
            // Check if already open
            for (Tab t : editorTabPane.getTabs()) {
                if (t instanceof EditorTab) {
                    EditorTab et = (EditorTab) t;
                    if (et.getFile() != null && et.getFile().equals(file)) {
                        editorTabPane.getSelectionModel().select(t);
                        return;
                    }
                }
            }

            try {
                com.jasperstudio.designer.DesignerEngine engine = new com.jasperstudio.designer.DesignerEngine();
                engine.openDesign(file);

                EditorTab tab = new EditorTab(file.getName(), engine);
                tab.setFile(file);

                editorTabPane.getTabs().add(tab);
                editorTabPane.getSelectionModel().select(tab);
                logger.info("Opened design: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to open design", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open design: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onSave() {
        if (currentEngine == null)
            return;

        EditorTab currentTab = (EditorTab) editorTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null)
            return;

        File file = currentTab.getFile();
        if (file == null) {
            file = showFileChooser("Save Report Design", true);
        }

        if (file != null) {
            try {
                currentEngine.saveDesign(file);
                currentTab.setFile(file); // Update tab info
                logger.info("Saved design to: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to save design", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save design: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onClose() {
        Tab selected = editorTabPane.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editorTabPane.getTabs().remove(selected);
        }
    }

    @FXML
    private void onCloseAll() {
        editorTabPane.getTabs().clear();
    }

    private File showFileChooser(String title, boolean save) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JRXML Files", "*.jrxml"));
        if (save) {
            return fileChooser.showSaveDialog(getScene().getWindow());
        } else {
            return fileChooser.showOpenDialog(getScene().getWindow());
        }
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }

    @FXML
    private void onZoomIn() {
        if (currentEngine != null)
            currentEngine.zoomIn();
    }

    @FXML
    private void onZoomOut() {
        if (currentEngine != null)
            currentEngine.zoomOut();
    }

    // Define Actions for Menus using currentEngine wrapper
    private void setupActions() {
        // This is a correction to setupBindings.
        // Since bindEngine handles disable props, we just need to ensure the menu items
        // invoke current engine actions.
        if (menuUndo != null)
            menuUndo.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.getHistoryManager().undo();
            });
        if (menuRedo != null)
            menuRedo.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.getHistoryManager().redo();
            });

        if (menuDelete != null)
            menuDelete.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.deleteSelection();
            });
        if (menuSelectAll != null)
            menuSelectAll.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.selectAll();
            });
        if (menuCut != null)
            menuCut.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.cut();
            });
        if (menuCopy != null)
            menuCopy.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.copy();
            });
        if (menuPaste != null)
            menuPaste.setOnAction(e -> {
                if (currentEngine != null)
                    currentEngine.paste();
            });
    }

    // Add Shortcuts
    public void setupShortcuts() {
        if (getScene() == null)
            return;

        getScene().getAccelerators().put(
                new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.S,
                        javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                this::onSave);
        getScene().getAccelerators().put(
                new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.W,
                        javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                this::onClose);
        getScene().getAccelerators().put(
                new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.W,
                        javafx.scene.input.KeyCombination.SHORTCUT_DOWN, javafx.scene.input.KeyCombination.SHIFT_DOWN),
                this::onCloseAll);
        getScene().getAccelerators().put(
                new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.N,
                        javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                this::onNew);
        getScene().getAccelerators().put(
                new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.O,
                        javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                this::onOpen);
    }
}

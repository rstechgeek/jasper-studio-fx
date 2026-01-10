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

    private final com.jasperstudio.designer.DesignerEngine designerEngine;

    @FXML private SplitPane mainSplit;
    @FXML private SplitPane leftSidebar;
    @FXML private TabPane leftUpperTabPane;
    @FXML private TabPane leftLowerTabPane;
    @FXML private Tab tabPalette;
    @FXML private Tab tabData;
    @FXML private Tab tabOutline;
    @FXML private StackPane paletteContainer;
    @FXML private StackPane dataContainer;
    @FXML private StackPane outlineContainer;

    // Right Sidebar
    @FXML private SplitPane rightSidebar;
    @FXML private TabPane rightUpperTabPane;
    @FXML private TabPane rightLowerTabPane;
    @FXML private Tab tabProperties;
    @FXML private Tab tabAssistant;
    @FXML private StackPane propertiesContainer;
    @FXML private StackPane assistantContainer;

    @FXML private StackPane canvasContainer;
    @FXML private StackPane logContainer;

    @FXML private Button btnUndo;
    @FXML private Button btnRedo;

    // Edit Menu
    @FXML private MenuItem menuUndo;
    @FXML private MenuItem menuRedo;
    @FXML private MenuItem menuCut;
    @FXML private MenuItem menuCopy;
    @FXML private MenuItem menuPaste;
    @FXML private MenuItem menuDelete;
    @FXML private MenuItem menuSelectAll;

    // Menu Items
    @FXML private CheckMenuItem menuShowPalette;
    @FXML private CheckMenuItem menuShowData;
    @FXML private CheckMenuItem menuShowOutline;
    @FXML private CheckMenuItem menuShowProperties;
    @FXML private CheckMenuItem menuShowAssistant;
    @FXML private CheckMenuItem menuShowGrid;
    @FXML private CheckMenuItem menuShowRulers;
    @FXML private CheckMenuItem menuSnapToGrid;
    @FXML private CheckMenuItem menuSnapToGuides;
    @FXML private CheckMenuItem menuSnapToGeometry;
    @FXML private CheckMenuItem menuShowSpreadsheetTags;
    @FXML private CheckMenuItem menuShowJSONTags;
    @FXML private CheckMenuItem menuShowCSVTags;
    @FXML private CheckMenuItem menuShowXLSTags;
    @FXML private CheckMenuItem menuHighlightRenderGrid;
    @FXML private CheckMenuItem menuShowPDF508Tags;
    @FXML private CheckMenuItem menuShowErrorsForElements;

    @FXML private RadioMenuItem menuStyleLight;
    @FXML private RadioMenuItem menuStyleDark;
    @FXML private RadioMenuItem menuStyleGlass;

    public MainWorkspace() {
        this.designerEngine = new com.jasperstudio.designer.DesignerEngine();
        loadFXML();
        initSubViews();
        setupBindings();
        setupViewActions();
        setupThemeActions(); // Added call
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
        // Instantiate and inject sub-views
        addToContainer(paletteContainer, new com.jasperstudio.ui.palette.PaletteView());
        addToContainer(dataContainer, new com.jasperstudio.ui.datasource.DataSourcePanel());
        addToContainer(outlineContainer, new com.jasperstudio.ui.outline.OutlinePanel(designerEngine));

        addToContainer(canvasContainer, new com.jasperstudio.ui.canvas.ReportCanvas(designerEngine));
        addToContainer(propertiesContainer, new com.jasperstudio.ui.properties.PropertiesPanel(designerEngine));
        addToContainer(assistantContainer, new com.jasperstudio.ui.assistant.AssistantPanel(designerEngine));

        addToContainer(logContainer, new com.jasperstudio.ui.logging.LogPanel(designerEngine));
    }

    private void addToContainer(StackPane container, Node node) {
        if (container != null) {
            container.getChildren().add(node);
        }
    }

    private void setupBindings() {
        // View Options
        bindBidirectional(menuShowGrid, designerEngine.showGridProperty());
        bindBidirectional(menuShowRulers, designerEngine.showRulersProperty());
        bindBidirectional(menuSnapToGrid, designerEngine.snapToGridProperty());
        bindBidirectional(menuSnapToGuides, designerEngine.snapToGuidesProperty());
        bindBidirectional(menuSnapToGeometry, designerEngine.snapToGeometryProperty());
        bindBidirectional(menuShowSpreadsheetTags, designerEngine.showSpreadsheetTagsProperty());
        bindBidirectional(menuShowJSONTags, designerEngine.showJSONTagsProperty());
        bindBidirectional(menuShowCSVTags, designerEngine.showCSVTagsProperty());
        bindBidirectional(menuShowXLSTags, designerEngine.showXLSTagsProperty());
        bindBidirectional(menuHighlightRenderGrid, designerEngine.highlightRenderGridProperty());
        bindBidirectional(menuShowPDF508Tags, designerEngine.showPDF508TagsProperty());
        bindBidirectional(menuShowErrorsForElements, designerEngine.showErrorsForElementsProperty());

        // History Actions
        setupAction(btnUndo, designerEngine.getHistoryManager()::undo, designerEngine.getHistoryManager().canUndoProperty().not());
        setupAction(btnRedo, designerEngine.getHistoryManager()::redo, designerEngine.getHistoryManager().canRedoProperty().not());
        setupAction(menuUndo, designerEngine.getHistoryManager()::undo, designerEngine.getHistoryManager().canUndoProperty().not());
        setupAction(menuRedo, designerEngine.getHistoryManager()::redo, designerEngine.getHistoryManager().canRedoProperty().not());

        // Edit Actions
        setupAction(menuDelete, designerEngine::deleteSelection);
        setupAction(menuSelectAll, designerEngine::selectAll);
        setupAction(menuCut, designerEngine::cut);
        setupAction(menuCopy, designerEngine::copy);
        setupAction(menuPaste, designerEngine::paste);
    }

    private void bindBidirectional(CheckMenuItem item, BooleanProperty property) {
        if (item != null && property != null) {
            item.selectedProperty().bindBidirectional(property);
        }
    }

    private void setupAction(ButtonBase button, Runnable action, ObservableValue<Boolean> disableProperty) {
        if (button != null) {
            button.setOnAction(e -> action.run());
            if (disableProperty != null) {
                button.disableProperty().bind(disableProperty);
            }
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

    private void setupViewActions() {
        // Listener for sidebar updates
        Runnable updateLayout = () -> {
            updateLeftSidebar();
            updateRightSidebar();
        };

        // Register listeners
        registerListener(menuShowPalette, updateLayout);
        registerListener(menuShowData, updateLayout);
        registerListener(menuShowOutline, updateLayout);
        registerListener(menuShowProperties, updateLayout);
        registerListener(menuShowAssistant, updateLayout);

        // Initial state
        updateLayout.run();
    }

    private void registerListener(CheckMenuItem item, Runnable action) {
        if (item != null) {
            item.selectedProperty().addListener((obs, old, val) -> action.run());
        }
    }

    private void updateRightSidebar() {
        updateTabPane(rightUpperTabPane, menuShowProperties, tabProperties);
        updateTabPane(rightLowerTabPane, menuShowAssistant, tabAssistant);
        updateSidebarContainer(rightSidebar, rightUpperTabPane, rightLowerTabPane);
    }

    private void updateLeftSidebar() {
        updateTabPane(leftUpperTabPane, menuShowPalette, tabPalette);
        updateTabPane(leftUpperTabPane, menuShowData, tabData);
        updateTabPane(leftLowerTabPane, menuShowOutline, tabOutline);
        updateSidebarContainer(leftSidebar, leftUpperTabPane, leftLowerTabPane);
    }

    private void updateSidebarContainer(SplitPane sidebar, TabPane... panes) {
        if (sidebar == null) return;

        // Update sidebar children (split items)
        for (TabPane pane : panes) {
            if (pane != null) {
                updateSplitPaneItem(sidebar, pane, !pane.getTabs().isEmpty());
            }
        }

        // Update Main Split
        boolean showSidebar = !sidebar.getItems().isEmpty();
        boolean hasSidebar = mainSplit.getItems().contains(sidebar);

        if (showSidebar && !hasSidebar) {
            if (sidebar == leftSidebar) {
                mainSplit.getItems().add(0, leftSidebar);
            } else {
                mainSplit.getItems().add(sidebar);
            }
            mainSplit.setDividerPositions(0.2, 0.8);
        } else if (!showSidebar && hasSidebar) {
            mainSplit.getItems().remove(sidebar);
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
        ToggleGroup themeGroup = new ToggleGroup();
        setupThemeItem(menuStyleLight, "LIGHT", themeGroup);
        setupThemeItem(menuStyleDark, "DARK", themeGroup);
        setupThemeItem(menuStyleGlass, "GLASS", themeGroup);

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

    private void setupThemeItem(RadioMenuItem item, String theme, ToggleGroup group) {
        if (item != null) {
            item.setToggleGroup(group);
            item.setOnAction(e -> updateTheme(theme));
        }
    }

    private void updateTabPane(TabPane pane, CheckMenuItem menu, Tab tab) {
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

    private void updateSplitPaneItem(SplitPane split, Node item, boolean show) {
        if (split == null || item == null)
            return;
        boolean has = split.getItems().contains(item);
        if (show && !has) {
            split.getItems().add(item);
        } else if (!show && has) {
            split.getItems().remove(item);
        }
    }

    @FXML
    private void onNew() {
        designerEngine.newDesign();
    }

    @FXML
    private void onOpen() {
        File file = showFileChooser("Open Report Design", false);
        if (file != null) {
            try {
                designerEngine.openDesign(file);
                logger.info("Opened design: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to open design", ex);
            }
        }
    }

    @FXML
    private void onSave() {
        File file = showFileChooser("Save Report Design", true);
        if (file != null) {
            try {
                designerEngine.saveDesign(file);
                logger.info("Saved design to: {}", file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Failed to save design", ex);
            }
        }
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
        designerEngine.zoomIn();
    }

    @FXML
    private void onZoomOut() {
        designerEngine.zoomOut();
    }
}

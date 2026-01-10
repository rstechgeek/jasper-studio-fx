package com.jasperstudio.ui.outline;

import com.jasperstudio.designer.DesignerEngine;
import com.jasperstudio.model.ElementModel;
import com.jasperstudio.model.JasperDesignModel;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

import net.sf.jasperreports.engine.design.JRDesignElement;

public class OutlinePanel extends VBox {

    private final DesignerEngine engine;

    @javafx.fxml.FXML
    private TreeView<Object> treeView;
    private boolean isUpdatingSelection = false;

    // Listener field
    private javafx.collections.ListChangeListener<com.jasperstudio.model.BandModel> bandsListener;

    public OutlinePanel(DesignerEngine engine) {
        this.engine = engine;
        loadFXML();

        // Initialize listener here so 'engine' is available
        this.bandsListener = c -> {
            javafx.application.Platform.runLater(() -> rebuildTree(engine.getDesign()));
        };

        // Listen for Design Changes
        engine.currentDesignProperty().addListener((obs, oldVal, newVal) -> bindDesign(oldVal, newVal));
        if (engine.getDesign() != null) {
            bindDesign(null, engine.getDesign());
        }

        // Selection Sync: Engine -> Tree
        engine.selectionProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingSelection)
                return;
            isUpdatingSelection = true;
            try {
                if (newVal == null) {
                    treeView.getSelectionModel().clearSelection();
                } else if (newVal instanceof com.jasperstudio.model.ElementModel) {
                    selectItemForElement(treeView.getRoot(),
                            ((com.jasperstudio.model.ElementModel) newVal).getElement());
                } else if (newVal instanceof com.jasperstudio.model.BandModel) {
                    selectItemForBand(treeView.getRoot(), (com.jasperstudio.model.BandModel) newVal);
                }
            } finally {
                isUpdatingSelection = false;
            }
        });

        // Selection Sync: Tree -> Engine
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingSelection)
                return;
            isUpdatingSelection = true;
            try {
                if (newVal != null) {
                    Object val = newVal.getValue();
                    if (val instanceof com.jasperstudio.model.BandModel) {
                        engine.setSelection(val);
                    } else if (val instanceof com.jasperstudio.model.ElementModel) {
                        engine.setSelection(val);
                    } else if (val instanceof com.jasperstudio.model.JasperDesignModel) {
                        engine.setSelection(val);
                    } else if (val instanceof JRDesignElement) { // Fallback, shouldn't happen with new tree
                        JRDesignElement el = (JRDesignElement) val;
                        ElementModel model = findModelForElement(engine.getDesign(), el);
                        engine.setSelection(model);
                    } else {
                        engine.clearSelection();
                    }
                } else {
                    engine.clearSelection();
                }
            } finally {
                isUpdatingSelection = false;
            }
        });
    }

    private void loadFXML() {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("OutlinePanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load OutlinePanel.fxml", e);
        }
    }

    private String getNameForItem(Object item) {
        if (item instanceof JasperDesignModel)
            return "Report";
        if (item instanceof com.jasperstudio.model.BandModel)
            return ((com.jasperstudio.model.BandModel) item).getType() + " ["
                    + ((com.jasperstudio.model.BandModel) item).getHeight() + "px]";
        if (item instanceof com.jasperstudio.model.ElementModel) {
            net.sf.jasperreports.engine.design.JRDesignElement el = ((com.jasperstudio.model.ElementModel) item)
                    .getElement();
            String type = el.getClass().getSimpleName().replace("JRDesign", "");
            return type + " [" + el.getX() + ", " + el.getY() + "]";
        }
        if (item instanceof JRDesignElement) { // Fallback
            JRDesignElement el = (JRDesignElement) item;
            String type = el.getClass().getSimpleName().replace("JRDesign", "");
            return type + " [" + el.getX() + ", " + el.getY() + "]";
        }
        return item.toString();
    }

    // List of all supported bands in order
    private static final java.util.List<String> ALL_BANDS = java.util.Arrays.asList(
            "Title",
            "Page Header",
            "Column Header",
            "Detail",
            "Column Footer",
            "Page Footer",
            "Last Page Footer",
            "Summary",
            "No Data",
            "Background");

    private void bindDesign(JasperDesignModel oldDesign, JasperDesignModel newDesign) {
        if (oldDesign != null) {
            oldDesign.getBands().removeListener(bandsListener);
        }
        if (newDesign != null) {
            newDesign.getBands().addListener(bandsListener);
            rebuildTree(newDesign);
        } else {
            rebuildTree(null);
        }
    }

    private void rebuildTree(JasperDesignModel design) {
        if (design == null) {
            treeView.setRoot(null);
            return;
        }

        TreeItem<Object> root = new TreeItem<>(design);
        root.setExpanded(true);

        for (String type : ALL_BANDS) {
            com.jasperstudio.model.BandModel band = design.getBand(type);

            if (band != null) {
                // Existing Band
                TreeItem<Object> bandItem = new TreeItem<>(band);
                bandItem.setExpanded(true);
                root.getChildren().add(bandItem);

                refreshBandItem(bandItem, band);

                // Live updates for elements
                band.getElements()
                        .addListener((javafx.collections.ListChangeListener<com.jasperstudio.model.ElementModel>) c -> {
                            refreshBandItem(bandItem, band);
                        });
            } else {
                // Placeholder for optional/missing band
                TreeItem<Object> placeholder = new TreeItem<>(new BandPlaceholder(type));
                root.getChildren().add(placeholder);
            }
        }

        treeView.setRoot(root);

        // Context Menu Setup
        treeView.setCellFactory(tv -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    setStyle("");
                } else {
                    setText(getNameForItem(item));
                    if (item instanceof BandPlaceholder) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                        javafx.scene.control.ContextMenu cm = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem addM = new javafx.scene.control.MenuItem("Add Band");
                        addM.setOnAction(e -> engine.addBand(((BandPlaceholder) item).type));
                        cm.getItems().add(addM);
                        setContextMenu(cm);
                    } else if (item instanceof com.jasperstudio.model.BandModel) {
                        setStyle("-fx-font-weight: bold;");
                        com.jasperstudio.model.BandModel b = (com.jasperstudio.model.BandModel) item;

                        // Allow delete for all bands
                        javafx.scene.control.ContextMenu cm = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem delM = new javafx.scene.control.MenuItem("Delete Band");
                        delM.setOnAction(e -> engine.deleteBand(b.getType()));
                        cm.getItems().add(delM);
                        setContextMenu(cm);
                    } else {
                        setStyle("");
                        setContextMenu(null);
                    }
                }
            }
        });
    }

    private static class BandPlaceholder {
        String type;

        BandPlaceholder(String t) {
            this.type = t;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private void refreshBandItem(TreeItem<Object> bandItem, com.jasperstudio.model.BandModel band) {
        bandItem.getChildren().clear();
        for (com.jasperstudio.model.ElementModel em : band.getElements()) {
            TreeItem<Object> item = new TreeItem<>(em);
            bandItem.getChildren().add(item);
        }
    }

    private void selectItemForBand(TreeItem<Object> current, com.jasperstudio.model.BandModel target) {
        if (current == null)
            return;
        if (current.getValue() == target) {
            treeView.getSelectionModel().select(current);
            treeView.scrollTo(treeView.getRow(current));
            return;
        }
        for (TreeItem<Object> child : current.getChildren()) {
            selectItemForBand(child, target);
        }
    }

    private void selectItemForElement(TreeItem<Object> current, JRDesignElement target) {
        if (current == null)
            return;
        if (current.getValue() == target) {
            treeView.getSelectionModel().select(current);
            int row = treeView.getRow(current);
            treeView.scrollTo(row);
            return;
        }
        for (TreeItem<Object> kid : current.getChildren()) {
            selectItemForElement(kid, target);
        }
    }

    private ElementModel findModelForElement(JasperDesignModel design, JRDesignElement target) {
        return new ElementModel(target);
    }
}

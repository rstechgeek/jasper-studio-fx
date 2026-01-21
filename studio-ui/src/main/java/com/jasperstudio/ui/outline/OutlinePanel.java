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

    private DesignerEngine engine;

    @javafx.fxml.FXML
    private javafx.scene.control.Label lblHeader;

    private String reportName = "Report";

    public void setHeaderTitle(String title) {
        if (lblHeader != null) {
            lblHeader.setText("Outline");
        }
        if (title != null && !title.trim().isEmpty()) {
            int dotIndex = title.lastIndexOf('.');
            this.reportName = (dotIndex > 0) ? title.substring(0, dotIndex) : title;
        } else {
            this.reportName = "Report";
        }
        if (treeView != null) {
            treeView.refresh();
        }
    }

    @javafx.fxml.FXML
    private TreeView<Object> treeView;
    private boolean isUpdatingSelection = false;

    // Listener fields
    private javafx.collections.ListChangeListener<com.jasperstudio.model.BandModel> bandsListener;
    private javafx.beans.value.ChangeListener<JasperDesignModel> designListener;
    private javafx.beans.value.ChangeListener<Object> selectionListener;
    private javafx.beans.value.ChangeListener<TreeItem<Object>> treeSelectionListener;

    public OutlinePanel(DesignerEngine engine) {
        loadFXML();

        // Initialize band listener (depended on by bindDesign)
        this.bandsListener = c -> {
            if (this.engine != null) {
                // Capture the design current at time of event
                JasperDesignModel d = this.engine.getDesign();
                javafx.application.Platform.runLater(() -> rebuildTree(d));
            }
        };

        // Initialize Tree Selection Listener
        this.treeSelectionListener = (obs, oldVal, newVal) -> {
            if (isUpdatingSelection || this.engine == null)
                return;
            isUpdatingSelection = true;
            try {
                if (this.engine.getViewMode() == com.jasperstudio.designer.DesignerEngine.ViewMode.SOURCE) {
                    handleSourceSelection(newVal);
                } else if (newVal != null) {
                    Object val = newVal.getValue();
                    if (val instanceof com.jasperstudio.model.BandModel) {
                        this.engine.setSelection(val);
                    } else if (val instanceof com.jasperstudio.model.ElementModel) {
                        this.engine.setSelection(val);
                    } else if (val instanceof com.jasperstudio.model.JasperDesignModel) {
                        this.engine.setSelection(val);
                    } else if (val instanceof JRDesignElement) {
                        JRDesignElement el = (JRDesignElement) val;
                        ElementModel model = findModelForElement(this.engine.getDesign(), el);
                        this.engine.setSelection(model);
                    } else {
                        this.engine.clearSelection();
                    }
                } else {
                    this.engine.clearSelection();
                }
            } finally {
                isUpdatingSelection = false;
            }
        };
        treeView.getSelectionModel().selectedItemProperty().addListener(treeSelectionListener);

        setDesignerEngine(engine);
    }

    private void handleSourceSelection(TreeItem<Object> item) {
        if (item == null || item.getValue() == null)
            return;
        if (item.getValue() instanceof XmlTag) {
            XmlTag tag = (XmlTag) item.getValue();
            // Calculate offset
            String xml = this.engine.getXmlSource();
            if (xml != null && !xml.isEmpty()) {
                int startOffset = getOffset(xml, tag.line, tag.col);
                int endOffset = -1;

                if (tag.endLine > 0) {
                    endOffset = getOffset(xml, tag.endLine, tag.endCol);
                }

                if (startOffset >= 0) {
                    if (endOffset > startOffset) {
                        // +1 to encompass the closing bracket '>' usually at that column
                        // But SAX locator is loose. Let's try to find the closing '>' from the
                        // endOffset
                        int closeBracket = xml.indexOf('>', endOffset);
                        if (closeBracket != -1) {
                            endOffset = closeBracket + 1;
                        }
                        this.engine.setSourceSelection(startOffset, endOffset);
                    } else {
                        // Fallback
                        int len = tag.name.length() + 1;
                        this.engine.setSourceSelection(startOffset, startOffset + len);
                    }
                }
            }
        }
    }

    private int getOffset(String xml, int line, int col) {
        // line is 1-based, col is 1-based
        int currentLine = 1;
        int max = xml.length();
        for (int i = 0; i < max; i++) {
            if (currentLine == line) {
                return i + (col - 1);
            }
            if (xml.charAt(i) == '\n') {
                currentLine++;
            }
        }
        return -1;
    }

    // ... listeners ...

    public void setDesignerEngine(DesignerEngine newEngine) {
        // Cleanup old engine listeners
        if (this.engine != null) {
            if (designListener != null) {
                this.engine.currentDesignProperty().removeListener(designListener);
            }
            if (selectionListener != null) {
                this.engine.selectionProperty().removeListener(selectionListener);
            }
            if (this.engine.getDesign() != null) {
                this.engine.getDesign().getBands().removeListener(bandsListener);
            }
        }

        this.engine = newEngine;

        if (this.engine != null) {
            setupEngineListeners();
            // Initial Sync
            bindDesign(null, this.engine.getDesign());
        } else {
            rebuildTree(null);
        }
    }

    private void setupEngineListeners() {
        // Listen for Design Changes
        this.designListener = (obs, oldVal, newVal) -> bindDesign(oldVal, newVal);
        this.engine.currentDesignProperty().addListener(designListener);

        // Selection Sync: Engine -> Tree
        this.selectionListener = (obs, oldVal, newVal) -> {
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
                } else if (newVal instanceof com.jasperstudio.model.JasperDesignModel) {
                    // Select Root
                    if (treeView.getRoot() != null && treeView.getRoot().getValue() == newVal) {
                        treeView.getSelectionModel().select(treeView.getRoot());
                    }
                }
            } finally {
                isUpdatingSelection = false;
            }
        };
        this.engine.selectionProperty().addListener(selectionListener);

        // XML / View Support
        this.engine.viewModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == com.jasperstudio.designer.DesignerEngine.ViewMode.SOURCE) {
                rebuildXmlTree(this.engine.getXmlSource());
            } else {
                if (this.engine.getDesign() != null) {
                    // Rebind to design model
                    bindToModel(this.engine.getDesign());
                }
            }
        });

        this.engine.xmlSourceProperty().addListener((obs, oldVal, newVal) -> {
            if (this.engine.getViewMode() == com.jasperstudio.designer.DesignerEngine.ViewMode.SOURCE) {
                // Throttle updates? For now simple.
                rebuildXmlTree(newVal);
            }
        });
    }

    private void bindToModel(JasperDesignModel design) {
        // Helper to recover standard view
        if (design != null) {
            rebuildTree(design);
            design.getBands().removeListener(bandsListener); // avoid double add?
            design.getBands().addListener(bandsListener);
        }
    }

    // --- XML Implementation ---
    private void rebuildXmlTree(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            treeView.setRoot(null);
            return;
        }
        try {
            javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
            XmlTreeHandler handler = new XmlTreeHandler();
            parser.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")), handler);

            TreeItem<Object> root = handler.getRoot();
            if (root != null) {
                treeView.setRoot(root);
                root.setExpanded(true);
            }
        } catch (Exception e) {
            // ignore or show error
        }
    }

    private class XmlTreeHandler extends org.xml.sax.helpers.DefaultHandler {
        private java.util.Stack<TreeItem<Object>> stack = new java.util.Stack<>();
        private TreeItem<Object> root;
        private org.xml.sax.Locator locator;

        @Override
        public void setDocumentLocator(org.xml.sax.Locator locator) {
            this.locator = locator;
        }

        public TreeItem<Object> getRoot() {
            return root;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
                throws org.xml.sax.SAXException {
            XmlTag tag = new XmlTag(qName, locator.getLineNumber(), locator.getColumnNumber());
            TreeItem<Object> item = new TreeItem<>(tag);
            item.setExpanded(true);

            if (root == null) {
                root = item;
            } else {
                if (!stack.isEmpty()) {
                    stack.peek().getChildren().add(item);
                }
            }
            stack.push(item);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (!stack.isEmpty()) {
                TreeItem<Object> item = stack.pop();
                if (item.getValue() instanceof XmlTag) {
                    XmlTag tag = (XmlTag) item.getValue();
                    tag.endLine = locator.getLineNumber();
                    tag.endCol = locator.getColumnNumber();
                }
            }
        }
    }

    private class XmlTag {
        String name;
        int line;
        int col;
        int endLine = -1;
        int endCol = -1;

        public XmlTag(String name, int line, int col) {
            this.name = name;
            this.line = line;
            this.col = col;
        }

        @Override
        public String toString() {
            return "<" + name + ">";
        }
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
            return this.reportName;
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
                        addM.setOnAction(e -> OutlinePanel.this.engine.addBand(((BandPlaceholder) item).type));
                        cm.getItems().add(addM);
                        setContextMenu(cm);
                    } else if (item instanceof com.jasperstudio.model.BandModel) {
                        setStyle("-fx-font-weight: bold;");
                        com.jasperstudio.model.BandModel b = (com.jasperstudio.model.BandModel) item;

                        // Allow delete for all bands
                        javafx.scene.control.ContextMenu cm = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem delM = new javafx.scene.control.MenuItem("Delete Band");
                        delM.setOnAction(e -> OutlinePanel.this.engine.deleteBand(b.getType()));
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

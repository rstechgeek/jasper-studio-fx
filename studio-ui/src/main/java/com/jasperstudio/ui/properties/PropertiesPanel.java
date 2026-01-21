package com.jasperstudio.ui.properties;

import com.jasperstudio.designer.DesignerEngine;
import com.jasperstudio.model.ElementModel;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * A panel that displays properties for the selected element or band.
 */
public class PropertiesPanel extends VBox {

    private DesignerEngine engine;

    // Controls
    @javafx.fxml.FXML
    private Label lblUuid;
    @javafx.fxml.FXML
    private TextField tfUuid;

    @javafx.fxml.FXML
    private Label lblX;
    @javafx.fxml.FXML
    private Spinner<Integer> spX;

    @javafx.fxml.FXML
    private Label lblY;
    @javafx.fxml.FXML
    private Spinner<Integer> spY;

    @javafx.fxml.FXML
    private Label lblW;
    @javafx.fxml.FXML
    private Spinner<Integer> spW;

    @javafx.fxml.FXML
    private Label lblH;
    @javafx.fxml.FXML
    private Spinner<Integer> spH;

    @javafx.fxml.FXML
    private Spinner<Integer> spPageWidth;
    @javafx.fxml.FXML
    private Spinner<Integer> spPageHeight;
    @javafx.fxml.FXML
    private Spinner<Integer> spMarginLeft;
    @javafx.fxml.FXML
    private Spinner<Integer> spMarginRight;
    @javafx.fxml.FXML
    private Spinner<Integer> spMarginTop;
    @javafx.fxml.FXML
    private Spinner<Integer> spMarginBottom;

    @javafx.fxml.FXML
    private Label lblText;
    @javafx.fxml.FXML
    private TextField tfText;

    // Page Labels for visibility toggling
    @javafx.fxml.FXML
    private Label lblPageWidth;
    @javafx.fxml.FXML
    private Label lblPageHeight;
    @javafx.fxml.FXML
    private Label lblMarginLeft;
    @javafx.fxml.FXML
    private Label lblMarginRight;
    @javafx.fxml.FXML
    private Label lblMarginTop;
    @javafx.fxml.FXML
    private Label lblMarginBottom;

    // Band Controls
    @javafx.fxml.FXML
    private Label lblSplitType;
    @javafx.fxml.FXML
    private javafx.scene.control.ComboBox<net.sf.jasperreports.engine.type.SplitTypeEnum> cmbSplitType;
    @javafx.fxml.FXML
    private Label lblPrintWhen;
    @javafx.fxml.FXML
    private javafx.scene.control.TextArea taPrintWhen;

    @javafx.fxml.FXML
    private GridPane grid;

    private javafx.beans.value.ChangeListener<Object> selectionListener;

    public PropertiesPanel(DesignerEngine engine) {
        loadFXML();

        this.selectionListener = (obs, oldVal, newVal) -> {
            unbind(oldVal);
            bind(newVal);
        };

        setDesignerEngine(engine);
    }

    public void setDesignerEngine(DesignerEngine newEngine) {
        if (this.engine != null) {
            this.engine.selectionProperty().removeListener(selectionListener);
            // Unbind current selection if any
            unbind(this.engine.getSelection());
        }

        this.engine = newEngine;

        if (this.engine != null) {
            this.engine.selectionProperty().addListener(selectionListener);
            // Verify if we should bind current selection immediately
            if (this.engine.getSelection() != null) {
                bind(this.engine.getSelection());
            } else {
                // If nothing selected, maybe clear inputs?
                // setDisable(true); // handled in bind/unbind logic usually
                unbind(null); // Ensure clean state
            }
        } else {
            unbind(null);
            setDisable(true);
        }
    }

    private void loadFXML() {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("PropertiesPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load PropertiesPanel.fxml", e);
        }
    }

    // private void initUI() { ... } // Removed

    // private void makeEditable(Spinner<?> spinner) { ... } // Done in FXML

    private void hidePageControls(boolean hide) {
        boolean visible = !hide;
        spPageWidth.setVisible(visible);
        spPageWidth.setManaged(visible);
        lblPageWidth.setVisible(visible);
        lblPageWidth.setManaged(visible);

        spPageHeight.setVisible(visible);
        spPageHeight.setManaged(visible);
        lblPageHeight.setVisible(visible);
        lblPageHeight.setManaged(visible);

        spMarginLeft.setVisible(visible);
        spMarginLeft.setManaged(visible);
        lblMarginLeft.setVisible(visible);
        lblMarginLeft.setManaged(visible);

        spMarginRight.setVisible(visible);
        spMarginRight.setManaged(visible);
        lblMarginRight.setVisible(visible);
        lblMarginRight.setManaged(visible);

        spMarginTop.setVisible(visible);
        spMarginTop.setManaged(visible);
        lblMarginTop.setVisible(visible);
        lblMarginTop.setManaged(visible);

        spMarginBottom.setVisible(visible);
        spMarginBottom.setManaged(visible);
        lblMarginBottom.setVisible(visible);
        lblMarginBottom.setManaged(visible);
    }

    private void hideElementControls(boolean hide) {
        boolean visible = !hide;

        // UUID
        lblUuid.setVisible(visible);
        lblUuid.setManaged(visible);
        tfUuid.setVisible(visible);
        tfUuid.setManaged(visible);

        // Bounds (X, Y, W, H) - H might need independent control for Band
        lblX.setVisible(visible);
        lblX.setManaged(visible);
        spX.setVisible(visible);
        spX.setManaged(visible);

        lblY.setVisible(visible);
        lblY.setManaged(visible);
        spY.setVisible(visible);
        spY.setManaged(visible);

        lblW.setVisible(visible);
        lblW.setManaged(visible);
        spW.setVisible(visible);
        spW.setManaged(visible);

        lblH.setVisible(visible);
        lblH.setManaged(visible);
        spH.setVisible(visible);
        spH.setManaged(visible);

        if (hide) {
            lblText.setVisible(false);
            lblText.setManaged(false);
            tfText.setVisible(false);
            tfText.setManaged(false);
        }
    }

    private void setControlVisible(Label lbl, javafx.scene.control.Control ctrl, boolean visible) {
        if (lbl != null) {
            lbl.setVisible(visible);
            lbl.setManaged(visible);
        }
        if (ctrl != null) {
            ctrl.setVisible(visible);
            ctrl.setManaged(visible);
        }
    }

    private void hideBandControls(boolean hide) {
        boolean visible = !hide;
        lblSplitType.setVisible(visible);
        lblSplitType.setManaged(visible);
        cmbSplitType.setVisible(visible);
        cmbSplitType.setManaged(visible);

        lblPrintWhen.setVisible(visible);
        lblPrintWhen.setManaged(visible);
        taPrintWhen.setVisible(visible);
        taPrintWhen.setManaged(visible);
    }

    private ElementModel currentModel; // Keep for reference if needed, though mostly using listeners

    // Listeners references to allow unbinding
    private javafx.beans.value.ChangeListener<Integer> xUiListener;
    private javafx.beans.value.ChangeListener<Number> xModelListener;

    private javafx.beans.value.ChangeListener<Integer> yUiListener;
    private javafx.beans.value.ChangeListener<Number> yModelListener;

    private javafx.beans.value.ChangeListener<Integer> wUiListener;
    private javafx.beans.value.ChangeListener<Number> wModelListener;

    private javafx.beans.value.ChangeListener<Integer> hUiListener;
    private javafx.beans.value.ChangeListener<Number> hModelListener;

    private javafx.beans.value.ChangeListener<String> textUiListener;
    private javafx.beans.value.ChangeListener<String> textModelListener;

    // Band Listeners
    private javafx.beans.value.ChangeListener<net.sf.jasperreports.engine.type.SplitTypeEnum> splitTypeUiListener;
    private javafx.beans.value.ChangeListener<net.sf.jasperreports.engine.type.SplitTypeEnum> splitTypeModelListener;
    private javafx.beans.value.ChangeListener<String> printWhenUiListener;
    private javafx.beans.value.ChangeListener<String> printWhenModelListener;

    // Page Model Listeners
    private javafx.beans.value.ChangeListener<Number> pgWListener, pgHListener, mLListener, mRListener, mTListener,
            mBListener;
    private javafx.beans.value.ChangeListener<Integer> pgWUiListener, pgHUiListener, mLUiListener, mRUiListener,
            mTUiListener, mBUiListener;

    private void unbind(Object selection) {
        if (selection instanceof ElementModel) {
            ElementModel m = (ElementModel) selection;
            if (xUiListener != null)
                spX.valueProperty().removeListener(xUiListener);
            if (xModelListener != null)
                m.xProperty().removeListener(xModelListener);

            if (yUiListener != null)
                spY.valueProperty().removeListener(yUiListener);
            if (yModelListener != null)
                m.yProperty().removeListener(yModelListener);

            if (wUiListener != null)
                spW.valueProperty().removeListener(wUiListener);
            if (wModelListener != null)
                m.widthProperty().removeListener(wModelListener);

            if (hUiListener != null)
                spH.valueProperty().removeListener(hUiListener);
            if (hModelListener != null)
                m.heightProperty().removeListener(hModelListener);

            if (textUiListener != null)
                tfText.textProperty().removeListener(textUiListener);
        } else if (selection instanceof com.jasperstudio.model.BandModel) {
            com.jasperstudio.model.BandModel m = (com.jasperstudio.model.BandModel) selection;
            if (hUiListener != null)
                spH.valueProperty().removeListener(hUiListener);
            if (hModelListener != null)
                m.heightProperty().removeListener(hModelListener);

            if (splitTypeUiListener != null)
                cmbSplitType.valueProperty().removeListener(splitTypeUiListener);
            if (splitTypeModelListener != null)
                m.splitTypeProperty().removeListener(splitTypeModelListener);

            if (printWhenUiListener != null)
                taPrintWhen.textProperty().removeListener(printWhenUiListener);
            if (printWhenModelListener != null)
                m.printWhenExpressionTextProperty().removeListener(printWhenModelListener);

        } else if (selection instanceof com.jasperstudio.model.JasperDesignModel) {
            com.jasperstudio.model.JasperDesignModel m = (com.jasperstudio.model.JasperDesignModel) selection;
            if (pgWUiListener != null)
                spPageWidth.valueProperty().removeListener(pgWUiListener);
            if (pgWListener != null)
                m.pageWidthProperty().removeListener(pgWListener);

            if (pgHUiListener != null)
                spPageHeight.valueProperty().removeListener(pgHUiListener);
            if (pgHListener != null)
                m.pageHeightProperty().removeListener(pgHListener);

            if (mLUiListener != null)
                spMarginLeft.valueProperty().removeListener(mLUiListener);
            if (mLListener != null)
                m.leftMarginProperty().removeListener(mLListener);

            if (mRUiListener != null)
                spMarginRight.valueProperty().removeListener(mRUiListener);
            if (mRListener != null)
                m.rightMarginProperty().removeListener(mRListener);

            if (mTUiListener != null)
                spMarginTop.valueProperty().removeListener(mTUiListener);
            if (mTListener != null)
                m.topMarginProperty().removeListener(mTListener);

            if (mBUiListener != null)
                spMarginBottom.valueProperty().removeListener(mBUiListener);
            if (mBListener != null)
                m.bottomMarginProperty().removeListener(mBListener);
        }
        this.currentModel = null;
    }

    private void bind(Object selection) {
        if (selection instanceof ElementModel) {
            hidePageControls(true);
            hideBandControls(true);
            hideElementControls(false);

            ElementModel m = (ElementModel) selection;
            this.currentModel = m;

            tfUuid.setText(m.getId());

            // X
            spX.getValueFactory().setValue((int) m.getX());
            xUiListener = (o, old, v) -> engine.executeCommand(
                    new com.jasperstudio.descriptor.ChangePropertyCommand<>("X", old, v, val -> m.setX(val)));
            spX.valueProperty().addListener(xUiListener);
            xModelListener = (o, old, v) -> spX.getValueFactory().setValue(v.intValue());
            m.xProperty().addListener(xModelListener);

            // Y
            spY.getValueFactory().setValue((int) m.getY());
            yUiListener = (o, old, v) -> engine.executeCommand(
                    new com.jasperstudio.descriptor.ChangePropertyCommand<>("Y", old, v, val -> m.setY(val)));
            spY.valueProperty().addListener(yUiListener);
            yModelListener = (o, old, v) -> spY.getValueFactory().setValue(v.intValue());
            m.yProperty().addListener(yModelListener);

            // W
            spW.getValueFactory().setValue((int) m.getWidth());
            wUiListener = (o, old, v) -> engine.executeCommand(
                    new com.jasperstudio.descriptor.ChangePropertyCommand<>("Width", old, v, val -> m.setWidth(val)));
            spW.valueProperty().addListener(wUiListener);
            wModelListener = (o, old, v) -> spW.getValueFactory().setValue(v.intValue());
            m.widthProperty().addListener(wModelListener);

            // H
            spH.getValueFactory().setValue((int) m.getHeight());
            hUiListener = (o, old, v) -> engine.executeCommand(
                    new com.jasperstudio.descriptor.ChangePropertyCommand<>("Height", old, v, val -> m.setHeight(val)));
            spH.valueProperty().addListener(hUiListener);
            hModelListener = (o, old, v) -> spH.getValueFactory().setValue(v.intValue());
            m.heightProperty().addListener(hModelListener);

            // Handle Text
            net.sf.jasperreports.engine.design.JRDesignElement jr = m.getElement();
            if (jr instanceof net.sf.jasperreports.engine.design.JRDesignStaticText) {
                lblText.setVisible(true);
                lblText.setManaged(true);
                tfText.setVisible(true);
                tfText.setManaged(true);
                tfText.setText(((net.sf.jasperreports.engine.design.JRDesignStaticText) jr).getText());

                textUiListener = (o, old, v) -> engine
                        .executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>("Text", old, v,
                                val -> ((net.sf.jasperreports.engine.design.JRDesignStaticText) jr).setText(val)));
                tfText.textProperty().addListener(textUiListener);
            } else {
                lblText.setVisible(false);
                lblText.setManaged(false);
                tfText.setVisible(false);
                tfText.setManaged(false);
            }
            setDisable(false);

        } else if (selection instanceof com.jasperstudio.model.BandModel) {
            hidePageControls(true);
            // Hide standard Element controls (UUID, X, Y, W, H)
            hideElementControls(true);

            // Re-enable Height for Band
            setControlVisible(lblH, spH, true);

            // Show Band specific controls
            hideBandControls(false);

            com.jasperstudio.model.BandModel m = (com.jasperstudio.model.BandModel) selection;

            // Band Height
            spH.getValueFactory().setValue(m.getHeight());
            hUiListener = (o, old, v) -> engine.executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>(
                    "Band Height", old, v, val -> m.setHeight(val)));
            spH.valueProperty().addListener(hUiListener);
            hModelListener = (o, old, v) -> spH.getValueFactory().setValue(v.intValue());
            m.heightProperty().addListener(hModelListener);

            // Split Type
            cmbSplitType.setItems(javafx.collections.FXCollections
                    .observableArrayList(net.sf.jasperreports.engine.type.SplitTypeEnum.values()));
            cmbSplitType.setValue(m.getSplitType());
            splitTypeUiListener = (o, old, v) -> engine
                    .executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>("Split Type", old, v,
                            val -> m.setSplitType(val)));
            cmbSplitType.valueProperty().addListener(splitTypeUiListener);
            splitTypeModelListener = (o, old, v) -> cmbSplitType.setValue(v);
            m.splitTypeProperty().addListener(splitTypeModelListener);

            // Print When Expression
            taPrintWhen.setText(m.getPrintWhenExpressionText());
            printWhenUiListener = (o, old, v) -> engine
                    .executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>("Print When", old, v,
                            val -> m.setPrintWhenExpressionText(val)));
            taPrintWhen.textProperty().addListener(printWhenUiListener);
            printWhenModelListener = (o, old, v) -> taPrintWhen.setText(v);
            m.printWhenExpressionTextProperty().addListener(printWhenModelListener);

            setDisable(false);

        } else if (selection instanceof com.jasperstudio.model.JasperDesignModel) {
            hideElementControls(true);
            hideBandControls(true);
            hidePageControls(false);

            com.jasperstudio.model.JasperDesignModel m = (com.jasperstudio.model.JasperDesignModel) selection;

            // Page Width
            spPageWidth.getValueFactory().setValue(m.getPageWidth());
            pgWUiListener = (o, old, v) -> engine
                    .executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>("Page Width", old, v,
                            val -> m.pageWidthProperty().set(val)));
            spPageWidth.valueProperty().addListener(pgWUiListener);
            pgWListener = (o, old, v) -> spPageWidth.getValueFactory().setValue(v.intValue());
            m.pageWidthProperty().addListener(pgWListener);

            // Page Height
            spPageHeight.getValueFactory().setValue(m.getPageHeight());
            pgHUiListener = (o, old, v) -> engine
                    .executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>("Page Height", old, v,
                            val -> m.pageHeightProperty().set(val)));
            spPageHeight.valueProperty().addListener(pgHUiListener);
            pgHListener = (o, old, v) -> spPageHeight.getValueFactory().setValue(v.intValue());
            m.pageHeightProperty().addListener(pgHListener);

            // Left
            spMarginLeft.getValueFactory().setValue(m.leftMarginProperty().get());
            mLUiListener = (o, old, v) -> engine.executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>(
                    "Margin Left", old, v, val -> m.leftMarginProperty().set(val)));
            spMarginLeft.valueProperty().addListener(mLUiListener);
            mLListener = (o, old, v) -> spMarginLeft.getValueFactory().setValue(v.intValue());
            m.leftMarginProperty().addListener(mLListener);

            // Right
            spMarginRight.getValueFactory().setValue(m.rightMarginProperty().get());
            mRUiListener = (o, old, v) -> engine.executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>(
                    "Margin Right", old, v, val -> m.rightMarginProperty().set(val)));
            spMarginRight.valueProperty().addListener(mRUiListener);
            mRListener = (o, old, v) -> spMarginRight.getValueFactory().setValue(v.intValue());
            m.rightMarginProperty().addListener(mRListener);

            // Top
            spMarginTop.getValueFactory().setValue(m.topMarginProperty().get());
            mTUiListener = (o, old, v) -> engine.executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>(
                    "Margin Top", old, v, val -> m.topMarginProperty().set(val)));
            spMarginTop.valueProperty().addListener(mTUiListener);
            mTListener = (o, old, v) -> spMarginTop.getValueFactory().setValue(v.intValue());
            m.topMarginProperty().addListener(mTListener);

            // Bottom
            spMarginBottom.getValueFactory().setValue(m.bottomMarginProperty().get());
            mBUiListener = (o, old, v) -> engine.executeCommand(new com.jasperstudio.descriptor.ChangePropertyCommand<>(
                    "Margin Bottom", old, v, val -> m.bottomMarginProperty().set(val)));
            spMarginBottom.valueProperty().addListener(mBUiListener);
            mBListener = (o, old, v) -> spMarginBottom.getValueFactory().setValue(v.intValue());
            m.bottomMarginProperty().addListener(mBListener);

            setDisable(false);
        } else {
            setDisable(true);
        }
    }

    private boolean isUpdating = false;
}

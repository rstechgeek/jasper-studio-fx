package com.jasperstudio.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;

public class BandModel {
    private final JRDesignBand band;
    private final StringProperty type = new SimpleStringProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();
    private final ObservableList<ElementModel> elements = FXCollections.observableArrayList();

    private final javafx.beans.property.ObjectProperty<net.sf.jasperreports.engine.type.SplitTypeEnum> splitType = new javafx.beans.property.SimpleObjectProperty<>();
    private final StringProperty printWhenExpressionText = new SimpleStringProperty();

    public BandModel(String type, JRDesignBand band) {
        this.type.set(type);
        this.band = band;

        syncFromBand();
        setupListeners();
    }

    private void syncFromBand() {
        if (band != null) {
            this.height.set(band.getHeight());
            this.splitType.set(band.getSplitTypeValue());
            if (band.getPrintWhenExpression() != null) {
                this.printWhenExpressionText.set(band.getPrintWhenExpression().getText());
            } else {
                this.printWhenExpressionText.set("");
            }

            elements.clear();
            for (net.sf.jasperreports.engine.JRChild child : band.getChildren()) {
                if (child instanceof JRDesignElement) {
                    elements.add(new ElementModel((JRDesignElement) child));
                }
            }
        }
    }

    private void setupListeners() {
        this.height.addListener((obs, old, newVal) -> {
            if (band != null)
                band.setHeight(newVal.intValue());
        });
        this.splitType.addListener((obs, old, newVal) -> {
            if (band != null)
                band.setSplitType(newVal);
        });
        this.printWhenExpressionText.addListener((obs, old, newVal) -> {
            if (band != null) {
                if (newVal == null || newVal.trim().isEmpty()) {
                    band.setPrintWhenExpression(null);
                } else {
                    net.sf.jasperreports.engine.design.JRDesignExpression expr = new net.sf.jasperreports.engine.design.JRDesignExpression();
                    expr.setText(newVal);
                    band.setPrintWhenExpression(expr);
                }
            }
        });
    }

    public void addElement(ElementModel model) {
        if (band != null) {
            band.addElement(model.getElement());
            elements.add(model);
        }
    }

    public void removeElement(ElementModel model) {
        if (band != null) {
            band.removeElement(model.getElement());
            elements.remove(model);
        }
    }

    public JRDesignBand getBand() {
        return band;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getType() {
        return type.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public int getHeight() {
        return height.get();
    }

    public ObservableList<ElementModel> getElements() {
        return elements;
    }

    public void setHeight(int h) {
        height.set(h);
    }

    public javafx.beans.property.ObjectProperty<net.sf.jasperreports.engine.type.SplitTypeEnum> splitTypeProperty() {
        return splitType;
    }

    public net.sf.jasperreports.engine.type.SplitTypeEnum getSplitType() {
        return splitType.get();
    }

    public void setSplitType(net.sf.jasperreports.engine.type.SplitTypeEnum st) {
        splitType.set(st);
    }

    public StringProperty printWhenExpressionTextProperty() {
        return printWhenExpressionText;
    }

    public String getPrintWhenExpressionText() {
        return printWhenExpressionText.get();
    }

    public void setPrintWhenExpressionText(String text) {
        printWhenExpressionText.set(text);
    }
}

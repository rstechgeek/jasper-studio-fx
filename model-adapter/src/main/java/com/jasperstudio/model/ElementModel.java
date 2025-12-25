package com.jasperstudio.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.sf.jasperreports.engine.design.JRDesignElement;

/**
 * Wrapper for a generic layout element.
 */
public class ElementModel {

    private final JRDesignElement element;

    // Position & Size
    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();
    private final DoubleProperty width = new SimpleDoubleProperty();
    private final DoubleProperty height = new SimpleDoubleProperty();

    // Metadata
    private final StringProperty uuid = new SimpleStringProperty();

    public ElementModel(JRDesignElement element) {
        this.element = element;
        syncFromElement();
        setupListeners();
    }

    // Specific Properties
    private final StringProperty text = new SimpleStringProperty();

    private void syncFromElement() {
        this.x.set(element.getX());
        this.y.set(element.getY());
        this.width.set(element.getWidth());
        this.height.set(element.getHeight());
        if (element.getUUID() != null) {
            this.uuid.set(element.getUUID().toString());
        }

        // Static Text Support
        if (element instanceof net.sf.jasperreports.engine.design.JRDesignStaticText) {
            this.text.set(((net.sf.jasperreports.engine.design.JRDesignStaticText) element).getText());
        }
    }

    private void setupListeners() {
        this.x.addListener((obs, old, val) -> element.setX(val.intValue()));
        this.y.addListener((obs, old, val) -> element.setY(val.intValue()));
        this.width.addListener((obs, old, val) -> element.setWidth(val.intValue()));
        this.height.addListener((obs, old, val) -> element.setHeight(val.intValue()));

        // Text Listener
        this.text.addListener((obs, old, val) -> {
            if (element instanceof net.sf.jasperreports.engine.design.JRDesignStaticText) {
                ((net.sf.jasperreports.engine.design.JRDesignStaticText) element).setText(val);
            }
        });
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String t) {
        text.set(t);
    }

    public StringProperty uuidProperty() {
        return uuid;
    }

    public JRDesignElement getElement() {
        return element;
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public double getX() {
        return x.get();
    }

    public void setX(double v) {
        x.set(v);
    }

    public double getY() {
        return y.get();
    }

    public void setY(double v) {
        y.set(v);
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(double v) {
        width.set(v);
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double v) {
        height.set(v);
    }

    public String getId() {
        return uuid.get();
    }
}

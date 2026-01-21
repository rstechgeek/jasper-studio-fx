package com.jasperstudio.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.sf.jasperreports.engine.design.JasperDesign;

/**
 * A JavaFX-friendly wrapper around {@link JasperDesign}.
 * Acts as the ViewModel for the Report.
 */
public class JasperDesignModel {

    private final JasperDesign design;

    // Observable Properties
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty pageWidth = new SimpleIntegerProperty();
    private final IntegerProperty pageHeight = new SimpleIntegerProperty();
    private final IntegerProperty columnWidth = new SimpleIntegerProperty();

    // Margins
    private final IntegerProperty leftMargin = new SimpleIntegerProperty();
    private final IntegerProperty rightMargin = new SimpleIntegerProperty();
    private final IntegerProperty topMargin = new SimpleIntegerProperty();
    private final IntegerProperty bottomMargin = new SimpleIntegerProperty();

    public JasperDesignModel(JasperDesign design) {
        this.design = design;
        sync();
        setupListeners();
    }

    public void sync() {
        this.name.set(design.getName());
        this.pageWidth.set(design.getPageWidth());
        this.pageHeight.set(design.getPageHeight());
        this.columnWidth.set(design.getColumnWidth());
        this.leftMargin.set(design.getLeftMargin());
        this.rightMargin.set(design.getRightMargin());
        this.topMargin.set(design.getTopMargin());
        this.bottomMargin.set(design.getBottomMargin());

        if (design.getQuery() != null) {
            this.queryString.set(design.getQuery().getText());
        } else {
            this.queryString.set("");
        }

        // Sync Bands
        this.bands.clear();
        addBandToModel("Title", design.getTitle());
        addBandToModel("Page Header", design.getPageHeader());
        addBandToModel("Column Header", design.getColumnHeader());

        // Detail
        net.sf.jasperreports.engine.JRBand detail = null;
        if (design.getDetailSection() != null && design.getDetailSection().getBands() != null
                && design.getDetailSection().getBands().length > 0) {
            detail = design.getDetailSection().getBands()[0];
        }
        addBandToModel("Detail", detail);

        addBandToModel("Column Footer", design.getColumnFooter());
        addBandToModel("Page Footer", design.getPageFooter());
        addBandToModel("Last Page Footer", design.getLastPageFooter());
        addBandToModel("Summary", design.getSummary());
        addBandToModel("No Data", design.getNoData());
        addBandToModel("Background", design.getBackground());
    }

    private void addBandToModel(String type, net.sf.jasperreports.engine.JRBand band) {
        if (band instanceof net.sf.jasperreports.engine.design.JRDesignBand) {
            this.bands.add(new BandModel(type, (net.sf.jasperreports.engine.design.JRDesignBand) band));
        }
    }

    private void setupListeners() {
        this.name.addListener((obs, old, newVal) -> design.setName(newVal));
        this.pageWidth.addListener((obs, old, newVal) -> design.setPageWidth(newVal.intValue()));
        this.pageHeight.addListener((obs, old, newVal) -> design.setPageHeight(newVal.intValue()));
        this.columnWidth.addListener((obs, old, newVal) -> design.setColumnWidth(newVal.intValue()));
        this.leftMargin.addListener((obs, old, newVal) -> design.setLeftMargin(newVal.intValue()));
        this.rightMargin.addListener((obs, old, newVal) -> design.setRightMargin(newVal.intValue()));
        this.topMargin.addListener((obs, old, newVal) -> design.setTopMargin(newVal.intValue()));
        this.bottomMargin.addListener((obs, old, newVal) -> design.setBottomMargin(newVal.intValue()));

        this.queryString.addListener((obs, old, newVal) -> {
            net.sf.jasperreports.engine.design.JRDesignQuery query = new net.sf.jasperreports.engine.design.JRDesignQuery();
            query.setText(newVal);
            design.setQuery(query);
        });
    }

    public JasperDesign getDesign() {
        return design;
    }

    // Bands List
    private final javafx.collections.ObservableList<BandModel> bands = javafx.collections.FXCollections
            .observableArrayList();

    public javafx.collections.ObservableList<BandModel> getBands() {
        return bands;
    }

    // Legacy support (delegate to appropriate band or just keep for compatibility
    // if needed)
    public void addElement(ElementModel element) {
        // Default to Title if used directly
        BandModel title = getBand("Title");
        if (title != null)
            title.addElement(element);
    }

    public BandModel getBand(String type) {
        for (BandModel b : bands) {
            if (b.getType().equals(type))
                return b;
        }
        return null;
    }

    // Property Getters
    public int getPageWidth() {
        return pageWidth.get();
    }

    public int getPageHeight() {
        return pageHeight.get();
    }

    // Elements List (Keep for legacy direct access if needed, or remove? Keeping
    // for safely)
    private final javafx.collections.ObservableList<ElementModel> elements = javafx.collections.FXCollections
            .observableArrayList();

    public javafx.collections.ObservableList<ElementModel> getElements() {
        return elements;
    }

    // Property Getters
    public StringProperty nameProperty() {
        return name;
    }

    public IntegerProperty pageWidthProperty() {
        return pageWidth;
    }

    public IntegerProperty pageHeightProperty() {
        return pageHeight;
    }

    public IntegerProperty columnWidthProperty() {
        return columnWidth;
    }

    public IntegerProperty leftMarginProperty() {
        return leftMargin;
    }

    public IntegerProperty rightMarginProperty() {
        return rightMargin;
    }

    public IntegerProperty topMarginProperty() {
        return topMargin;
    }

    public IntegerProperty bottomMarginProperty() {
        return bottomMargin;
    }

    // Query Support
    private final StringProperty queryString = new SimpleStringProperty();

    public StringProperty queryStringProperty() {
        return queryString;
    }

    public String getQueryString() {
        return queryString.get();
    }

    public void setQueryString(String query) {
        this.queryString.set(query);
    }
}

package com.jasperstudio.model;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import java.io.File;

/**
 * Service for loading and saving JRXML files.
 */
public class JrxmlService {

    public JasperDesignModel load(File file) throws JRException {
        JasperDesign design = JRXmlLoader.load(file);
        return new JasperDesignModel(design);
    }

    public void save(JasperDesignModel model, File file) throws JRException {
        if (model == null || model.getDesign() == null) {
            throw new IllegalArgumentException("Cannot save null model");
        }
        JRXmlWriter.writeReport(model.getDesign(), file.getAbsolutePath(), "UTF-8");
    }

    public JasperDesignModel loadFromString(String xmlContent) throws JRException {
        try (java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(
                xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            JasperDesign design = JRXmlLoader.load(is);
            return new JasperDesignModel(design);
        } catch (java.io.IOException e) {
            throw new JRException("Failed to read XML content", e);
        }
    }

    public String saveToString(JasperDesignModel model) throws JRException {
        if (model == null || model.getDesign() == null) {
            throw new IllegalArgumentException("Cannot save null model");
        }
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        JRXmlWriter.writeReport(model.getDesign(), os, "UTF-8");
        return os.toString(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Serializes a single element by wrapping it in a dummy design.
     */
    public String serializeElement(net.sf.jasperreports.engine.design.JRDesignElement element) throws JRException {
        net.sf.jasperreports.engine.design.JasperDesign dummy = new net.sf.jasperreports.engine.design.JasperDesign();
        dummy.setName("ClipboardWrapper");
        net.sf.jasperreports.engine.design.JRDesignBand band = new net.sf.jasperreports.engine.design.JRDesignBand();
        band.setHeight(element.getHeight() + element.getY() + 100);

        // Clone element to safely add to dummy?
        // JRDesignElement is mutable. Clone it to avoid detaching from original parent
        // if we were to just add it.
        // Actually, for serialization, adding it temporarily is fine if we are careful,
        // but cloning is safer to avoid parent-check side effects.
        net.sf.jasperreports.engine.design.JRDesignElement clone = (net.sf.jasperreports.engine.design.JRDesignElement) element
                .clone();
        band.addElement(clone);
        dummy.setTitle(band);

        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        JRXmlWriter.writeReport(dummy, os, "UTF-8");
        return os.toString(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Deserializes a single element from a dummy design wrapper.
     */
    public net.sf.jasperreports.engine.design.JRDesignElement deserializeElement(String xml) throws JRException {
        try (java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(
                xml.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            net.sf.jasperreports.engine.design.JasperDesign dummy = JRXmlLoader.load(is);
            if (dummy.getTitle() != null && dummy.getTitle().getElements() != null
                    && dummy.getTitle().getElements().length > 0) {
                return (net.sf.jasperreports.engine.design.JRDesignElement) dummy.getTitle().getElements()[0];
            }
            return null;
        } catch (Exception e) {
            throw new JRException("Failed to deserialize element", e);
        }
    }
}

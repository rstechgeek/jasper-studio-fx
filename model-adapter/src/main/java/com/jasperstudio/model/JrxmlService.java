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
}

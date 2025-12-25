package com.jasperstudio.model;

import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JasperDesignModelTest {

    @Test
    public void testPropertySync() {
        JasperDesign mockDesign = new JasperDesign();
        mockDesign.setName("OldName");
        mockDesign.setPageWidth(500);

        JasperDesignModel model = new JasperDesignModel(mockDesign);

        // 1. Verify initial sync
        assertEquals("OldName", model.nameProperty().get());
        assertEquals(500, model.pageWidthProperty().get());

        // 2. Change property
        model.nameProperty().set("NewReport");
        model.pageWidthProperty().set(800);

        // 3. Verify POJO update
        assertEquals("NewReport", mockDesign.getName());
        assertEquals(800, mockDesign.getPageWidth());
    }
}

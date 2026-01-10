package com.jasperstudio.ai;

import com.jasperstudio.model.JasperDesignModel;

public interface AiService {
    String ask(String prompt);

    void analyzeDesign(JasperDesignModel design);
}

package com.jasperstudio.ai;

import com.jasperstudio.model.JasperDesignModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockAiService implements AiService {
    private static final Logger logger = LoggerFactory.getLogger(MockAiService.class);

    @Override
    public String ask(String prompt) {
        // Simple keyword-based responses
        String lower = prompt.toLowerCase();
        if (lower.contains("add") || lower.contains("create")) {
            return "To add elements, drag them from the Palette on the left.";
        } else if (lower.contains("band")) {
            return "Bands define the report structure. You can add bands like Title, Detail, etc.";
        } else if (lower.contains("error")) {
            return "Check the Log Panel at the bottom for detailed error messages.";
        }
        return "I'm a mock AI assistant. I can help with basic questions about the studio. Try asking 'how to add element'.";
    }

    @Override
    public void analyzeDesign(JasperDesignModel design) {
        logger.info("Analyzing design: {}", design.getDesign().getName());
        // No-op for mock
    }
}

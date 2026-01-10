package com.jasperstudio.ui.assistant;

import com.jasperstudio.designer.DesignerEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;

public class AssistantPanel extends BorderPane {

    private final DesignerEngine engine;
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();

    @FXML
    private ListView<ChatMessage> chatHistory;
    @FXML
    private TextArea inputField;
    @FXML
    private Button btnSend;

    private final com.jasperstudio.ai.AiService aiService;

    public AssistantPanel(DesignerEngine engine) {
        this.engine = engine;
        this.aiService = new com.jasperstudio.ai.MockAiService();
        loadFXML();
        initUI();
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AssistantPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AssistantPanel.fxml", e);
        }
    }

    private void initUI() {
        chatHistory.setItems(messages);
        chatHistory.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ChatMessage> call(ListView<ChatMessage> listView) {
                return new ChatCell();
            }
        });

        btnSend.setOnAction(e -> sendMessage());
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                sendMessage();
            }
        });

        // Initial welcome
        messages.add(new ChatMessage("System",
                "Hello! I am your JasperReport Assistant. How can I help you design your report today?", true));
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty())
            return;

        // User Message
        messages.add(new ChatMessage("User", text, false));
        inputField.clear();

        // AI Response
        // Run on background thread if blocking (Mock is fast so UI thread is fine for
        // now)
        processRequest(text);
    }

    private void processRequest(String text) {
        String response;
        if (text.toLowerCase().contains("analyze")) {
            if (engine.getDesign() != null) {
                aiService.analyzeDesign(engine.getDesign());
                response = "I have analyzed your design: " + engine.getDesign().getDesign().getName() + ". Looks good!";
            } else {
                response = "There is no design open to analyze.";
            }
        } else {
            response = aiService.ask(text);
        }

        messages.add(new ChatMessage("Assistant", response, true));
        chatHistory.scrollTo(messages.size() - 1);
    }

    // Helper classes
    public static class ChatMessage {
        public String sender;
        public String content;
        public boolean isSystem;

        public ChatMessage(String sender, String content, boolean isSystem) {
            this.sender = sender;
            this.content = content;
            this.isSystem = isSystem;
        }
    }

    private static class ChatCell extends ListCell<ChatMessage> {
        private final VBox layout = new VBox(5);
        private final Label senderLabel = new Label();
        private final Label msgLabel = new Label();

        public ChatCell() {
            layout.getChildren().addAll(senderLabel, msgLabel);
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(200); // approximate

            // Styling
            senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            msgLabel.setStyle("-fx-padding: 8; -fx-background-radius: 8;");
        }

        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                senderLabel.setText(item.sender);
                msgLabel.setText(item.content);

                if (item.isSystem) {
                    layout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    msgLabel.setStyle(
                            "-fx-padding: 8; -fx-background-radius: 8; -fx-background-color: #e0e0e0; -fx-text-fill: black;");
                    senderLabel.setTextFill(javafx.scene.paint.Color.GRAY);
                } else {
                    layout.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    msgLabel.setStyle(
                            "-fx-padding: 8; -fx-background-radius: 8; -fx-background-color: #007bff; -fx-text-fill: white;");
                    senderLabel.setTextFill(javafx.scene.paint.Color.DARKBLUE);
                }
                setGraphic(layout);
            }
        }
    }
}

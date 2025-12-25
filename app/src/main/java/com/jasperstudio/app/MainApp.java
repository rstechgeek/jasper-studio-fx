package com.jasperstudio.app;

import com.jasperstudio.ui.MainWorkspace;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Jasper Studio FX Application...");
        // initialize workspace
        MainWorkspace workspace = new MainWorkspace();

        Scene scene = new Scene(workspace, 1200, 800);

        // Load default CSS
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Jasper Studio FX - Revived");
        primaryStage.setScene(scene);
        primaryStage.show();
        logger.info("Main Window shown.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

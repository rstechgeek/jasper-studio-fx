package com.jasperstudio.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class GridSizeDialog extends Dialog<Pair<Integer, Integer>> {

    public GridSizeDialog(int currentX, int currentY) {
        setTitle("Grid Size");
        setHeaderText("Set the horizontal and vertical grid spacing.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField xSpacing = new TextField(String.valueOf(currentX));
        xSpacing.setPromptText("X Spacing");
        TextField ySpacing = new TextField(String.valueOf(currentY));
        ySpacing.setPromptText("Y Spacing");

        grid.add(new Label("Horizontal (X):"), 0, 0);
        grid.add(xSpacing, 1, 0);
        grid.add(new Label("Vertical (Y):"), 0, 1);
        grid.add(ySpacing, 1, 1);

        getDialogPane().setContent(grid);

        // Validation - enable/disable OK button
        Node okButton = getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(false);

        xSpacing.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!isValid(newValue) || !isValid(ySpacing.getText()));
        });
        ySpacing.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!isValid(newValue) || !isValid(xSpacing.getText()));
        });

        // Convert the result to a Pair<Integer, Integer> when OK is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int x = Integer.parseInt(xSpacing.getText());
                    int y = Integer.parseInt(ySpacing.getText());
                    return new Pair<>(x, y);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    private boolean isValid(String text) {
        try {
            int val = Integer.parseInt(text);
            return val >= 2 && val <= 1000; // reasonable bounds
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

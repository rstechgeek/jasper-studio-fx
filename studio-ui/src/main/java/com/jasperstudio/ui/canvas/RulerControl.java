package com.jasperstudio.ui.canvas;

import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class RulerControl extends Pane {

    private final javafx.beans.property.ObjectProperty<Orientation> orientation = new javafx.beans.property.SimpleObjectProperty<>(
            Orientation.HORIZONTAL);
    private final Canvas canvas;
    private double zoom = 1.0;
    private double offset = 0.0;

    // Ruler settings
    private static final double RULER_SIZE = 20.0;
    private static final int MAJOR_TICK_STEP = 100; // pixels at 100%
    private static final int MINOR_TICK_STEP = 10;
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color TICK_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Font FONT = new Font("Arial", 9);

    public RulerControl() {
        this(Orientation.HORIZONTAL);
    }

    public RulerControl(Orientation orientation) {
        this.orientation.set(orientation);
        this.canvas = new Canvas();
        getChildren().add(canvas);

        this.orientation.addListener((obs, old, newOri) -> configureLayout(newOri));
        configureLayout(this.orientation.get());

        // Redraw on resize
        widthProperty().addListener(o -> draw());
        heightProperty().addListener(o -> draw());
    }

    private void configureLayout(Orientation ori) {
        if (ori == Orientation.HORIZONTAL) {
            setPrefHeight(RULER_SIZE);
            setMinHeight(RULER_SIZE);
            setMaxHeight(RULER_SIZE);
            setPrefWidth(USE_COMPUTED_SIZE);
            setMinWidth(USE_COMPUTED_SIZE);
            setMaxWidth(Double.MAX_VALUE);

            canvas.heightProperty().bind(heightProperty());
            canvas.widthProperty().bind(widthProperty());
        } else {
            setPrefWidth(RULER_SIZE);
            setMinWidth(RULER_SIZE);
            setMaxWidth(RULER_SIZE);
            setPrefHeight(USE_COMPUTED_SIZE);
            setMinHeight(USE_COMPUTED_SIZE);
            setMaxHeight(Double.MAX_VALUE);

            canvas.widthProperty().bind(widthProperty());
            canvas.heightProperty().bind(heightProperty());
        }
        draw();
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public javafx.beans.property.ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        draw();
    }

    public void setOffset(double offset) {
        this.offset = offset;
        draw();
    }

    private void draw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        gc.setStroke(TICK_COLOR);
        gc.setFill(TEXT_COLOR);
        gc.setFont(FONT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP); // For Horizontal ruler numbers
        gc.setLineWidth(0.7);

        // Effective start/end in DOCUMENT coordinates
        // Canvas coordinate '0' corresponds to document '-offset' (if offset is shift)
        // Usually offset is positive scroll value, i.e. how much is hidden to the
        // left/top.
        // So screen 0 = offset in doc.

        if (orientation.get() == Orientation.HORIZONTAL) {
            drawHorizontal(gc, w, h);
        } else {
            drawVertical(gc, w, h);
        }

        // Border line
        gc.setStroke(Color.GRAY);
        if (orientation.get() == Orientation.HORIZONTAL) {
            gc.strokeLine(0, h - 0.5, w, h - 0.5);
        } else {
            gc.strokeLine(w - 0.5, 0, w - 0.5, h);
        }
    }

    private void drawHorizontal(GraphicsContext gc, double w, double h) {
        // We iterate pixels on screen and map to document units
        // Document Unit = (Screen Pixel + Offset) / Zoom
        // But simpler to iterate logic units?

        // Start Document Unit
        double startDoc = offset / zoom;
        double endDoc = (offset + w) / zoom;

        // Align start to nearest minor tick
        double startTick = Math.floor(startDoc / MINOR_TICK_STEP) * MINOR_TICK_STEP;

        for (double val = startTick; val < endDoc; val += MINOR_TICK_STEP) {
            // Position on Screen
            double screenX = (val * zoom) - offset;

            if (screenX < 0 || screenX > w)
                continue;

            boolean isMajor = Math.abs(val % MAJOR_TICK_STEP) < 0.001;

            double tickHeight = isMajor ? h * 0.5 : h * 0.25;
            // Draw tick from bottom up
            gc.strokeLine(screenX, h - tickHeight, screenX, h);

            if (isMajor) {
                gc.fillText(String.valueOf((int) val), screenX + 2, 0);
            }
        }
    }

    private void drawVertical(GraphicsContext gc, double w, double h) {
        double startDoc = offset / zoom;
        double endDoc = (offset + h) / zoom;

        double startTick = Math.floor(startDoc / MINOR_TICK_STEP) * MINOR_TICK_STEP;

        // Text configuration for Vertical
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.CENTER);

        for (double val = startTick; val < endDoc; val += MINOR_TICK_STEP) {
            double screenY = (val * zoom) - offset;

            if (screenY < 0 || screenY > h)
                continue;

            boolean isMajor = Math.abs(val % MAJOR_TICK_STEP) < 0.001;

            double tickWidth = isMajor ? w * 0.5 : w * 0.25;
            // Draw tick from right to left
            gc.strokeLine(w - tickWidth, screenY, w, screenY);

            if (isMajor) {
                // Rotate text? Or just draw simple
                // For vertical ruler, drawing non-rotated text is usually easier to read if
                // room permits
                // Or draw rotated -90
                gc.save();
                gc.translate(w - tickWidth - 2, screenY);
                gc.rotate(-90);
                gc.fillText(String.valueOf((int) val), 0, 0);
                gc.restore();
            }
        }
    }
}

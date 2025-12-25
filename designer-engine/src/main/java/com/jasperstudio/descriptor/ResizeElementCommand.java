package com.jasperstudio.descriptor;

import com.jasperstudio.model.ElementModel;

public class ResizeElementCommand implements Command {

    private final ElementModel element;
    private final int oldX, oldY, oldW, oldH;
    private final int newX, newY, newW, newH;

    public ResizeElementCommand(ElementModel element, int oldX, int oldY, int oldW, int oldH, int newX, int newY,
            int newW, int newH) {
        this.element = element;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldW = oldW;
        this.oldH = oldH;
        this.newX = newX;
        this.newY = newY;
        this.newW = newW;
        this.newH = newH;
    }

    @Override
    public void execute() {
        element.setX(newX);
        element.setY(newY);
        element.setWidth(newW);
        element.setHeight(newH);
    }

    @Override
    public void undo() {
        element.setX(oldX);
        element.setY(oldY);
        element.setWidth(oldW);
        element.setHeight(oldH);
    }

    @Override
    public String getName() {
        return "Resize Element";
    }
}

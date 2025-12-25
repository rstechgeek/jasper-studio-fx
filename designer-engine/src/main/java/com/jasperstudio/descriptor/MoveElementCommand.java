package com.jasperstudio.descriptor;

import com.jasperstudio.model.ElementModel;

public class MoveElementCommand implements Command {

    private final ElementModel element;
    private final int oldX, oldY;
    private final int newX, newY;

    public MoveElementCommand(ElementModel element, int oldX, int oldY, int newX, int newY) {
        this.element = element;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute() {
        element.setX(newX);
        element.setY(newY);
    }

    @Override
    public void undo() {
        element.setX(oldX);
        element.setY(oldY);
    }

    @Override
    public String getName() {
        return "Move Element";
    }
}

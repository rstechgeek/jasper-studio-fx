package com.jasperstudio.descriptor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.Stack;

/**
 * Manages the stack of executed commands for Undo/Redo functionality.
 */
public class HistoryManager {

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

    /**
     * Executes a new command and pushes it onto the history stack.
     * Clears the redo stack.
     */
    public void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        updateProperties();
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        updateProperties();
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;
        Command cmd = redoStack.pop();
        cmd.redo();
        undoStack.push(cmd);
        updateProperties();
    }

    private void updateProperties() {
        canUndo.set(!undoStack.isEmpty());
        canRedo.set(!redoStack.isEmpty());
    }

    public BooleanProperty canUndoProperty() {
        return canUndo;
    }

    public BooleanProperty canRedoProperty() {
        return canRedo;
    }
}

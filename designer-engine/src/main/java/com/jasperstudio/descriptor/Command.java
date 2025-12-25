package com.jasperstudio.descriptor;

/**
 * Interface for all executable and undoable commands in the designer.
 */
public interface Command {
    /**
     * Executes the command logic.
     */
    void execute();

    /**
     * Reverts the logic executed by this command.
     */
    void undo();

    /**
     * Re-executes the command (often just calls execute()).
     */
    default void redo() {
        execute();
    }

    /**
     * A friendly name for the command (e.g. "Move Element", "Resize").
     * Useful for UI tooltips "Undo Move Element".
     */
    String getName();
}

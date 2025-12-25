package com.jasperstudio.descriptor;

import java.util.function.Consumer;

public class ChangePropertyCommand<T> implements Command {

    private final String propertyName;
    private final T oldValue;
    private final T newValue;
    private final Consumer<T> setter;

    public ChangePropertyCommand(String propertyName, T oldValue, T newValue, Consumer<T> setter) {
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.setter = setter;
    }

    @Override
    public void execute() {
        setter.accept(newValue);
    }

    @Override
    public void undo() {
        setter.accept(oldValue);
    }

    @Override
    public String getName() {
        return "Change " + propertyName;
    }
}

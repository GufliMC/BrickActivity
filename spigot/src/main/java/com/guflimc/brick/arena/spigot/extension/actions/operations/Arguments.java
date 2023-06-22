package com.guflimc.brick.arena.spigot.extension.actions.operations;

import java.util.List;

class Arguments {

    private final List<Object> arguments;

    protected Arguments(List<Object> arguments) {
        this.arguments = List.copyOf(arguments);
    }

    public <T> T get(int index) {
        return (T) arguments.get(index);
    }
}
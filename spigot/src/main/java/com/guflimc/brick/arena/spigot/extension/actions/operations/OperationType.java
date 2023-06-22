package com.guflimc.brick.arena.spigot.extension.actions.operations;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface OperationType {

    Consumer<Object> compile(@NotNull String input, @NotNull Class<?>... sources);

}

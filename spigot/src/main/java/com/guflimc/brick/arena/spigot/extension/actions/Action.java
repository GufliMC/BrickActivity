package com.guflimc.brick.arena.spigot.extension.actions;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record Action(@NotNull String name, @NotNull Predicate<Object> condition, @NotNull Collection<Consumer<Object>> operations) {

    public Action(@NotNull String name,
                  @NotNull Predicate<Object> condition,
                  @NotNull Collection<Consumer<Object>> operations) {
        this.name = name;
        this.condition = condition;
        this.operations = Set.copyOf(operations);
    }

    public void run(@NotNull Object source) {
        if ( !condition.test(source) ) {
            return;
        }

        for ( Consumer<Object> op : operations ) {
            try {
                op.accept(source);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

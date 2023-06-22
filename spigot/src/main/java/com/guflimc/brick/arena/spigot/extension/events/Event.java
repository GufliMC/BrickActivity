package com.guflimc.brick.arena.spigot.extension.events;

import com.guflimc.brick.arena.spigot.extension.actions.Action;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public record Event(@NotNull Class<?> type, @NotNull Collection<Action> actions) {

    public Event(@NotNull Class<?> type, @NotNull Collection<Action> actions) {
        this.type = type;
        this.actions = Set.copyOf(actions);
    }

}

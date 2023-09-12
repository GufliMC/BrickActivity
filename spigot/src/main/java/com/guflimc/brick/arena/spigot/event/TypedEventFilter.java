package com.guflimc.brick.arena.spigot.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class TypedEventFilter<T extends Event> implements EventFilter {

    private final Class<T> type;
    private final Predicate<T> filter;

    public TypedEventFilter(@NotNull Class<T> type, @NotNull Predicate<T> filter) {
        this.type = type;
        this.filter = filter;
    }

    @Override
    public boolean test(@NotNull Event event) {
        return type.isInstance(event) && filter.test(type.cast(event));
    }
}

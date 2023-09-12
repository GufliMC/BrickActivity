package com.guflimc.brick.arena.spigot.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventFilter {

    boolean test(@NotNull Event event);

}

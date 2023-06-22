package com.guflimc.brick.arena.spigot.events;

import com.guflimc.brick.arena.spigot.domain.SpigotActivity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class ActivityEvent extends Event {

    private final SpigotActivity activity;

    public ActivityEvent(@NotNull SpigotActivity activity) {
        this.activity = activity;
    }

    public SpigotActivity activity() {
        return activity;
    }

}

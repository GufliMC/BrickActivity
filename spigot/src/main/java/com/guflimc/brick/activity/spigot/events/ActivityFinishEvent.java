package com.guflimc.brick.activity.spigot.events;

import com.guflimc.brick.activity.spigot.domain.Activity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ActivityFinishEvent extends ActivityEvent {

    public ActivityFinishEvent(Activity activity) {
        super(activity);
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

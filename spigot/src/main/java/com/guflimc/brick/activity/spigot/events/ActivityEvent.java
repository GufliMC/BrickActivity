package com.guflimc.brick.activity.spigot.events;

import com.guflimc.brick.activity.spigot.domain.Activity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class ActivityEvent extends Event {

    private final Activity activity;

    public ActivityEvent(@NotNull Activity activity) {
        this.activity = activity;
    }

    public Activity activity() {
        return activity;
    }

}

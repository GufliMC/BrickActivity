package com.guflimc.brick.arena.spigot.activity;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class ActivityGroup {

    private final Activity activity;

    public ActivityGroup(@NotNull Activity activity) {
        this.activity = activity;
    }

    public Collection<ActivityPlayer> members() {
        return activity.players().stream()
                .filter(player -> player.isMemberOf(this))
                .collect(Collectors.toUnmodifiableSet());
    }
}

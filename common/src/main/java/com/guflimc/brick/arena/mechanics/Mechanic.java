package com.guflimc.brick.arena.mechanics;

import com.guflimc.brick.arena.domain.Activity;
import org.jetbrains.annotations.NotNull;

public abstract class Mechanic<P> {

    protected final Activity<P> activity;

    public Mechanic(@NotNull Activity<P> activity) {
        this.activity = activity;
    }

    public abstract void enable();

    public abstract void disable();

}

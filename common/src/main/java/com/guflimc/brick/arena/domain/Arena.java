package com.guflimc.brick.arena.domain;

import com.guflimc.brick.math.common.geometry.pos3.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class Arena {

    private final Predicate<Location> contains;

    public Arena(@NotNull Predicate<Location> contains) {
        this.contains = contains;
    }

    public boolean contains(@NotNull Location location) {
        return contains.test(location);
    }
}

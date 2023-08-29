package com.guflimc.brick.arena.system.arena;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ArenaManager {

    private final AtomicReference<Arena> arena = new AtomicReference<>();

    public Optional<Arena> arena() {
        return Optional.ofNullable(arena.get());
    }

    public void setArena(@Nullable Arena arena) {
        this.arena.set(arena);
    }

}

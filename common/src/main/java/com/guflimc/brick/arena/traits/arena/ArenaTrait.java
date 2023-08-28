package com.guflimc.brick.arena.traits.arena;

import com.guflimc.brick.arena.domain.Arena;
import com.guflimc.brick.arena.traits.Trait;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ArenaTrait implements Trait {

    private final AtomicReference<Arena> arena = new AtomicReference<>();

    public Optional<Arena> arena() {
        return Optional.ofNullable(arena.get());
    }

    public void setArena(@Nullable Arena arena) {
        this.arena.set(arena);
    }

}

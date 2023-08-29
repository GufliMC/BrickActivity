package com.guflimc.brick.arena.activity;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Activity<P, PD> {

    private final Function<P, PD> factory;
    private final Map<P, PD> participants = new ConcurrentHashMap<>();

    public Activity(@NotNull Function<P, PD> factory) {
        this.factory = factory;
    }

    //

    public void add(@NotNull P player) {
        if ( participants.containsKey(player) ) {
            return;
        }

        participants.put(player, factory.apply(player));
    }

    public void remove(@NotNull P player) {
        participants.remove(player);
    }

    public Collection<P> players() {
        return Collections.unmodifiableCollection(participants.keySet());
    }

    public Optional<PD> data(@NotNull P player) {
        return Optional.ofNullable(participants.get(player));
    }

}

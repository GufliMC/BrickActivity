package com.guflimc.brick.arena;

import com.guflimc.brick.arena.traits.Trait;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public class Activity<P, PC> {

    private final Function<P, PC> factory;
    private final Map<P, PC> participants = new ConcurrentHashMap<>();

    private final Set<Trait> traits = new CopyOnWriteArraySet<>();

    private Activity(@NotNull Function<P, PC> factory) {
        this.factory = factory;
    }

    //

    public void add(@NotNull P participant) {
        if ( participants.containsKey(participant) ) {
            return;
        }

        participants.put(participant, factory.apply(participant));
    }

    public void remove(@NotNull P participant) {
        participants.remove(participant);
    }

    public Optional<PC> participant(@NotNull P participant) {
        return Optional.ofNullable(participants.get(participant));
    }

    public Collection<P> participants() {
        return Collections.unmodifiableCollection(participants.keySet());
    }

    //

    protected void add(@NotNull Trait trait) {
        traits.add(trait);
    }

    public <T> T trait(@NotNull Class<T> type) {
        return traits.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseThrow();
    }
}

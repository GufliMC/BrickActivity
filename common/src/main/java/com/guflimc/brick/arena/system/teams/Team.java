package com.guflimc.brick.arena.system.teams;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public interface Team {

    default <P> Collection<P> members(@NotNull TeamActivity<P, ?> activity) {
        return activity.players().stream()
                .filter(p -> activity.data(p).map(pd -> this == pd.team().get()).orElse(false))
                .collect(Collectors.toUnmodifiableSet());
    }

}

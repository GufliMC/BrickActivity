package com.guflimc.brick.arena.traits.teams;

import com.guflimc.brick.arena.Activity;
import com.guflimc.brick.arena.traits.Trait;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TeamsTrait<P> implements Trait {

    private final Activity<P, ? extends TeamHolder> activity;
    private final Set<Team> teams;

    private TeamsTrait(@NotNull Activity<P, ? extends TeamHolder> activity, @NotNull Set<Team> teams) {
        this.activity = activity;
        this.teams = teams;
    }

    public static <P> TeamsTrait<P> of(@NotNull Activity<P, ? extends TeamHolder> activity, @NotNull Team... teams) {
        return new TeamsTrait<>(activity, Set.of(teams));
    }

    public Collection<Team> teams() {
        return Collections.unmodifiableCollection(teams);
    }

    public Optional<Team> team(@NotNull P participant) {
        return activity.participant(participant)
                .map(TeamHolder::team)
                .map(AtomicReference::get);
    }

    public Collection<P> participants(@NotNull Team team) {
        return activity.participants().stream()
                .filter(p -> team(p).map(t -> t.equals(team)).orElse(false))
                .collect(Collectors.toUnmodifiableSet());
    }

}

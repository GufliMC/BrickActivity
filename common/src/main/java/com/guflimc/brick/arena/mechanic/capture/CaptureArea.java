package com.guflimc.brick.arena.mechanic.capture;

import com.guflimc.brick.arena.system.teams.Team;
import com.guflimc.brick.arena.system.teams.TeamActivity;
import com.guflimc.brick.arena.system.teams.TeamHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class CaptureArea<P> {

    private final TeamActivity<P, ?> activity;
    private final Predicate<P> contains;
    private final CaptureConfig config;

    private Team defender;
    private Team conquestor;
    private double progress;

    public CaptureArea(@NotNull TeamActivity<P, ?> activity, @NotNull Predicate<P> contains, @NotNull CaptureConfig config) {
        this.activity = activity;
        this.contains = contains;
        this.config = config;
    }

    // getters

    public final Optional<Team> defender() {
        return Optional.ofNullable(defender);
    }

    public final Optional<Team> conquestor() {
        return Optional.ofNullable(conquestor);
    }

    public final double progress() {
        return progress;
    }

    public final Collection<P> players() {
        return activity.players().stream()
                .filter(contains)
                .toList();
    }

    public final Collection<P> players(@NotNull Team team) {
        return activity.players().stream()
                .filter(contains)
                .filter(player -> activity.data(player).map(TeamHolder::team).map(AtomicReference::get).orElse(null) == team)
                .toList();
    }

    public final List<Team> majority() {
        Map<Team, Integer> counts = new HashMap<>();

        players().forEach(player -> {
            Team team = activity.data(player).map(TeamHolder::team).map(AtomicReference::get).orElse(null);
            if (team == null) return;
            counts.putIfAbsent(team, 0);
            counts.computeIfPresent(team, (id, i) -> i + 1);
        });

        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        return counts.entrySet().stream()
                .filter(entry -> entry.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();
    }

    // mutators

    public void update() {
        List<Team> majority = majority();
        if (majority.size() == 1) {
            update(majority.get(0));
        }
    }

    private void update(@NotNull Team conquestor) {
        // capture area is released -> given conquestor becomes active conquestor
        if ( this.progress == 0 && this.conquestor == null ) {
            this.conquestor = conquestor;
        }

        // the given conquestor is not the active conquestor -> progress on releasing
        if ( !this.conquestor.equals(conquestor) ) {
            release();
            return;
        }

        // the progress is already full -> return
        if ( this.progress == 1 ) {
            return;
        }

        // progress on capturing
        this.progress += config.capture();
        this.progress = Math.max(0, Math.min(this.progress, 1));

        // progress is full -> successful capture
        if ( this.progress == 1 && this.defender != this.conquestor ) {
            this.defender = this.conquestor;
        }
    }

    private void release() {
        // progress is already 0 -> return
        if ( this.progress == 0 ) {
            return;
        }

        // progress on releasing
        this.progress -= config.release();
        this.progress = Math.max(0, Math.min(this.progress, 1));

        // progress is 0 -> successful release
        if ( this.progress == 0 ) {
            this.conquestor = this.defender = null;
        }
    }

}

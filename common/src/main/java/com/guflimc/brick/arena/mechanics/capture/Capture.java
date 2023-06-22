package com.guflimc.brick.arena.mechanics.capture;

import com.guflimc.brick.arena.domain.Activity;
import com.guflimc.brick.arena.domain.Team;
import com.guflimc.brick.arena.mechanics.Mechanic;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class Capture<P> extends Mechanic<P> {

    private final Predicate<P> contains;
    private final CaptureTracker tracker;

    public Capture(@NotNull Activity<P> activity, @NotNull Predicate<P> contains, @NotNull CaptureConfig config) {
        super(activity);
        this.contains = contains;
        this.tracker = new CaptureTracker(config);
    }

    public final Collection<P> players() {
        return activity.players().stream()
                .filter(contains)
                .toList();
    }

    public final List<Team> majority() {
        Map<Team, Integer> counts = new HashMap<>();

        players().forEach(player -> {
            Team team = activity.team(player).orElse(null);
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

    protected final void update() {
        List<Team> majority = majority();
        if (majority.size() == 1) {
            tracker.update(majority.get(0));
        }
    }

}

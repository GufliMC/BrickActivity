package com.guflimc.brick.arena.timer;

import org.jetbrains.annotations.NotNull;

import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Timer {

    private final Collection<Milestone> milestones = new CopyOnWriteArraySet<>();

    private long seconds;

    public abstract void start();

    public abstract void stop();

    public final void on(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(t -> t == unit.getDuration().getSeconds() * value, runnable));
    }

    public final void every(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(t -> t % (unit.getDuration().getSeconds() * value) == 0, runnable));
    }

    public final void set(long value, @NotNull TemporalUnit unit) {
        seconds = unit.getDuration().getSeconds() * value;
        postTick();
    }

    public final long get(@NotNull TemporalUnit unit) {
        return unit.getDuration().getSeconds() * seconds;
    }

    protected final void tick() {
        seconds++;
        postTick();
    }

    private void postTick() {
        // milestones
        milestones.stream()
                .filter(milestone -> milestone.predicate().test(seconds))
                .forEach(milestone -> milestone.runnable().run());
    }

}

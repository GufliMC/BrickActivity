package com.guflimc.brick.arena.domain;

import org.jetbrains.annotations.NotNull;

import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Timer {

    private record Milestone(long seconds, Runnable runnable) {
    }

    private record RepeatingMilestone(long seconds, Runnable runnable) {
    }

    private final Collection<Milestone> milestones = new CopyOnWriteArraySet<>();
    private final Collection<RepeatingMilestone> repeatingMilestones = new CopyOnWriteArraySet<>();

    private long seconds;

    public abstract void start();

    public abstract void stop();

    public final void on(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(unit.getDuration().getSeconds() * value, runnable));
    }

    public final void every(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        repeatingMilestones.add(new RepeatingMilestone(unit.getDuration().getSeconds() * value, runnable));
    }

    public final void set(long value, @NotNull TemporalUnit unit) {
        seconds = unit.getDuration().getSeconds() * value;
        notifySubscriptions();
    }

    public final long get(@NotNull TemporalUnit unit) {
        return unit.getDuration().getSeconds() * seconds;
    }

    protected final void tick() {
        seconds++;
        notifySubscriptions();
    }

    private void notifySubscriptions() {
        for ( Milestone milestone : milestones ) {
            if ( milestone.seconds == seconds ) {
                milestone.runnable.run();
            }
        }

        for ( RepeatingMilestone milestone : repeatingMilestones ) {
            if ( seconds % milestone.seconds == 0 ) {
                milestone.runnable.run();
            }
        }
    }

}

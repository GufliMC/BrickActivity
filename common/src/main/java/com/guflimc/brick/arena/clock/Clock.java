package com.guflimc.brick.arena.clock;

import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Clock {

    private final Collection<Milestone> milestones = new CopyOnWriteArraySet<>();

    private final ClockDirection direction;
    private final long begin;

    private long state;

    protected Clock(long begin, @NotNull ClockDirection direction) {
        this.direction = direction;
        this.begin = begin;
    }

    protected Clock(long begin) {
        this(begin, ClockDirection.FORWARDS);
    }

    //

    public abstract void start();

    public abstract void stop();

    // callbacks

    public final void on(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(t -> t == unit.getDuration().getSeconds() * value, runnable));
    }

    public final void every(long value, @NotNull TemporalUnit unit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(t -> t % (unit.getDuration().getSeconds() * value) == 0, runnable));
    }

    // mutators

    public final void set(long value, @NotNull TemporalUnit unit) {
        state = unit.getDuration().getSeconds() * value;
        checkMilestones();
    }

    public final void reset() {
        state = begin;
        checkMilestones();
    }

    // getters

    public final long get(@NotNull TemporalUnit unit) {
        return state / unit.getDuration().getSeconds();
    }

    public final long get(@NotNull TemporalField field) {
        if ( !(field instanceof ChronoField cf) ) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }

        switch ( cf ) {
            case SECOND_OF_MINUTE:
                return state % 60;
            case SECOND_OF_DAY:
                return state % 86400;
            case MINUTE_OF_HOUR:
                return (state / 60) % 60;
            case MINUTE_OF_DAY:
                return (state / 60) % 1440;
            case HOUR_OF_AMPM:
                return (state / 3600) % 12;
            case CLOCK_HOUR_OF_AMPM:
                int ham = (int) (state / 3600) % 12;
                return (ham % 12 == 0 ? 12 : ham);
            case HOUR_OF_DAY:
                return (state / 3600) % 24;
            case CLOCK_HOUR_OF_DAY:
                int h = (int) (state / 3600);
                return (h == 0 ? 24 : h);
            case AMPM_OF_DAY:
                return ((state / 3600) % 24) / 12;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    // internal

    protected final void tick() {
        state += direction == ClockDirection.FORWARDS ? 1 : -1;
        checkMilestones();
    }

    private void checkMilestones() {
        // milestones
        milestones.stream()
                .filter(milestone -> milestone.predicate().test(state))
                .forEach(milestone -> milestone.runnable().run());
    }

}

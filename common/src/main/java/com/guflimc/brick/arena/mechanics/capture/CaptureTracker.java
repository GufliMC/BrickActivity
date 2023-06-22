package com.guflimc.brick.arena.mechanics.capture;

import com.guflimc.brick.arena.domain.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class CaptureTracker {

    private final CaptureConfig config;

    private Team owner;
    private Team contester;

    private double progress;

    public CaptureTracker(@NotNull CaptureConfig config) {
        this.config = config;
    }

    public Optional<Team> owner() {
        return Optional.ofNullable(owner);
    }

    public Optional<Team> contester() {
        return Optional.ofNullable(contester);
    }

    public double progress() {
        return progress;
    }

    //

    public void update(@NotNull Team contester) {
        // capture point is released -> given contester becomes active contester
        if ( this.progress == 0 && this.contester == null ) {
            this.contester = contester;
        }

        // the given contester is not the active contester -> work to releasing
        if ( !this.contester.equals(contester) ) {
            release();
            return;
        }

        // the progress is already full -> return
        if ( this.progress == 1 ) {
            return;
        }

        // work to capturing
        this.progress += config.capture();
        this.progress = Math.max(0, Math.min(this.progress, 1));

        // progress is full -> successful capture
        if ( this.progress == 1 && this.owner != this.contester ) {
            this.owner = this.contester;
        }
    }

    public void release() {
        // progress is already 0 -> return
        if ( this.progress == 0 ) {
            return;
        }

        // work on releasing
        this.progress -= config.release();
        this.progress = Math.max(0, Math.min(this.progress, 1));

        // progress is 0 -> successful release
        if ( this.progress == 0 ) {
            this.contester = this.owner = null;
        }
    }

}
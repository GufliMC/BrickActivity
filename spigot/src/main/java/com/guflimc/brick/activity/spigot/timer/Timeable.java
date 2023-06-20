package com.guflimc.brick.activity.spigot.timer;

import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public interface Timeable {

    void on(long value, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable);

    void on(long value, @NotNull ChronoUnit chronoUnit, @NotNull Runnable runnable);

    void every(long value, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable);

    void every(long value, @NotNull ChronoUnit chronoUnit, @NotNull Runnable runnable);

}

package com.guflimc.brick.activity.spigot.timer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class Timer implements Timeable {

    private final JavaPlugin plugin;

    private record Milestone(long seconds, Runnable runnable) { }
    private record RepeatingMilestone(long seconds, Runnable runnable) { }

    private final Collection<Milestone> milestones = new CopyOnWriteArraySet<>();
    private final Collection<RepeatingMilestone> repeatingMilestones = new CopyOnWriteArraySet<>();

    private long seconds;
    private BukkitTask task;

    public Timer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::run, 0L, 20L);
    }

    public void stop() {
        if ( task == null || task.isCancelled() ) {
            return;
        }
        task.cancel();
    }

    @Override
    public void on(long value, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(timeUnit.toSeconds(value), runnable));
    }

    @Override
    public void on(long value, @NotNull ChronoUnit chronoUnit, @NotNull Runnable runnable) {
        milestones.add(new Milestone(chronoUnit.getDuration().getSeconds() * value, runnable));
    }

    @Override
    public void every(long value, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable) {
        repeatingMilestones.add(new RepeatingMilestone(timeUnit.toSeconds(value), runnable));
    }

    @Override
    public void every(long value, @NotNull ChronoUnit chronoUnit, @NotNull Runnable runnable) {
        repeatingMilestones.add(new RepeatingMilestone(chronoUnit.getDuration().getSeconds() * value, runnable));
    }

    //

    private void run() {
        seconds++;

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

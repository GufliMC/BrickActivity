package com.guflimc.brick.arena.spigot.mechanics.capture;

import com.guflimc.brick.arena.mechanics.capture.Capture;
import com.guflimc.brick.arena.mechanics.capture.CaptureConfig;
import com.guflimc.brick.arena.spigot.domain.SpigotActivity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SpigotCapture extends Capture<Player> {

    private final SpigotActivity activity;
    private BukkitTask task;

    public SpigotCapture(@NotNull SpigotActivity activity,
                         @NotNull Predicate<Player> contains,
                         @NotNull CaptureConfig config) {
        super(activity, contains, config);
        this.activity = activity;
    }

    @Override
    public void enable() {
        if ( task != null && !task.isCancelled() ) {
            return;
        }
        task = activity.plugin().getServer().getScheduler().runTaskTimerAsynchronously(activity.plugin(), this::update, 0L, 20L);
    }

    @Override
    public void disable() {
        if ( task == null || task.isCancelled() ) {
            return;
        }
        task.cancel();
    }

}

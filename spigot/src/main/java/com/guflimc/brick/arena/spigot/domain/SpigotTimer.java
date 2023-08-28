package com.guflimc.brick.arena.spigot.domain;

import com.guflimc.brick.arena.timer.Timer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class SpigotTimer extends Timer {

    private final JavaPlugin plugin;
    private BukkitTask task;

    public SpigotTimer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if ( task != null && !task.isCancelled() ) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 0L, 20L);
    }

    @Override
    public void stop() {
        if ( task == null || task.isCancelled() ) {
            return;
        }
        task.cancel();
    }
}

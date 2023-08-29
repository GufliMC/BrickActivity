package com.guflimc.brick.arena.spigot.clock;

import com.guflimc.brick.arena.clock.Clock;
import com.guflimc.brick.arena.clock.ClockDirection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class SpigotClock extends Clock {

    private final JavaPlugin plugin;
    private BukkitTask task;

    public SpigotClock(@NotNull JavaPlugin plugin, long begin, @NotNull ClockDirection direction) {
        super(begin, direction);
        this.plugin = plugin;
    }

    public SpigotClock(@NotNull JavaPlugin plugin, long begin) {
        super(begin);
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

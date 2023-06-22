package com.guflimc.brick.arena.spigot.mechanics.nexus;

import com.guflimc.brick.arena.mechanics.nexus.Nexus;
import com.guflimc.brick.arena.mechanics.nexus.NexusConfig;
import com.guflimc.brick.arena.spigot.domain.EventSubscription;
import com.guflimc.brick.arena.spigot.domain.SpigotActivity;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import com.guflimc.brick.math.spigot.SpigotAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class SpigotNexus extends Nexus<Player> {

    private final SpigotActivity activity;
    private EventSubscription subscription;

    public SpigotNexus(@NotNull SpigotActivity activity, Location location, @NotNull NexusConfig config) {
        super(activity, location, config);
        this.activity = activity;
    }

    @Override
    public void enable() {
        if ( subscription != null ) {
            return;
        }
        subscription = activity.on(BlockBreakEvent.class)
                .condition(e -> e.getBlock().getLocation().equals(SpigotAdapter.adapt(location)))
                .handler(e -> {
                    e.setCancelled(true);
                    this.damage(e.getPlayer());
                }).subscribe();
    }

    @Override
    public void disable() {
        if ( subscription == null ) {
            return;
        }
        subscription.unsubscribe();
        subscription = null;
    }
}

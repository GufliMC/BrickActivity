package com.guflimc.brick.arena.spigot.mechanics.respawn;

import com.guflimc.brick.arena.mechanics.respawn.Respawn;
import com.guflimc.brick.arena.mechanics.respawn.RespawnConfig;
import com.guflimc.brick.arena.spigot.domain.EventSubscription;
import com.guflimc.brick.arena.spigot.domain.SpigotActivity;
import com.guflimc.brick.math.spigot.SpigotAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class SpigotRespawn extends Respawn<Player> {

    private final SpigotActivity activity;
    private EventSubscription subscription;

    public SpigotRespawn(@NotNull SpigotActivity activity, @NotNull RespawnConfig config) {
        super(activity, config);
        this.activity = activity;
    }

    @Override
    public void enable() {
        if (subscription != null) {
            return;
        }

        subscription = activity.on(PlayerRespawnEvent.class)
                .handler((e) -> {
                    e.setRespawnLocation(SpigotAdapter.adapt(spawn(e.getPlayer())));
                })
                .subscribe();
    }

    @Override
    public void disable() {
        if (subscription == null) {
            return;
        }
        subscription.unsubscribe();
        subscription = null;
    }
}

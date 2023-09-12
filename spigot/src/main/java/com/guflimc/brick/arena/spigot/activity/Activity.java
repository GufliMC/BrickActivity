package com.guflimc.brick.arena.spigot.activity;

import com.guflimc.brick.arena.spigot.event.EventFilter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class Activity {

    final JavaPlugin plugin;

    final Collection<ActivityPlayer> players = new CopyOnWriteArraySet<>();

    public Activity(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Branch branch() {
        return new Branch(this);
    }

    // participants

    public void join(@NotNull ActivityPlayer player) {
        if ( players.stream().anyMatch(p -> p.id().equals(player.id())) ) {
            throw new IllegalArgumentException("Player with the same id already joined");
        }
        players.add(player);
    }

    public void quit(@NotNull ActivityPlayer player) {
        players.remove(player);
    }

    public Collection<ActivityPlayer> players() {
        return players;
    }

    public Optional<ActivityPlayer> player(@NotNull UUID id) {
        return players.stream()
                .filter(p -> p.id().equals(id))
                .findFirst();
    }

    public Optional<ActivityPlayer> player(@NotNull Player player) {
        return player(player.getUniqueId());
    }

    // event filter

    private final EventFilter filter = event -> {

        // ignore events that are related to a player not in this activity
        Player player = null;
        if (event instanceof PlayerEvent pe ) {
            player = pe.getPlayer();
        }
        else if ( event instanceof EntityEvent ee && ee.getEntity() instanceof Player p ) {
            player = p;
        }
        else if ( event instanceof EntityDamageByEntityEvent edbee && edbee.getDamager() instanceof Player p ) {
            player = p;
        }
        else if ( event instanceof EntityCombustByEntityEvent ecbee && ecbee.getCombuster() instanceof Player p) {
            player = p;
        }
        else if ( event instanceof InventoryInteractEvent iie && iie.getWhoClicked() instanceof Player p ) {
            player = p;
        }
        else if ( event instanceof BlockPlaceEvent bpe ) {
            player = bpe.getPlayer();
        }
        else if ( event instanceof BlockBreakEvent bbe ) {
            player = bbe.getPlayer();
        }
        if ( player != null && players.stream().map(p -> p.player().orElse(null)).filter(Objects::nonNull).noneMatch(player::equals) ) {
            return false;
        }

        // ignore events that are related to a block not in this activity
        // TODO

        return true;
    };

    EventFilter filter() {
        return filter;
    }

}

package com.guflimc.brick.arena.spigot.activity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class ActivityPlayer {

    private final UUID id;
    private Player player;

    private final Collection<ActivityGroup> groups = new CopyOnWriteArraySet<>();

    public ActivityPlayer(@NotNull UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    // player

    public Optional<Player> player() {
        return Optional.ofNullable(player);
    }

    public void connect(@NotNull Player player) {
        this.player = player;
    }

    public void disconnect() {
        this.player = null;
    }

    // group

    public Collection<ActivityGroup> groups() {
        return groups;
    }

    public void join(@NotNull ActivityGroup group) {
        groups.add(group);
    }

    public void quit(@NotNull ActivityGroup group) {
        groups.remove(group);
    }

    public boolean isMemberOf(@NotNull ActivityGroup group) {
        return groups.contains(group);
    }
}

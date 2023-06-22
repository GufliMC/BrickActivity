package com.guflimc.brick.arena.mechanics.nexus;

import com.guflimc.brick.arena.domain.Activity;
import com.guflimc.brick.arena.domain.Team;
import com.guflimc.brick.arena.mechanics.Mechanic;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import org.jetbrains.annotations.NotNull;

public abstract class Nexus<P> extends Mechanic<P> {

    protected final Location location;
    protected final NexusConfig config;

    private int health;

    public Nexus(@NotNull Activity<P> activity, Location location, @NotNull NexusConfig config) {
        super(activity);
        this.location = location;
        this.config = config;
        this.health = config.health();
    }

    public int health() {
        return health;
    }

    public void damage(int damage) {
        health -= damage;
        health = Math.max(0, health);
    }

    public void damage(@NotNull P player) {
        Team team = activity.team(player).orElse(null);
        if ( team == null ) {
            return;
        }

        if ( !config.attackers().contains(team) ) {
            return;
        }

        damage(1);
    }

}

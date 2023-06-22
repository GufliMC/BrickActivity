package com.guflimc.brick.arena.mechanics.respawn;

import com.guflimc.brick.arena.domain.Activity;
import com.guflimc.brick.arena.domain.Team;
import com.guflimc.brick.arena.mechanics.Mechanic;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Respawn<P> extends Mechanic<P> {

    private final RespawnConfig config;
    private final Random random = new Random();

    public Respawn(@NotNull Activity<P> activity, @NotNull RespawnConfig config) {
        super(activity);
        this.config = config;
    }

    public Location spawn(@NotNull Team team) {
        if ( !config.spawns().containsKey(team) ) {
            throw new IllegalArgumentException("Team " + team + " has no spawns");
        }
        Collection<Location> spawns = config.spawns().get(team);
        if ( spawns.isEmpty() ) {
            throw new IllegalArgumentException("Team " + team + " has no spawns");
        }

        return spawns.stream().skip(random.nextInt(spawns.size()))
                .findFirst().orElse(null);
    }

    public Location spawn(@NotNull P player) {
        Team team = activity.team(player).orElse(null);
        if ( team == null ) {
            throw new IllegalArgumentException("Player " + player + " has no team");
        }
        return spawn(team);
    }

}

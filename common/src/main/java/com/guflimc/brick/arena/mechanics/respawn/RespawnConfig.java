package com.guflimc.brick.arena.mechanics.respawn;

import com.guflimc.brick.arena.domain.Team;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record RespawnConfig(@NotNull Map<Team, Collection<Location>> spawns) {

    public RespawnConfig(@NotNull Map<Team, Collection<Location>> spawns) {
        this.spawns = Map.copyOf(spawns);
    }

    //

    public static class Builder {

        private final Map<Team, Collection<Location>> spawns = new HashMap<>();

        public final void add(@NotNull Team team, @NotNull Collection<Location> spawns) {
            this.spawns.computeIfAbsent(team, (t) -> new HashSet<>());
            this.spawns.get(team).addAll(spawns);
        }

        public final void add(@NotNull Team team, @NotNull Location... spawns) {
            add(team, Arrays.asList(spawns));
        }

        public RespawnConfig build() {
            return new RespawnConfig(spawns);
        }
    }

}

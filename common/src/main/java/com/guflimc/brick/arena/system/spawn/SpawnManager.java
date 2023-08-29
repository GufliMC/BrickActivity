package com.guflimc.brick.arena.system.spawn;

import com.guflimc.brick.arena.system.teams.Team;
import com.guflimc.brick.arena.system.teams.TeamActivity;
import com.guflimc.brick.math.common.geometry.pos3.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SpawnManager<P> {

    private final TeamActivity<P, ?> ts;
    private final Map<Team, Collection<Location>> spawns;

    private final Random random = new Random();

    public SpawnManager(@NotNull TeamActivity<P, ?> ts, @NotNull Map<Team, Collection<Location>> spawns) {
        this.ts = ts;
        spawns.replaceAll((team, coll) -> Set.copyOf(coll));
        this.spawns = Map.copyOf(spawns);
    }

    public Collection<Location> spawns(@NotNull Team team) {
        if ( !spawns.containsKey(team) ) {
            throw new IllegalArgumentException("Team " + team + " has no spawns");
        }
        return spawns.get(team);
    }

    public Location spawn(@NotNull Team team) {
        return spawns(team).stream()
                .skip(random.nextInt(spawns.size()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Team " + team + " has no spawns"));
    }

    public Location spawn(@NotNull P player) {
        return ts.data(player)
                .map(pd -> pd.team().get())
                .map(this::spawn)
                .orElseThrow(() -> new IllegalArgumentException("Player " + player + " has no team"));
    }

}

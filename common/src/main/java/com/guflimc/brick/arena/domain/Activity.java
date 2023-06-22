package com.guflimc.brick.arena.domain;

import com.guflimc.brick.arena.domain.storage.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Activity<P> {

    private static class PlayerContainer {
        private final Attributes attributes = new Attributes();
        private Team team;
    }

    private static class TeamContainer {
        private final Attributes attributes = new Attributes();
    }

    private final Map<Team, TeamContainer> teams = new ConcurrentHashMap<>();
    private final Map<P, PlayerContainer> players = new ConcurrentHashMap<>();

    private final Collection<Arena> arenas = new CopyOnWriteArraySet<>();

    //

    public void add(@NotNull Team team) {
        teams.put(team, new TeamContainer());
    }

    public void add(@NotNull P player) {
        players.put(player, new PlayerContainer());
    }

    public void remove(@NotNull P player) {
        players.remove(player);
    }

    public void set(@NotNull P player, Team team) {
        if (!players.containsKey(player)) {
            throw new IllegalArgumentException("Player not in activity");
        }
        if (team != null && !teams.containsKey(team)) {
            throw new IllegalArgumentException("Team not in activity");
        }
        players.get(player).team = team;
    }

    //

    public Collection<Team> teams() {
        return Collections.unmodifiableCollection(teams.keySet());
    }

    public Optional<Team> team(@NotNull P player) {
        if (!players.containsKey(player)) {
            throw new IllegalArgumentException("Player not in activity");
        }
        return Optional.ofNullable(players.get(player).team);
    }

    public Collection<P> players() {
        return Collections.unmodifiableCollection(players.keySet());
    }

    public Collection<P> players(@NotNull Team team) {
        return players.entrySet().stream()
                .filter((e) -> team.equals(e.getValue().team))
                .map(Map.Entry::getKey)
                .toList();
    }

    //

    public Attributes attributes(@NotNull P player) {
        if (!players.containsKey(player)) {
            throw new IllegalArgumentException("Player not in activity");
        }
        return players.get(player).attributes;
    }

    public Attributes attributes(@NotNull Team team) {
        if (!teams.containsKey(team)) {
            throw new IllegalArgumentException("Team not in activity");
        }
        return teams.get(team).attributes;
    }

    //

    public Collection<Arena> arenas() {
        return Collections.unmodifiableCollection(arenas);
    }

    public void add(@NotNull Arena arena) {
        arenas.add(arena);
    }

    public void remove(@NotNull Arena arena) {
        arenas.remove(arena);
    }

}

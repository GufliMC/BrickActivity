package com.guflimc.brick.activity.spigot.domain;

import com.guflimc.brick.activity.spigot.events.ActivityFinishEvent;
import com.guflimc.brick.activity.spigot.extension.actions.Action;
import com.guflimc.brick.activity.spigot.timer.Timeable;
import com.guflimc.brick.activity.spigot.timer.Timer;
import com.guflimc.brick.regions.api.domain.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Activity {

    private static class PlayerContainer {
        private final Attributes attributes = new Attributes();
        private Team team;
    }

    private static class TeamContainer {
        private final Attributes attributes = new Attributes();
    }

    final JavaPlugin plugin;

    private final Map<Team, TeamContainer> teams = new ConcurrentHashMap<>();
    private final Map<Player, PlayerContainer> players = new ConcurrentHashMap<>();

    final Collection<EventSubscription> subscriptions = new CopyOnWriteArraySet<>();

    private final Timer timer;

    private Region arena;
    private Team winner;

    public Activity(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.timer = new Timer(plugin);
        this.timer.start();
    }

    //

    public void add(@NotNull Team team) {
        if (finished()) {
            throw new IllegalStateException("Activity is finished");
        }
        teams.put(team, new TeamContainer());
    }

    public void add(@NotNull Player player) {
        if (finished()) {
            throw new IllegalStateException("Activity is finished");
        }
        players.put(player, new PlayerContainer());
    }

    public void remove(@NotNull Player player) {
        if (finished()) {
            throw new IllegalStateException("Activity is finished");
        }
        players.remove(player);
    }

    public void set(@NotNull Player player, Team team) {
        if (finished()) {
            throw new IllegalStateException("Activity is finished");
        }
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

    public Optional<Team> team(@NotNull Player player) {
        if (!players.containsKey(player)) {
            throw new IllegalArgumentException("Player not in activity");
        }
        return Optional.ofNullable(players.get(player).team);
    }

    public Collection<Player> players() {
        return Collections.unmodifiableCollection(players.keySet());
    }

    public Collection<Player> players(@NotNull Team team) {
        return players.entrySet().stream()
                .filter((e) -> team.equals(e.getValue().team))
                .map(Map.Entry::getKey)
                .toList();
    }

    //

    public Attributes attributes(@NotNull Player player) {
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

    public void arena(@NotNull Region region) {
        if (finished()) {
            throw new IllegalStateException("Activity is finished");
        }
        this.arena = region;
    }

    public Optional<Region> arena() {
        return Optional.ofNullable(arena);
    }

    //

    public <T extends Event> EventSubscription.Builder<T> on(@NotNull Class<T> eventClass) {
        return new ActivityEventSubscription.Builder<>(this, eventClass);
    }

    public void on(@NotNull Listener listener) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(EventHandler.class)) {
                continue;
            }
            if (m.getParameterCount() != 1) {
                continue;
            }

            Class<?> type = m.getParameters()[0].getType();
            if (!Event.class.isAssignableFrom(type)) {
                continue;
            }

            EventHandler settings = m.getAnnotation(EventHandler.class);
            Class<? extends Event> eventClass = type.asSubclass(Event.class);

            on(eventClass)
                    .priority(settings.priority())
                    .condition((e) -> !settings.ignoreCancelled() || !(e instanceof Cancellable c) || !c.isCancelled())
                    .handler((e, s) -> {
                        try {
                            m.invoke(listener, e);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .subscribe();
        }
    }

    public <T extends Event> EventSubscription.Builder<T> on(@NotNull Class<T> eventClass, @NotNull Action... actions) {
        return new ActivityEventSubscription.Builder<>(this, eventClass)
                .handler((e) -> {

                });
    }

    public class ActionSource<T extends Event> {

        private final Activity activity;
        private final T event;

    }

    //

    public Timeable timer() {
        return timer;
    }

    //

    public boolean finished() {
        return winner != null;
    }

    public void finish(@NotNull Team winner) {
        if (finished()) {
            throw new IllegalStateException("Activity already finished");
        }
        if (!teams.containsKey(winner)) {
            throw new IllegalArgumentException("Team not in activity");
        }

        this.winner = winner;
        plugin.getServer().getPluginManager().callEvent(new ActivityFinishEvent(this));

        Set.copyOf(subscriptions).forEach(EventSubscription::unsubscribe);
    }

    public Team winner() {
        if (!finished()) {
            throw new IllegalStateException("Activity not finished");
        }
        return winner;
    }

}

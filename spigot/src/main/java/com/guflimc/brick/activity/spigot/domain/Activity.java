package com.guflimc.brick.activity.spigot.domain;

import com.guflimc.brick.activity.spigot.events.ActivityEvent;
import com.guflimc.brick.activity.spigot.events.ActivityFinishEvent;
import com.guflimc.brick.math.spigot.SpigotMath;
import com.guflimc.brick.regions.api.domain.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class Activity {

    private static class PlayerContainer {
        private final Attributes attributes = new Attributes();
        private Team team;
    }

    private static class TeamContainer {
        private final Attributes attributes = new Attributes();
    }

    private final JavaPlugin plugin;

    private final Map<Team, TeamContainer> teams = new ConcurrentHashMap<>();
    private final Map<Player, PlayerContainer> players = new ConcurrentHashMap<>();

    private final Collection<EventSubscription> subscriptions = new CopyOnWriteArraySet<>();

    private Region arena;

    private Team winner;

    public Activity(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
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

    //

    public <T extends Event> EventSubscription.Builder<T> on(@NotNull Class<T> eventClass) {
        return new ActivityEventSubscriptionBuilder<>(this, eventClass);
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

    private static class ActivityEventSubscriptionBuilder<T extends Event> extends EventSubscription.Builder<T> {

        private final Activity activity;
        private final Class<T> eventClass;

        private ActivityEventSubscriptionBuilder(@NotNull Activity activity, @NotNull Class<T> eventClass) {
            this.activity = activity;
            this.eventClass = eventClass;
        }

        @Override
        public EventSubscription subscribe() {
            if (handler == null) {
                throw new IllegalStateException("Handler not set");
            }
            ActivityEventSubscription<T> sub = new ActivityEventSubscription<>(activity, eventClass, handler, until, conditions);
            activity.plugin.getServer().getPluginManager().registerEvent(eventClass, sub, priority, sub, activity.plugin);
            return sub;
        }
    }

    private static class ActivityEventSubscription<T extends Event> extends EventSubscription implements Listener, EventExecutor {

        private final Activity activity;
        private final Class<T> eventClass;

        private final BiConsumer<T, EventSubscription> handler;
        private final Collection<Predicate<T>> until;
        private final Collection<Predicate<T>> conditions;

        private ActivityEventSubscription(@NotNull Activity activity,
                                          @NotNull Class<T> eventClass,
                                          @NotNull BiConsumer<T, EventSubscription> handler,
                                          @NotNull Collection<Predicate<T>> until,
                                          @NotNull Collection<Predicate<T>> conditions) {
            this.activity = activity;
            this.eventClass = eventClass;
            this.handler = handler;
            this.until = Set.copyOf(until);
            this.conditions = Set.copyOf(conditions);
        }

        @Override
        public void unsubscribe() {
            activity.subscriptions.remove(this);
            try {
                // unfortunately we can't cache this reflect call, as the method is static
                Method getHandlerListMethod = eventClass.getMethod("getHandlerList");
                HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
                handlerList.unregister(this);
            } catch (Throwable t) {
                // ignored
            }
        }

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) {
            if (!eventClass.isInstance(event)) {
                return;
            }

            T ev = eventClass.cast(event);

            // activity conditions
            if (event instanceof ActivityEvent ae && !activity.equals(ae.activity())) {
                return;
            }
            if (event instanceof PlayerEvent ep && !activity.players.containsKey(ep.getPlayer())) {
                return;
            }
            if (event instanceof EntityEvent ee && !activity.arena().map((a) -> a.contains(SpigotMath.toBrickLocation(ee.getEntity().getLocation()))).orElse(false)) {
                return;
            }
            if (event instanceof BlockEvent be && !activity.arena().map((a) -> a.contains(SpigotMath.toBrickLocation(be.getBlock().getLocation()))).orElse(false)) {
                return;
            }

            if (!conditions.stream().allMatch((c) -> c.test(ev))) {
                return;
            }

            if (!until.stream().allMatch((u) -> u.test(ev))) {
                unsubscribe();
                return;
            }

            handler.accept(ev, this);
        }
    }
}

package com.guflimc.brick.arena.spigot.subscription;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class EventSubscription<T extends Event> implements Listener, EventExecutor {

    private final Consumer<EventSubscription<T>> unsubscribe;

    private final Class<T> eventClass;
    private final BiConsumer<T, EventSubscription<T>> handler;
    private final Collection<Predicate<T>> until;
    private final Collection<Predicate<T>> conditions;
    private final Collection<Predicate<Event>> filters;

    private EventSubscription(@NotNull Consumer<EventSubscription<T>> unsubscribe,
                              @NotNull Class<T> eventClass,
                              @NotNull BiConsumer<T, EventSubscription<T>> handler,
                              @NotNull Collection<Predicate<T>> until,
                              @NotNull Collection<Predicate<T>> conditions,
                              @NotNull Collection<Predicate<Event>> filters) {
        this.unsubscribe = unsubscribe;

        this.eventClass = eventClass;
        this.handler = handler;
        this.until = Set.copyOf(until);
        this.conditions = Set.copyOf(conditions);
        this.filters = filters;
    }

    public void unsubscribe() {
        unsubscribe.accept(this);
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
    public void execute(@NotNull Listener listener, @NotNull Event e) {
        if (!eventClass.isInstance(e)) {
            return;
        }

        if (!filters.stream().allMatch((f) -> f.test(e))) {
            return;
        }

        T event = eventClass.cast(e);

        if (!conditions.stream().allMatch((c) -> c.test(event))) {
            return;
        }

        if (!until.stream().allMatch((u) -> u.test(event))) {
            unsubscribe();
            return;
        }

        handler.accept(event, this);
    }

    //

    public static class Builder<T extends Event> {

        private final JavaPlugin plugin;
        private final Consumer<EventSubscription<T>> unsubscribe;

        private final Class<T> eventClass;
        private BiConsumer<T, EventSubscription<T>> handler;
        private final Collection<Predicate<T>> until = new CopyOnWriteArraySet<>();
        private final Collection<Predicate<T>> conditions = new CopyOnWriteArraySet<>();
        private final Collection<Predicate<Event>> filters;
        private EventPriority priority = EventPriority.NORMAL;

        public Builder(@NotNull JavaPlugin plugin, @NotNull Consumer<EventSubscription<T>> unsubscribe, @NotNull Class<T> eventClass, @NotNull Collection<Predicate<Event>> filters) {
            this.plugin = plugin;
            this.unsubscribe = unsubscribe;
            this.eventClass = eventClass;
            this.filters = filters;
        }

        public EventSubscription.Builder<T> handler(@NotNull BiConsumer<T, EventSubscription<T>> handler) {
            this.handler = handler;
            return this;
        }

        public EventSubscription.Builder<T> handler(@NotNull Consumer<T> handler) {
            return handler((e, s) -> handler.accept(e));
        }

        public EventSubscription.Builder<T> until(@NotNull Predicate<T> condition) {
            this.until.add(condition);
            return this;
        }

        public EventSubscription.Builder<T> until(@NotNull Instant instant) {
            return until(e -> Instant.now().isBefore(instant));
        }

        public EventSubscription.Builder<T> until(@NotNull Duration duration) {
            Instant target = Instant.now().plus(duration);
            return until(target);
        }

        public EventSubscription.Builder<T> until(int amount) {
            AtomicReference<Integer> count = new AtomicReference<>(0);
            return until(e -> count.getAndSet(count.get() + 1) < amount);
        }

        public EventSubscription.Builder<T> priority(@NotNull EventPriority priority) {
            this.priority = priority;
            return this;
        }

        public EventSubscription.Builder<T> condition(@NotNull Predicate<T> condition) {
            this.conditions.add(condition);
            return this;
        }

        public EventSubscription<T> subscribe() {
            if (handler == null) {
                throw new IllegalStateException("Handler not set");
            }
            EventSubscription<T> sub = new EventSubscription<>(unsubscribe, eventClass, handler, until, conditions, filters);
            plugin.getServer().getPluginManager().registerEvent(eventClass, sub, priority, sub, plugin);
            return sub;
        }


    }
}
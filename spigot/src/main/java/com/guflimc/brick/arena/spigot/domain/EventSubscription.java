package com.guflimc.brick.arena.spigot.domain;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EventSubscription {

    public abstract void unsubscribe();

    //

    public static abstract class Builder<T extends Event> {
        
        protected BiConsumer<T, EventSubscription> handler;
        protected Collection<Predicate<T>> until = new CopyOnWriteArraySet<>();
        protected Collection<Predicate<T>> conditions = new CopyOnWriteArraySet<>();
        protected EventPriority priority = EventPriority.NORMAL;

        public Builder<T> handler(@NotNull BiConsumer<T, EventSubscription> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<T> handler(@NotNull Consumer<T> handler) {
            return handler((e, s) -> handler.accept(e));
        }

        public Builder<T> until(@NotNull Predicate<T> condition) {
            this.until.add(condition);
            return this;
        }

        public Builder<T> until(@NotNull Instant instant) {
            return until(e -> Instant.now().isBefore(instant));
        }

        public Builder<T> until(@NotNull Duration duration) {
            Instant target = Instant.now().plus(duration);
            return until(target);
        }

        public Builder<T> until(int amount) {
            AtomicReference<Integer> count = new AtomicReference<>(0);
            return until(e -> count.getAndSet(count.get() + 1) < amount);
        }

        public Builder<T> priority(@NotNull EventPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder<T> condition(@NotNull Predicate<T> condition) {
            this.conditions.add(condition);
            return this;
        }

        public abstract EventSubscription subscribe();

    }

}

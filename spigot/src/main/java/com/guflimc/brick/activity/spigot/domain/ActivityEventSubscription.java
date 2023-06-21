package com.guflimc.brick.activity.spigot.domain;

import com.guflimc.brick.activity.spigot.events.ActivityEvent;
import com.guflimc.brick.math.spigot.SpigotMath;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;


class ActivityEventSubscription<T extends Event> extends EventSubscription implements Listener, EventExecutor {

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
        if (event instanceof PlayerEvent ep && !activity.players().contains(ep.getPlayer())) {
            return;
        }
        if (event instanceof EntityEvent ee && !activity.arena().map((a) -> a.contains(SpigotMath.toBrickLocation(ee.getEntity().getLocation()))).orElse(false)) {
            return;
        }
        if (event instanceof BlockEvent be && !activity.arena().map((a) -> a.contains(SpigotMath.toBrickLocation(be.getBlock().getLocation()))).orElse(false))
            return;

        if (!conditions.stream().allMatch((c) -> c.test(ev))) {
            return;
        }

        if (!until.stream().allMatch((u) -> u.test(ev))) {
            unsubscribe();
            return;
        }

        handler.accept(ev, this);
    }

    //

    static class Builder<T extends Event> extends EventSubscription.Builder<T> {

        private final Activity activity;
        private final Class<T> eventClass;

        Builder(@NotNull Activity activity, @NotNull Class<T> eventClass) {
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
}
package com.guflimc.brick.arena.spigot.state;

import com.guflimc.brick.arena.spigot.event.EventSubscription;
import com.guflimc.brick.arena.state.State;
import com.guflimc.brick.scheduler.api.SchedulerTask;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class SpigotState implements State {

    private final JavaPlugin plugin;
    private final SpigotScheduler scheduler;

    private final Collection<Predicate<Event>> filters = new CopyOnWriteArraySet<>();
    private final Collection<EventSubscription<?>> subscriptions = new CopyOnWriteArraySet<>();

    protected SpigotState(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = new SpigotScheduler(plugin, toString());
    }

    @Override
    public void onEnd() {
        new HashSet<>(subscriptions).forEach(EventSubscription::unsubscribe);
        scheduler.shutdown();
    }

    // Event handling

    protected final void filter(Predicate<Event> filter) {
        filters.add(filter);
    }

    protected final <T extends Event> EventSubscription.Builder<T> listen(@NotNull Class<T> eventClass) {
        return new EventSubscription.Builder<>(plugin, subscriptions::remove, eventClass, filters);
    }

    protected final void listen(@NotNull Listener listener) {
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

            EventHandler an = m.getAnnotation(EventHandler.class);
            Class<? extends Event> eventClass = type.asSubclass(Event.class);

            listen(eventClass)
                    .priority(an.priority())
                    .condition((e) -> !an.ignoreCancelled() || !(e instanceof Cancellable c) || !c.isCancelled())
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

    // Scheduling

    protected final SchedulerTask repeat(long interval, TimeUnit unit, Runnable task) {
        return scheduler.syncRepeating(task, interval, unit);
    }

    protected final SchedulerTask repeatLater(long delay, long interval, TimeUnit unit, Runnable task) {
        return scheduler.syncRepeating(task, delay, interval, unit);
    }

    protected final SchedulerTask repeatAsync(long interval, TimeUnit unit, Runnable task) {
        return scheduler.asyncRepeating(task, interval, unit);
    }

    protected final SchedulerTask repeatAsyncLater(long delay, long interval, TimeUnit unit, Runnable task) {
        return scheduler.asyncRepeating(task, delay, interval, unit);
    }

    protected final SchedulerTask later(long delay, TimeUnit unit, Runnable task) {
        return scheduler.syncLater(task, delay, unit);
    }

    protected final SchedulerTask laterAsync(long delay, TimeUnit unit, Runnable task) {
        return scheduler.asyncLater(task, delay, unit);
    }

}

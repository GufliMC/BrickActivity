package com.guflimc.brick.arena.spigot.state;

import com.guflimc.brick.arena.spigot.subscription.EventSubscription;
import com.guflimc.brick.arena.state.State;
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
import java.util.function.Predicate;

public abstract class BaseState implements State {

    private final JavaPlugin plugin;

    private final Collection<Predicate<Event>> filters = new CopyOnWriteArraySet<>();
    private final Collection<EventSubscription<?>> subscriptions = new CopyOnWriteArraySet<>();

    protected BaseState(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnd() {
        new HashSet<>(subscriptions).forEach(EventSubscription::unsubscribe);
    }

    //

    public void addFilter(Predicate<Event> filter) {
        filters.add(filter);
    }

    public <T extends Event> EventSubscription.Builder<T> on(@NotNull Class<T> eventClass) {
        return new EventSubscription.Builder<>(plugin, subscriptions::remove, eventClass, filters);
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

            EventHandler an = m.getAnnotation(EventHandler.class);
            Class<? extends Event> eventClass = type.asSubclass(Event.class);

            on(eventClass)
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
}

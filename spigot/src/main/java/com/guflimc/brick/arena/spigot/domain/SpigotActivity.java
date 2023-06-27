package com.guflimc.brick.arena.spigot.domain;

import com.guflimc.brick.arena.domain.Activity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class SpigotActivity extends Activity<Player> {

    public final JavaPlugin plugin;
    final Collection<EventSubscription> subscriptions = new CopyOnWriteArraySet<>();

    public SpigotActivity(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;

        // clear memory
        on(PlayerQuitEvent.class).handler(e -> remove(e.getPlayer())).subscribe();
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    // EVENTS

    public <T extends Event> EventSubscription.Builder<T> on(@NotNull Class<T> eventClass) {
        return new BaseEventSubscription.Builder<>(this, eventClass);
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

}

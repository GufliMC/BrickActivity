package com.guflimc.brick.arena.spigot.activity;

import com.guflimc.brick.arena.spigot.event.EventSubscription;
import com.guflimc.brick.scheduler.api.SchedulerTask;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class Branch {

    private final Activity activity;
    private final SpigotScheduler scheduler;

    private boolean terminated = false;

    Branch(Activity activity) {
        this.activity = activity;
        this.scheduler = new SpigotScheduler(activity.plugin, this.toString());
    }

    public void terminate() {
        if ( terminated ) {
            throw new IllegalStateException("Branch already terminated");
        }
        terminated = true;
        scheduler.shutdown();
    }
    
    private void checkState() {
        if ( terminated ) {
            throw new IllegalStateException("Branch is terminated");
        }
    }

    // Event handling

    public <T extends Event> EventSubscription.Builder<T> listen(@NotNull Class<T> eventClass) {
        checkState();
        return new EventSubscription.Builder<>(activity.plugin, activity.filter(), eventClass)
                .until((v) -> terminated);
    }

    public void listen(@NotNull Listener listener) {
        checkState();
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

    public SchedulerTask repeat(long interval, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.syncRepeating(task, interval, unit);
    }

    public SchedulerTask repeatLater(long delay, long interval, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.syncRepeating(task, delay, interval, unit);
    }

    public SchedulerTask repeatAsync(long interval, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.asyncRepeating(task, interval, unit);
    }

    public SchedulerTask repeatAsyncLater(long delay, long interval, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.asyncRepeating(task, delay, interval, unit);
    }

    public SchedulerTask later(long delay, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.syncLater(task, delay, unit);
    }

    public SchedulerTask laterAsync(long delay, TimeUnit unit, Runnable task) {
        checkState();
        return scheduler.asyncLater(task, delay, unit);
    }

}

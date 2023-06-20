package com.guflimc.brick.activity.spigot.domain;

import org.jetbrains.annotations.NotNull;

public record AttributeKey<T>(String key, Class<T> type) {

    public AttributeKey(@NotNull String key, @NotNull Class<T> type) {
        this.key = key;
        this.type = type;
    }

}

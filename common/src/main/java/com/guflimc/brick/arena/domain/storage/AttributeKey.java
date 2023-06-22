package com.guflimc.brick.arena.domain.storage;

import org.jetbrains.annotations.NotNull;

public record AttributeKey<T>(String key, Class<T> type) {

    public AttributeKey(@NotNull String key, @NotNull Class<T> type) {
        this.key = key;
        this.type = type;
    }

}

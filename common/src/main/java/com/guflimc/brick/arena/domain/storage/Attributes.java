package com.guflimc.brick.arena.domain.storage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Attributes {

    private final Map<AttributeKey<?>, Object> attributes = new ConcurrentHashMap<>();

    <T> void set(@NotNull AttributeKey<T> key, @NotNull T value) {
        attributes.put(key, value);
    }

    <T> Optional<T> get(@NotNull AttributeKey<T> key) {
        return Optional.ofNullable(key.type().cast(attributes.get(key)));
    }

    <T> void clear(@NotNull AttributeKey<T> key) {
        attributes.remove(key);
    }

}

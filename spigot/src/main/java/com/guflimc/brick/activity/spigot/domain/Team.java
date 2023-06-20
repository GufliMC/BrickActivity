package com.guflimc.brick.activity.spigot.domain;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

public record Team(String name, Component displayName, RGBLike color) {

    public Team(@NotNull String name, @NotNull Component displayName, @NotNull RGBLike color) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
    }

    public TextColor textColor() {
        return TextColor.color(color);
    }

    public NamedTextColor namedTextColor() {
        return NamedTextColor.nearestTo(textColor());
    }

    //

    public static Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;
        private Component displayName;
        private RGBLike color;

        private Builder(String name) {
            this.name = name;
        }

        public Builder displayName(@NotNull Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder color(@NotNull RGBLike color) {
            this.color = color;
            return this;
        }

        public Team build() {
            return new Team(name, displayName, color);
        }

    }
}

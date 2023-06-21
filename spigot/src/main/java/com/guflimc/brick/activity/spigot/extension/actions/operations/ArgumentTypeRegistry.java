package com.guflimc.brick.activity.spigot.extension.actions.operations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ArgumentTypeRegistry {

    private final static Map<Class<?>, Function<String, Object>> parsers = new ConcurrentHashMap<>();

    public static void register(@NotNull Class<?> type, @NotNull Function<String, Object> parser) {
        type = MethodType.methodType(type).wrap().returnType();
        parsers.put(type, parser);
    }

    public static Function<String, Object> get(@NotNull Class<?> type) {
        type = MethodType.methodType(type).wrap().returnType();
        return parsers.get(type);
    }

    //

    static {
        // java
        register(Integer.class, Integer::decode);
        register(Double.class, Double::parseDouble);
        register(Float.class, Float::parseFloat);
        register(Long.class, Long::parseLong);
        register(Boolean.class, Boolean::parseBoolean);
        register(Short.class, Short::parseShort);
        register(Byte.class, Byte::parseByte);
        register(String.class, (input) -> input);
        register(java.awt.Color.class, java.awt.Color::decode);
        register(LocalTime.class, LocalTime::parse);
        register(LocalDate.class, LocalDate::parse);
        register(LocalDateTime.class, LocalDateTime::parse);

        // spigot
        register(World.class, (input) -> Bukkit.getServer().getWorld(input));
        register(Material.class, Material::matchMaterial);
        register(Sound.class, (input) -> Arrays.stream(Sound.values())
                .filter(s -> s.getKey().toString().equals(input))
                .findFirst().orElse(null));
        register(EntityType.class, (input) -> Arrays.stream(EntityType.values())
                .filter(s -> s.getKey().toString().equals(input))
                .findFirst().orElse(null));
        register(PotionEffectType.class, (input) -> Arrays.stream(PotionEffectType.values())
                .filter(s -> s.getKey().toString().equals(input))
                .findFirst().orElse(null));
        register(Enchantment.class, (input) -> Arrays.stream(Enchantment.values())
                .filter(s -> s.getKey().toString().equals(input))
                .findFirst().orElse(null));
        register(Color.class, (input) -> Color.fromRGB(Integer.decode(input)));

        // adventure
        register(Component.class, (input) -> LegacyComponentSerializer.legacyAmpersand().deserialize(input));
        register(TextColor.class, (input) -> {
            NamedTextColor color = NamedTextColor.NAMES.value(input);
            if (color != null) {
                return color;
            }
            return TextColor.color(Integer.decode(input));
        });
    }

}

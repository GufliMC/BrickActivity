package com.guflimc.brick.arena.spigot.extension.actions.operations;

import com.guflimc.brick.math.common.geometry.pos3.Location;
import com.guflimc.brick.math.spigot.SpigotMath;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class OperationTypeRegistry {

    private final static Map<String, OperationType> types = new ConcurrentHashMap<>();

    private OperationTypeRegistry() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static void register(@NotNull String name, @NotNull OperationType type) {
        if ( types.containsKey(name) ) {
            throw new IllegalArgumentException("Operation type with this name already registered: " + name);
        }
        types.put(name, type);
    }

    public static Consumer<Object> compile(@NotNull String input, @NotNull Class<?>... sources) {
        String name = input.split(" ")[0];
        OperationType type = types.get(name);
        if ( type == null ) {
            throw new IllegalArgumentException("Operation type not found: " + name);
        }

        input = input.substring(name.length() + 1);
        return type.compile(input, sources);
    }

    //

    static {
        register("setblock", BaseOperationType.builder()
                .string(Location.class)
                .string(Material.class)
                .executor((args) -> {
                    Location location = args.get(0);
                    Material material = args.get(1);
                    SpigotMath.toSpigotLocation(location).getBlock().setType(material);
                }).build());
    }
}

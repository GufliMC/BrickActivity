package com.guflimc.brick.activity.spigot.extension.compile;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class Compiler {

    protected record CompiledSection(@NotNull Class<?> type, @NotNull Function<Object, Object> retriever) {

        public CompiledSection(@NotNull Class<?> type, @NotNull Function<Object, Object> retriever) {
            this.type = MethodType.methodType(type).wrap().returnType();
            this.retriever = retriever;
        }

    }

    protected final CompiledSection compile(@NotNull Class<?> source, @NotNull String section) {
        if (section.startsWith("$source")) {
            return compile(source, section.substring(7).split("\\."));
        }
        return compile(section);
    }

    protected CompiledSection compile(@NotNull String section) {
        if (section.matches("^\\d*(\\.\\d+)?$")) {
            return new CompiledSection(Double.class, (obj) -> Double.parseDouble(section));
        }

        if (section.equals("true") || section.equals("false")) {
            return new CompiledSection(Boolean.class, (obj) -> Boolean.parseBoolean(section));
        }

        return new CompiledSection(String.class, (obj) -> section);
    }

    private CompiledSection compile(@NotNull Class<?> source, @NotNull String[] path) {
        String field = path[0];
        if (field.equals("")) {
            return compile(source, Arrays.copyOfRange(path, 1, path.length));
        }

        if (field.equals("class")) {
            return new CompiledSection(Class.class, Object::getClass);
        }

        // find matching method
        Method method = method(source, field);
        if (method == null) {
            throw new RuntimeException("Could not find method for key " + field + " in " + source.getName());
        }

        Function<Object, Object> supplier = (obj) -> {
            try {
                return method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        if (path.length == 1) {
            return new CompiledSection(method.getReturnType(), supplier);
        }

        CompiledSection child = compile(method.getReturnType(), Arrays.copyOfRange(path, 1, path.length));
        return new CompiledSection(child.type, (obj) -> child.retriever.apply(supplier.apply(obj)));
    }

    // REFLECTION

    public static Method method(@NotNull Class<?> source, @NotNull String name) {
        return rmethod(source, name, new HashSet<>());
    }

    private static Method rmethod(@NotNull Class<?> source, @NotNull String name, @NotNull Set<Class<?>> visited) {
        if (visited.contains(source)) {
            return null;
        }
        visited.add(source);

        for (Method method : source.getDeclaredMethods()) {
            if (method.getReturnType().equals(Void.TYPE) || method.getParameterCount() != 0) {
                continue;
            }

            String n = method.getName();
            if (n.startsWith("get")) n = n.substring(3);
            n = n.substring(0, 1).toLowerCase() + n.substring(1);

            if (name.equals(n)) {
                return method;
            }
        }

        for (Class<?> sup : supertypes(source)) {
            Method method = rmethod(sup, name, visited);
            if (method != null) {
                return method;
            }
        }

        return null;
    }

    public static Collection<Class<?>> supertypes(Class<?> type) {
        Set<Class<?>> supertypes = new HashSet<>();
        Optional.ofNullable(type.getSuperclass()).ifPresent(supertypes::add);
        supertypes.addAll(Arrays.asList(type.getInterfaces()));
        return supertypes;
    }

}

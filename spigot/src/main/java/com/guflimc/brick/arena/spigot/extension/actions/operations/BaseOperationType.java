package com.guflimc.brick.arena.spigot.extension.actions.operations;

import com.guflimc.brick.arena.spigot.extension.compile.Compiler;
import com.guflimc.brick.arena.spigot.extension.compile.StringArrayParser;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseOperationType extends Compiler implements OperationType {

    private final List<Parameter> parameters;
    private final Consumer<Arguments> executor;

    public BaseOperationType(@NotNull List<Parameter> parameters, @NotNull Consumer<Arguments> executor) {
        this.parameters = List.copyOf(parameters);
        this.executor = executor;
    }

    private record Parameter(@NotNull Class<?> type, @NotNull StringArrayParser.ParseMode parseMode) {
    }

    @Override
    public Consumer<Object> compile(@NotNull String input, @NotNull Class<?>... sources) {
        StringArrayParser arrayParser = new StringArrayParser(input, parameters.size());
        String[] stringArgs = arrayParser.parse();

        Map<Class<?>, Consumer<Object>> consumers = new HashMap<>();
        for (Class<?> source : sources) {
            List<CompiledSection> sections = new ArrayList<>();
            for (int i = 0; i < parameters.size(); i++) {
                Parameter param = parameters.get(i);
                String arg = stringArgs[i];
                CompiledSection section = compile(source, param.type(), arg);
                sections.add(section);
            }

            consumers.put(source, (obj) -> {
                List<Object> arguments = new ArrayList<>();
                for (int i = 0; i < parameters.size(); i++) {
                    arguments.add(sections.get(i).retriever().apply(obj));
                }
                executor.accept(new Arguments(arguments));
            });
        }

        return (obj) -> {
            if (!consumers.containsKey(obj.getClass())) {
                throw new RuntimeException("Unsupported source for this operation.");
            }
            consumers.get(obj.getClass()).accept(obj);
        };
    }

    private CompiledSection compile(@NotNull Class<?> source, @NotNull Class<?> target, @NotNull String value) {
        CompiledSection section = compile(source, value);
        if (section.type().equals(target)) {
            return section;
        }

        if (!section.type().equals(String.class)) {
            throw new IllegalArgumentException("The argument " + value + " is not of type " + target.getName());
        }

        if (Enum.class.isAssignableFrom(target)) {
            Object constant = Arrays.stream(target.getEnumConstants())
                    .filter(con -> ((Enum<?>) con).name().equalsIgnoreCase(value))
                    .findFirst().map(con -> (Object) con)
                    .orElseThrow(() -> new IllegalArgumentException("No enum constant " + value + " found for type " + target.getName() + "."));
            return new CompiledSection(target, (obj) -> constant);
        }

        Function<String, Object> parser = ArgumentTypeRegistry.get(target);
        if ( parser != null ) {
            Object parsed = parser.apply(value);
            if ( parsed == null ) {
                throw new IllegalArgumentException("The argument " + value + " cannot be parsed into type " + target.getName());
            }

            return new CompiledSection(target, (obj) -> parsed);
        }

        throw new IllegalArgumentException("The argument " + value + " is not of type " + target.getName());
    }

    //

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Parameter> parameters = new ArrayList<>();
        private Consumer<Arguments> executor;

        private Builder() {}

        public Builder parameter(@NotNull Class<?> type, @NotNull StringArrayParser.ParseMode parseMode) {
            parameters.add(new Parameter(type, parseMode));
            return this;
        }

        public Builder string(@NotNull Class<?> type) {
            return parameter(type, StringArrayParser.ParseMode.STRING);
        }

        public Builder greedy(@NotNull Class<?> type) {
            return parameter(type, StringArrayParser.ParseMode.GREEDY);
        }

        public Builder executor(@NotNull Consumer<Arguments> executor) {
            this.executor = executor;
            return this;
        }

        public BaseOperationType build() {
            if (executor == null) {
                throw new IllegalStateException("Executor is not set");
            }
            return new BaseOperationType(parameters, executor);
        }

    }


}

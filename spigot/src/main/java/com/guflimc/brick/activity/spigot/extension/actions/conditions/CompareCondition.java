package com.guflimc.brick.activity.spigot.extension.actions.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

public record CompareCondition(@NotNull String left, @NotNull Operator operator,
                               @NotNull String right) implements Condition {

    @Override
    public Predicate<Object> compile(Class<?>... sources) {
        return new CompareConditionCompiler(this).compile(sources);
    }

    //

    public enum Operator {
        EQUALS("=="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUALS(">="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUALS("<="),
        IS("is"),
        IS_NOT("is not");
//        IN("in"), // TODO
//        NOT_IN("not in");

        private final String key;

        Operator(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        public static Operator from(@NotNull String key) {
            return Arrays.stream(values()).filter(o -> o.key().equals(key))
                    .findFirst().orElseThrow();
        }

    }

}

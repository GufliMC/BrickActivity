package com.guflimc.brick.activity.spigot.extension.actions.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public record CompositeCondition(@NotNull Operator operator, @NotNull List<Condition> children) implements Condition {

    public CompositeCondition(@NotNull Operator operator, @NotNull List<Condition> children) {
        this.operator = operator;
        this.children = List.copyOf(children);
    }

    @Override
    public Predicate<Object> compile(Class<?>... sources) {
        return null;
    }

    public enum Operator {
        AND("&&"),
        OR("||"),
        NOT("!"),
        XOR("^");

        private final String key;

        Operator(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}

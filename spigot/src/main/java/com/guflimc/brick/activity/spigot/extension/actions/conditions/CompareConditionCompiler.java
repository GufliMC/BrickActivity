package com.guflimc.brick.activity.spigot.extension.actions.conditions;

import com.guflimc.brick.activity.spigot.extension.compile.Compiler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

class CompareConditionCompiler extends Compiler {

    private final static Map<CompareCondition.Operator, BiFunction<CompiledSection, CompiledSection, Predicate<Object>>> OPERATORS = Map.of(
            CompareCondition.Operator.EQUALS, CompareConditionCompiler::equals,
            CompareCondition.Operator.NOT_EQUALS, CompareConditionCompiler::notEquals,
            CompareCondition.Operator.GREATER_THAN, CompareConditionCompiler::greaterThan,
            CompareCondition.Operator.GREATER_THAN_OR_EQUALS, CompareConditionCompiler::greaterThanOrEquals,
            CompareCondition.Operator.LESS_THAN, CompareConditionCompiler::lessThan,
            CompareCondition.Operator.LESS_THAN_OR_EQUALS, CompareConditionCompiler::lessThanOrEquals,
            CompareCondition.Operator.IS, CompareConditionCompiler::is,
            CompareCondition.Operator.IS_NOT, CompareConditionCompiler::isNot
    );

    private final CompareCondition condition;

    CompareConditionCompiler(@NotNull CompareCondition condition) {
        this.condition = condition;
    }

    // COMPILE

    public Predicate<Object> compile(Class<?>... sources) {
        Map<Class<?>, Predicate<Object>> predicates = new HashMap<>();

        for (Class<?> source : sources) {
            CompiledSection left = compile(source, condition.left());
            CompiledSection right = compile(source, condition.right());

            Predicate<Object> predicate = OPERATORS.get(condition.operator()).apply(left, right);
            predicates.put(source, predicate);
        }

        return (obj) -> {
            if (!predicates.containsKey(obj.getClass())) {
                throw new RuntimeException("Unsupported source for this condition.");
            }
            return predicates.get(obj.getClass()).test(obj);
        };
    }

    // OPERATIONS

    private static Predicate<Object> equals(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        if ( left.type().isEnum() || right.type().isEnum() ) {
            return (obj) -> Objects.equals(left.retriever().apply(obj).toString(), right.retriever().apply(obj).toString());
        }
        return (obj) -> Objects.equals(left.retriever().apply(obj), right.retriever().apply(obj));
    }

    private static Predicate<Object> notEquals(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Predicate<Object> equals = equals(left, right);
        return (obj) -> !equals.test(obj);
    }

    private static Function<Object, Integer> compare(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        if (!Comparable.class.isAssignableFrom(left.type()) || !Comparable.class.isAssignableFrom(right.type())) {
            throw new RuntimeException("Cannot compare " + left.type().getName() + " and " + right.type().getName());
        }

        return (obj) -> {
            Comparable<Object> lv = (Comparable<Object>) left.retriever().apply(obj);
            Comparable<Object> rv = (Comparable<Object>) right.retriever().apply(obj);
            return lv.compareTo(rv);
        };
    }

    private static Predicate<Object> greaterThan(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Function<Object, Integer> compare = compare(left, right);
        return (obj) -> compare.apply(obj) > 0;
    }

    private static Predicate<Object> greaterThanOrEquals(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Function<Object, Integer> compare = compare(left, right);
        return (obj) -> compare.apply(obj) >= 0;
    }

    private static Predicate<Object> lessThan(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Function<Object, Integer> compare = compare(left, right);
        return (obj) -> compare.apply(obj) < 0;
    }

    private static Predicate<Object> lessThanOrEquals(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Function<Object, Integer> compare = compare(left, right);
        return (obj) -> compare.apply(obj) <= 0;
    }

    private static Predicate<Object> is(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        boolean isClass = right.type().getName().equals("java.lang.Class");
        if (isClass && !left.type().isAssignableFrom(right.type())) {
            throw new RuntimeException(left.type().getName() + " and " + right.type().getName() + " have no common supertypes.");
        } else if (!right.type().equals(String.class)) {
            throw new RuntimeException("Cannot do type check for type value " + right.type().getName());
        }

        return (obj) -> {
            Object lv = left.retriever().apply(obj);
            Object rv = right.retriever().apply(obj);

            if (isClass) {
                Class<?> type = (Class<?>) rv;
                return type.isInstance(lv);
            }

            return Stream.concat(
                            Stream.of(lv.getClass()),
                            supertypes(lv.getClass()).stream()
                    )
                    .map(Class::getSimpleName)
                    .anyMatch(rv::equals);
        };
    }

    private static Predicate<Object> isNot(@NotNull CompiledSection left, @NotNull CompiledSection right) {
        Predicate<Object> is = is(left, right);
        return (obj) -> !is.test(obj);
    }

}
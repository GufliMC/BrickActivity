package com.guflimc.brick.activity.spigot.extension.actions.conditions;

import com.guflimc.brick.activity.spigot.extension.compile.StringArrayParser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface Condition {

    Predicate<Object> compile(@NotNull Class<?>... sources);


    static Condition parse(@NotNull String definition) {
        StringArrayParser parser = new StringArrayParser(definition, 3);
        String[] sections = parser.parse();
        return new CompareCondition(sections[0], CompareCondition.Operator.from(sections[1]), sections[2]);
    }
}

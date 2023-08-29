package com.guflimc.brick.arena.state;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StateSeries {

    private final List<State> states;
    private int index = -1;

    private StateSeries(@NotNull List<State> states) {
        this.states = states;
    }

    public static StateSeries of(State... states) {
        return new StateSeries(List.of(states));
    }

    public void skip(int amount) {
        if ( index >= states.size() ) {
            return;
        }

        if ( index >= 0 ) {
            states.get(index).onEnd();
        }
        index += amount;
        if ( index < states.size() ) {
            states.get(index).onBegin();
        }
    }

    public void next() {
        skip(1);
    }

}

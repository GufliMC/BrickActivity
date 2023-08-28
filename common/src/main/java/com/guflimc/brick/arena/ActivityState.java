package com.guflimc.brick.arena;

public class ActivityState<S extends Enum<S>> {

    private final Class<S> type;
    private S state;

    public ActivityState(Class<S> type) {
        this.type = type;
        this.state = type.getEnumConstants()[0];
    }

    public S get() {
        return state;
    }

    public void next() {
        int index = state.ordinal() + 1;
        if ( index >= type.getEnumConstants().length ) {
            index = 0;
        }
        state = type.getEnumConstants()[index];
    }

}

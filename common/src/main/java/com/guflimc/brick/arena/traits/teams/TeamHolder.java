package com.guflimc.brick.arena.traits.teams;

import java.util.concurrent.atomic.AtomicReference;

public interface TeamHolder {

    AtomicReference<Team> team();

}

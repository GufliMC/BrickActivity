package com.guflimc.brick.arena.system.teams;

import java.util.concurrent.atomic.AtomicReference;

public interface TeamHolder {

    AtomicReference<Team> team();

}

package com.guflimc.brick.arena.clock;

import java.util.function.Predicate;

record Milestone(Predicate<Long> predicate, Runnable runnable) {

}
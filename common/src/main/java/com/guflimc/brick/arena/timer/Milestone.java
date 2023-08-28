package com.guflimc.brick.arena.timer;

import java.util.function.Predicate;

record Milestone(Predicate<Long> predicate, Runnable runnable) {

}
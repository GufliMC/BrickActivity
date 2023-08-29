package com.guflimc.brick.arena.system.teams;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface TeamActivity<P, PC extends TeamHolder> {

    Collection<P> players();

    Optional<PC> data(@NotNull P player);

}

package com.guflimc.brick.arena.mechanics.nexus;

import com.guflimc.brick.arena.domain.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record NexusConfig(@NotNull Collection<Team> attackers, int health) {
}

package com.cuzzi.reforged.stats;

import net.minecraft.world.entity.player.Player;

/**
 * Anything that gives bonus stats: armor, weapons, accessories, buffs...
 * The future item system registers one of these and the screen picks it up.
 */
@FunctionalInterface
public interface StatBonusSource {
    int bonus(Player player, StatType type);
}
package com.cuzzi.reforged.server.magic.lib;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DamageLib {

    /**
     * Calcula el daño mágico base escalado por la Inteligencia del jugador.
     * @param baseDamage Daño base del hechizo.
     * @param intScaling Multiplicador (Ej: 0.5 = 50% de la INT se suma al daño).
     */
    public static float calculateMagicDamage(ServerPlayer caster, float baseDamage, float intScaling) {
        PlayerStatsData stats = ModAttachments.stats(caster);
        if (stats == null) return baseDamage;

        int intelligence = stats.appliedValue(StatType.INTELLIGENCE) + stats.bonus(StatType.INTELLIGENCE, caster);
        return baseDamage + (intelligence * intScaling);
    }

    /**
     * Aplica daño mágico directo a un objetivo.
     */
    public static void applyMagicDamage(ServerPlayer caster, LivingEntity target, float amount) {
        target.hurt(caster.damageSources().magic(), amount);
    }

    /**
     * Aplica daño mágico indirecto (proveniente de un proyectil o campo).
     */
    public static void applyIndirectMagicDamage(ServerPlayer caster, Entity sourceEntity, LivingEntity target, float amount) {
        target.hurt(caster.damageSources().indirectMagic(sourceEntity, caster), amount);
    }
}
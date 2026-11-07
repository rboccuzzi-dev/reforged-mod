package com.cuzzi.reforged.server.magic.spells.impl;

import com.cuzzi.reforged.server.magic.lib.VisualLib;
import com.cuzzi.reforged.server.magic.spells.archetypes.AoESpell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FrostNovaSpell extends AoESpell {

    public FrostNovaSpell() {
        super(
                "frost_nova",
                "Frost Nova",
                "Shatters the ground with ice, slowing enemies.",
                "reforged:textures/gui/skills/frost_nova.png",
                StatType.MANA,
                30,      // Cost
                200,     // Cooldown
                5.0,     // Radius
                true     // Target Ground
        );
    }

    @Override
    protected void playAreaVisuals(ServerPlayer caster, Vec3 center, double radius) {
        ServerLevel level = (ServerLevel) caster.level();

        VisualLib.spawnFloorRune(level, center, (float) radius, -3.0f, 60, "reforged:textures/entity/rune/placeholder_rune.png", 0x00BFFF);
        VisualLib.spawnBurst(level, center.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 100, 0.2);
    }

    @Override
    protected void onAffectEntity(ServerPlayer caster, LivingEntity target) {
        target.hurt(caster.damageSources().freeze(), 8.0f);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
    }
}
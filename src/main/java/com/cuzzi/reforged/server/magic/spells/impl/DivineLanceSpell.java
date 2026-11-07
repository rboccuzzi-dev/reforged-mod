package com.cuzzi.reforged.server.magic.spells.impl;

import com.cuzzi.reforged.server.magic.lib.VisualLib;
import com.cuzzi.reforged.server.magic.spells.archetypes.RaycastSpell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DivineLanceSpell extends RaycastSpell {

    public DivineLanceSpell() {
        super(
                "divine_lance",
                "Divine Lance",
                "Fires a piercing beam of holy energy.",
                "reforged:textures/gui/skills/divine_lance.png",
                StatType.MANA,
                15,      // Cost
                40,      // Cooldown
                30.0     // Range
        );
    }

    @Override
    protected void onCastVisuals(ServerPlayer caster) {
        VisualLib.spawnFrontalRune(caster, 1.5, 1.0f, 15.0f, 20, "reforged:textures/entity/rune/placeholder_rune.png", 0xFFD700);
    }

    @Override
    protected ParticleOptions getBeamParticle() {
        return ParticleTypes.END_ROD;
    }

    @Override
    protected void onHitEntity(ServerPlayer caster, LivingEntity target, Vec3 hitPos) {
        target.hurt(caster.damageSources().magic(), 12.0f);
        VisualLib.spawnBurst((ServerLevel) caster.level(), hitPos, ParticleTypes.CRIT, 20, 0.3);
    }

    @Override
    protected void onHitBlock(ServerPlayer caster, Vec3 hitPos) {
        VisualLib.spawnBurst((ServerLevel) caster.level(), hitPos, ParticleTypes.FLAME, 10, 0.1);
    }
}
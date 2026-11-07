package com.cuzzi.reforged.server.magic.spells.impl;

import com.cuzzi.reforged.server.magic.lib.DamageLib;
import com.cuzzi.reforged.server.magic.lib.VisualLib;
import com.cuzzi.reforged.server.magic.spells.archetypes.ProjectileSpell;
import com.cuzzi.reforged.server.magic.spells.archetypes.SpellProjectileEntity;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FireballSpell extends ProjectileSpell {

    public FireballSpell() {
        super(
                "fireball",
                "Fireball",
                "Hurls a devastating sphere of flames.",
                "reforged:textures/gui/skills/fireball.png",
                StatType.MANA,
                20,      // Cost
                40,      // Cooldown
                1.5f,    // Speed
                0.0f     // Gravity
        );
    }

    @Override
    protected void onCastVisuals(ServerPlayer caster) {
        VisualLib.spawnFrontalRune(caster, 1.0, 0.5f, 20.0f, 10, "reforged:textures/entity/rune/placeholder_rune.png", 0xFF4500);
    }

    @Override
    public void onProjectileTick(SpellProjectileEntity projectile) {
        ServerLevel level = (ServerLevel) projectile.level();
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.1, 0.1, 0.1, 0);
        level.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.1, 0.1, 0.1, 0);
    }

    @Override
    public void onHitEntity(ServerPlayer caster, SpellProjectileEntity projectile, LivingEntity target) {
        float damage = DamageLib.calculateMagicDamage(caster, 10.0f, 0.6f);
        DamageLib.applyIndirectMagicDamage(caster, projectile, target, damage);

        target.igniteForSeconds(4);
        explodeVisuals(projectile.position(), (ServerLevel) caster.level());
    }

    @Override
    public void onHitBlock(ServerPlayer caster, SpellProjectileEntity projectile, Vec3 hitPos) {
        explodeVisuals(hitPos, (ServerLevel) caster.level());
    }

    private void explodeVisuals(Vec3 pos, ServerLevel level) {
        VisualLib.spawnBurst(level, pos, ParticleTypes.LAVA, 30, 0.3);
        VisualLib.spawnBurst(level, pos, ParticleTypes.LARGE_SMOKE, 15, 0.2);
    }
}
package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.server.magic.lib.TargetingLib;
import com.cuzzi.reforged.server.magic.lib.VisualLib;
import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class RaycastSpell extends Spell {

    private final double range;

    public RaycastSpell(String id, String name, String desc, String iconPath, StatType resourceType, int cost, int cooldown, double range) {
        super(id, name, desc, iconPath, resourceType, cost, cooldown);
        this.range = range;
    }

    @Override
    protected void onCast(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));

        onCastVisuals(player);

        EntityHitResult hitEntity = TargetingLib.raycastEntities(player, range, 0.5f);

        if (hitEntity != null && hitEntity.getEntity() instanceof LivingEntity target) {
            end = hitEntity.getLocation();
            VisualLib.drawParticleBeam(level, start, end, getBeamParticle(), 5.0f);
            onHitEntity(player, target, end);
        } else {
            HitResult hitBlock = TargetingLib.raycastBlocks(player, range);
            if (hitBlock.getType() == HitResult.Type.BLOCK) {
                end = hitBlock.getLocation();
                VisualLib.drawParticleBeam(level, start, end, getBeamParticle(), 5.0f);
                onHitBlock(player, end);
            } else {
                VisualLib.drawParticleBeam(level, start, end, getBeamParticle(), 5.0f);
            }
        }
    }

    protected abstract void onHitEntity(ServerPlayer caster, LivingEntity target, Vec3 hitPos);
    protected abstract void onHitBlock(ServerPlayer caster, Vec3 hitPos);
    protected abstract ParticleOptions getBeamParticle();
    protected abstract void onCastVisuals(ServerPlayer caster);
}
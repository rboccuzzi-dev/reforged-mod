package com.cuzzi.reforged.server.magic.lib;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class TargetingLib {

    /** Raycast puro de bloques (Busca el piso o paredes). */
    public static HitResult raycastBlocks(LivingEntity caster, double maxDistance) {
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = start.add(look.scale(maxDistance));
        return caster.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
    }

    /** Raycast de Entidades (Para hechizos tipo disparo láser / hit-scan). */
    public static EntityHitResult raycastEntities(LivingEntity caster, double maxDistance, float inflation) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().scale(maxDistance));
        AABB searchBox = caster.getBoundingBox().expandTowards(caster.getLookAngle().scale(maxDistance)).inflate(1.0);

        Predicate<net.minecraft.world.entity.Entity> filter = e -> !e.isSpectator() && e.isPickable() && e != caster;
        return ProjectileUtil.getEntityHitResult(caster.level(), caster, start, end, searchBox, filter, inflation);
    }

    /** Obtiene todas las entidades vivas en un radio exacto (Para AoE y Bursts). */
    public static List<LivingEntity> getEntitiesInRadius(Level level, Vec3 center, double radius, LivingEntity exclude) {
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> e != exclude && e.distanceToSqr(center) <= radius * radius);
    }
}
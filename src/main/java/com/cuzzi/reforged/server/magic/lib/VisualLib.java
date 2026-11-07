package com.cuzzi.reforged.server.magic.lib;

import com.cuzzi.reforged.server.magic.RuneEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class VisualLib {

    /** Dibuja una línea perfecta de partículas (Para Rayos/Beams). Súper ligero para el server. */
    public static void drawParticleBeam(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, float densityPerBlock) {
        double distance = start.distanceTo(end);
        int particleCount = (int) Math.max(1, distance * densityPerBlock);
        Vec3 dir = end.subtract(start).normalize();

        for (int i = 0; i <= particleCount; i++) {
            double step = (distance / particleCount) * i;
            Vec3 pos = start.add(dir.scale(step));
            // count = 1, speed = 0 para que la partícula se quede quieta en la línea
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.0);
        }
    }

    /** Explosión esférica de partículas (Para Bursts y AoE). */
    public static void spawnBurst(ServerLevel level, Vec3 center, ParticleOptions particle, int count, double speed) {
        level.sendParticles(particle, center.x, center.y, center.z, count, 0.5, 0.5, 0.5, speed);
    }

    /** Invoca una Runa anclada a la mirada del jugador (Para casteo frontal). */
    public static void spawnFrontalRune(ServerPlayer caster, double distance, float scale, float spinSpeed, int lifespan, String texture, int color) {
        Vec3 pos = caster.getEyePosition().add(caster.getLookAngle().scale(distance));
        RuneEntity rune = new RuneEntity(caster.level(), pos.x, pos.y, pos.z, caster.getXRot(), caster.getYRot(), scale, spinSpeed, lifespan, texture, color);
        caster.level().addFreshEntity(rune);
    }

    /** Invoca una Runa anclada al piso de forma plana. */
    public static void spawnFloorRune(ServerLevel level, Vec3 pos, float scale, float spinSpeed, int lifespan, String texture, int color) {
        // Pitch = 90 hace que la runa quede perfectamente acostada en el suelo
        RuneEntity rune = new RuneEntity(level, pos.x, pos.y + 0.05, pos.z, 90.0f, 0.0f, scale, spinSpeed, lifespan, texture, color);
        level.addFreshEntity(rune);
    }
}
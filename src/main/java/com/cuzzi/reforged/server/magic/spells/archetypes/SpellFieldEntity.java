package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.registry.ModEntities;
import com.cuzzi.reforged.server.magic.lib.TargetingLib;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SpellFieldEntity extends Entity {

    private LivingEntity owner;
    private FieldSpell spell;
    private double radius;
    private int lifespan;
    private int tickInterval;

    public SpellFieldEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SpellFieldEntity(Level level, Vec3 pos, LivingEntity owner, FieldSpell spell, double radius, int lifespan, int tickInterval) {
        super(ModEntities.SPELL_FIELD.get(), level);
        this.setPos(pos);
        this.owner = owner;
        this.spell = spell;
        this.radius = radius;
        this.lifespan = lifespan;
        this.tickInterval = tickInterval;
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (this.tickCount > lifespan) {
            this.discard();
            return;
        }

        if (spell != null) {
            spell.onFieldTick(this); // Partículas visuales ambientales

            // Aplica el efecto de área solo en los intervalos designados (para máximo rendimiento)
            if (this.tickCount % tickInterval == 0) {
                List<LivingEntity> targets = TargetingLib.getEntitiesInRadius(this.level(), this.position(), radius, owner);
                for (LivingEntity target : targets) {
                    if (owner instanceof net.minecraft.server.level.ServerPlayer player) {
                        spell.onAffectEntity(player, target);
                    }
                }
            }
        }
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b) {}
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
}
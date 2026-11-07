package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SpellProjectileEntity extends ThrowableProjectile {

    public ProjectileSpell spellArchetype;

    public SpellProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectileEntity(Level level, LivingEntity shooter, ProjectileSpell spell) {
        super(ModEntities.SPELL_PROJECTILE.get(), shooter, level);
        this.spellArchetype = spell;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && spellArchetype != null) {
            spellArchetype.onProjectileTick(this); // Emite partículas mientras vuela
        }
        if (this.tickCount > 200) this.discard(); // Muere a los 10 segundos
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && this.getOwner() instanceof net.minecraft.server.level.ServerPlayer player && spellArchetype != null) {
            if (result.getEntity() instanceof LivingEntity target) {
                spellArchetype.onHitEntity(player, this, target);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && this.getOwner() instanceof net.minecraft.server.level.ServerPlayer player && spellArchetype != null) {
            spellArchetype.onHitBlock(player, this, result.getLocation());
            this.discard();
        }
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b) {}
}
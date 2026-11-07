package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class ProjectileSpell extends Spell {

    private final float speed;
    private final float gravity;

    public ProjectileSpell(String id, String name, String desc, String iconPath, StatType resourceType, int cost, int cooldown, float speed, float gravity) {
        super(id, name, desc, iconPath, resourceType, cost, cooldown);
        this.speed = speed;
        this.gravity = gravity;
    }

    @Override
    protected void onCast(ServerPlayer player) {
        onCastVisuals(player);

        SpellProjectileEntity projectile = new SpellProjectileEntity(player.level(), player, this);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 0.0F);
        projectile.setNoGravity(gravity == 0);
        player.level().addFreshEntity(projectile);
    }

    protected abstract void onCastVisuals(ServerPlayer caster);

    public abstract void onProjectileTick(SpellProjectileEntity projectile);
    public abstract void onHitEntity(ServerPlayer caster, SpellProjectileEntity projectile, LivingEntity target);
    public abstract void onHitBlock(ServerPlayer caster, SpellProjectileEntity projectile, Vec3 hitPos);
}
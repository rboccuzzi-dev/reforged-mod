package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.server.magic.lib.TargetingLib;
import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class AoESpell extends Spell {

    private final double radius;
    private final boolean targetGround;

    public AoESpell(String id, String name, String desc, String iconPath, StatType resourceType, int cost, int cooldown, double radius, boolean targetGround) {
        super(id, name, desc, iconPath, resourceType, cost, cooldown);
        this.radius = radius;
        this.targetGround = targetGround;
    }

    @Override
    protected void onCast(ServerPlayer player) {
        Vec3 center = targetGround ? getTargetGround(player, 20.0) : player.position();

        playAreaVisuals(player, center, radius);

        List<LivingEntity> targets = TargetingLib.getEntitiesInRadius(player.level(), center, radius, player);

        for (LivingEntity target : targets) {
            onAffectEntity(player, target);
        }
    }

    protected abstract void playAreaVisuals(ServerPlayer caster, Vec3 center, double radius);
    protected abstract void onAffectEntity(ServerPlayer caster, LivingEntity target);
}
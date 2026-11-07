package com.cuzzi.reforged.server.magic.spells.archetypes;

import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class FieldSpell extends Spell {

    private final double radius;
    private final int durationTicks;
    private final int tickInterval;

    // ACTUALIZADO: Ahora recibe los 7 argumentos base + los 3 específicos del Field
    public FieldSpell(String id, String name, String desc, String iconPath, StatType resourceType, int cost, int cooldown, double radius, int durationTicks, int tickInterval) {
        super(id, name, desc, iconPath, resourceType, cost, cooldown); // Pasa los 7 a Spell
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.tickInterval = tickInterval;
    }

    @Override
    protected void onCast(ServerPlayer player) {
        Vec3 pos = getTargetGround(player, 15.0);

        onFieldCreated(player, pos); // Spawnea runas gigantes o efectos visuales

        SpellFieldEntity field = new SpellFieldEntity(player.level(), pos, player, this, radius, durationTicks, tickInterval);
        player.level().addFreshEntity(field);
    }

    public abstract void onFieldCreated(ServerPlayer caster, Vec3 pos);
    public abstract void onFieldTick(SpellFieldEntity field); // Para emitir partículas continuas
    public abstract void onAffectEntity(ServerPlayer caster, LivingEntity target);
}
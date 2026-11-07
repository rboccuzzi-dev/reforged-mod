package com.cuzzi.reforged.server.magic.spells.impl;

import com.cuzzi.reforged.server.magic.lib.DamageLib;
import com.cuzzi.reforged.server.magic.lib.VisualLib;
import com.cuzzi.reforged.server.magic.spells.archetypes.FieldSpell;
import com.cuzzi.reforged.server.magic.spells.archetypes.SpellFieldEntity;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HealingSanctuarySpell extends FieldSpell {

    public HealingSanctuarySpell() {
        super(
                "healing_sanctuary",                          // ID
                "Santuario de Curación",                      // Nombre en UI
                "Crea un área que cura a los aliados.",       // Descripción
                "reforged:textures/gui/skills/healing.png",   // Ícono
                StatType.MANA,                                // Tipo de recurso
                50,                                           // Costo
                400,                                          // Cooldown (20s)
                4.0,                                          // Radio
                200,                                          // Duración (10s)
                20                                            // Tick interval (1s)
        );
    }

    @Override
    public void onFieldCreated(ServerPlayer caster, Vec3 pos) {
        // Dibuja una Runa curativa verde en el piso que gira suavemente
        VisualLib.spawnFloorRune((ServerLevel) caster.level(), pos, 4.0f, 1.5f, 200, "reforged:textures/entity/rune/placeholder_rune.png", 0x00FF00);
    }

    @Override
    public void onFieldTick(SpellFieldEntity field) {
        // Emite chispas verdes curativas flotando hacia arriba constantemente
        if (field.tickCount % 5 == 0) {
            VisualLib.spawnBurst((ServerLevel) field.level(), field.position(), ParticleTypes.HAPPY_VILLAGER, 5, 0.5);
        }
    }

    @Override
    public void onAffectEntity(ServerPlayer caster, LivingEntity target) {
        // Solo cura a jugadores o mascotas (puedes ajustar el filtro)
        if (target instanceof net.minecraft.world.entity.player.Player || target == caster) {
            // Curación base 2 + 10% de la Inteligencia
            float healAmount = DamageLib.calculateMagicDamage(caster, 2.0f, 0.1f);
            target.heal(healAmount);
        }
    }
}
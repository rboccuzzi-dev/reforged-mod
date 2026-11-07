package com.cuzzi.reforged.registry;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.server.magic.RuneEntity;
import com.cuzzi.reforged.server.magic.spells.archetypes.SpellFieldEntity;
import com.cuzzi.reforged.server.magic.spells.archetypes.SpellProjectileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reforged.MODID);

    public static final Supplier<EntityType<RuneEntity>> RUNE = ENTITIES.register("rune", () ->
            EntityType.Builder.<RuneEntity>of(RuneEntity::new, MobCategory.MISC)
                    .sized(2.0f, 0.1f) // Tamaño base de la hitbox (plana)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .build("rune")
    );

    public static final Supplier<EntityType<SpellProjectileEntity>> SPELL_PROJECTILE = ENTITIES.register("spell_projectile", () ->
            EntityType.Builder.<SpellProjectileEntity>of(SpellProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(64).updateInterval(1).build("spell_projectile")
    );

    public static final Supplier<EntityType<SpellFieldEntity>> SPELL_FIELD = ENTITIES.register("spell_field", () ->
            EntityType.Builder.<SpellFieldEntity>of(SpellFieldEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f).clientTrackingRange(64).build("spell_field")
    );
}
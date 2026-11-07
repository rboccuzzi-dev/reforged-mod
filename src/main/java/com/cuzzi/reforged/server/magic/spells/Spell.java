package com.cuzzi.reforged.server.magic.spells;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Spell {

    private final String id;
    private final Component name;
    private final Component description;
    private final ResourceLocation icon;

    private final StatType resourceType; // MANA, AURA, o DEMONIC_POWER
    private final int cost;
    private final int cooldownTicks;

    public Spell(String id, String name, String desc, String iconPath, StatType resourceType, int cost, int cooldownTicks) {
        this.id = id;
        this.name = Component.literal(name);
        this.description = Component.literal(desc);
        this.icon = ResourceLocation.parse(iconPath); // Ej: "reforged:textures/gui/skills/fireball.png"
        this.resourceType = resourceType;
        this.cost = cost;
        this.cooldownTicks = cooldownTicks;
    }

    public String getId() { return id; }
    public Component getName() { return name; }
    public Component getDescription() { return description; }
    public ResourceLocation getIcon() { return icon; }
    public StatType getResourceType() { return resourceType; }
    public int getCost() { return cost; }
    public int getCooldownTicks() { return cooldownTicks; }

    public boolean canCast(ServerPlayer player, PlayerStatsData data) {
        if (data.isOnCooldown(this.id)) return false;
        return data.current(this.resourceType, player) >= this.cost;
    }

    public boolean cast(ServerPlayer player) {
        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return false;

        // 1. Revisar Enfriamiento
        if (data.isOnCooldown(this.id)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§e" + this.getName().getString() + " está en enfriamiento."), true);
            return false;
        }

        // 2. Revisar Maná / Aura
        if (data.current(this.resourceType, player) < this.cost) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo tienes suficiente " + this.resourceType.name() + " para " + this.getName().getString()), true);
            return false;
        }

        // 3. Descontar recurso
        if (this.cost > 0) {
            if (this.resourceType == StatType.MANA) data.setCurrentMana(data.currentMana() - this.cost);
            else if (this.resourceType == StatType.AURA) data.setCurrentAura(data.currentAura() - this.cost);
        }

        // 4. Aplicar Cooldown
        data.setCooldown(this.id, this.cooldownTicks);
        com.cuzzi.reforged.server.StatsService.refresh(player);

        // 5. Ejecutar la magia
        onCast(player);
        return true;
    }
    protected abstract void onCast(ServerPlayer player);

    // --- MÉTODOS UTILITARIOS DE APUNTADO ---

    /**
     * Devuelve el punto exacto en el suelo/bloque al que mira el jugador.
     */
    protected Vec3 getTargetGround(ServerPlayer player, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);

        HitResult result = player.level().clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getLocation();
        }
        return endPos; // Si no choca con nada, devuelve el punto máximo en el aire
    }

    /**
     * Devuelve un punto flotante frente a la cámara del jugador.
     */
    protected Vec3 getPointInFront(ServerPlayer player, double distance) {
        return player.getEyePosition().add(player.getLookAngle().scale(distance));
    }
}
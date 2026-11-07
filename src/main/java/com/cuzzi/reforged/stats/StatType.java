package com.cuzzi.reforged.stats;

import net.minecraft.network.chat.Component;

public enum StatType {
    // Recursos derivados (No alocables, no reciben bufos globales)
    HEALTH("health", false, false, false, false),
    MANA("mana", false, false, false, false),

    // Stats Base Alocables (Reciben el bufo global "+X a todas las stats")
    STRENGTH("strength", false, true, true, false),
    AGILITY("agility", false, true, true, false),
    INTELLIGENCE("intelligence", false, true, true, false),
    DEXTERITY("dexterity", false, true, true, false),
    STAMINA("stamina", false, true, true, false),
    ENDURANCE("endurance", false, true, true, false),

    // Stats Especiales / Desbloqueables
    AURA("aura", true, false, false, true),
    COURAGE("courage", true, false, false, true),
    DEFENSE("defense", true, false, false, false),
    DEITY("deity", true, false, false, true),
    DEMONIC_POWER("demonic_power", true, false, false, true),
    DOMINANCE("dominance", true, false, false, true),
    FIGHTING_SPIRIT("fighting_spirit", true, false, false, true),
    INDOMITABLE("indomitable", true, false, false, true),
    LUCK("luck", true, false, true, true),
    INSIGHT("insight", true, false, false, true),
    LEADERSHIP("leadership", true, false, false, true),
    PERSISTENCE("persistence", true, false, false, true),
    PERSUASION("persuasion", true, false, false, true),
    REGENERATION("regeneration", true, false, false, false),
    WILLPOWER("willpower", true, false, false, true);

    private final String id;
    private final boolean hidden;
    private final boolean allocatable;
    private final boolean receivesAllBuffs; // ¿Recibe el bufo "+X a todas las stats alocables"?
    private final boolean unlockable;       // ¿Se puede desbloquear mediante títulos/eventos?

    StatType(String id, boolean hidden, boolean allocatable, boolean receivesAllBuffs, boolean unlockable) {
        this.id = id;
        this.hidden = hidden;
        this.allocatable = allocatable;
        this.receivesAllBuffs = receivesAllBuffs;
        this.unlockable = unlockable;
    }

    public static StatType byId(String id) {
        for (StatType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }

    public String id() { return id; }
    public boolean isHidden() { return hidden; }
    public boolean isAllocatable() { return allocatable; }
    public boolean receivesAllBuffs() { return receivesAllBuffs; }
    public boolean isUnlockable() { return unlockable; }

    public Component displayName() {
        return Component.translatable("stat.reforged." + id);
    }
}
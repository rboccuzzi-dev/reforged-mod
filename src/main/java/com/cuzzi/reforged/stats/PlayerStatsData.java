package com.cuzzi.reforged.stats;

import com.cuzzi.reforged.rarity.JobRarityRules;
import com.cuzzi.reforged.titles.TitleDefinition;
import com.cuzzi.reforged.titles.TitleRegistry;
import com.cuzzi.reforged.titles.TitleStatBonusSource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PlayerStatsData {

    public static final Codec<PlayerStatsData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("class", "warrior").forGetter(d -> d.playerClass.id()),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("points", Map.of())
                    .forGetter(PlayerStatsData::committedAsStrings),
            Codec.INT.optionalFieldOf("available", 0).forGetter(d -> d.availablePoints),
            Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
            Codec.INT.optionalFieldOf("mana", 100).forGetter(d -> d.currentMana),
            Codec.INT.optionalFieldOf("aura", 100).forGetter(d -> d.currentAura),
            Codec.INT.optionalFieldOf("demonic_power", 0).forGetter(d -> d.demonicPower),
            Codec.INT.optionalFieldOf("courage", 0).forGetter(d -> d.courage),
            Codec.STRING.listOf().optionalFieldOf("revealed", List.of())
                    .forGetter(PlayerStatsData::revealedAsStrings),
            Codec.INT.optionalFieldOf("awakening", 1).forGetter(d -> d.awakeningStage),
            Codec.STRING.listOf().optionalFieldOf("unlocked_titles", List.of())
                    .forGetter(d -> new ArrayList<>(d.unlockedTitles)),
            Codec.STRING.optionalFieldOf("active_title", "").forGetter(d -> d.activeTitle),
            Codec.STRING.optionalFieldOf("display_title", "").forGetter(d -> d.displayTitle),

            // --- NUEVOS CAMPOS AÑADIDOS ---
            Codec.STRING.listOf().optionalFieldOf("unlocked_skills", List.of())
                    .forGetter(d -> new ArrayList<>(d.unlockedSkills)),
            Codec.STRING.listOf().optionalFieldOf("skill_slots", Arrays.asList("", "", "", "", ""))
                    .forGetter(d -> Arrays.asList(d.skillSlots))

    ).apply(inst, PlayerStatsData::fromCodec));

    private static final List<StatBonusSource> BONUS_SOURCES = new ArrayList<>();

    public static void registerBonusSource(StatBonusSource source) {
        BONUS_SOURCES.add(source);
    }

    private float savedHealth = -1.0f;
    private ClassDefinition playerClass = ClassDefinition.WARRIOR;
    private final Map<StatType, Integer> committed = new EnumMap<>(StatType.class);
    private final Map<StatType, Integer> pending = new EnumMap<>(StatType.class);
    private final Set<StatType> revealed = EnumSet.noneOf(StatType.class);
    private int availablePoints = ClassDefinition.WARRIOR.startingPoints();
    private int level = 1;
    private int xp = 0;

    private int currentMana = 100;
    private int currentAura = 100;
    private int demonicPower = 0;
    private int courage = 0;

    // Food Buff System (+1% per shank for 2 min)
    private float foodBuffPercent = 0.0f;
    private int foodBuffTicks = 0;

    private int awakeningStage = 1;

    private final Set<String> unlockedTitles = new HashSet<>();
    private String activeTitle = "";
    private String displayTitle = "";

    private static PlayerStatsData fromCodec(String classId, Map<String, Integer> points, int available,
                                             int level, int xp, int mana, int aura, int demonicPower, int courage,
                                             List<String> revealed, int awakening, List<String> unlockedTitles,
                                             String activeTitle, String displayTitle,
                                             List<String> unlockedSkills, List<String> skillSlots) {
        PlayerStatsData d = new PlayerStatsData();
        d.playerClass = ClassDefinition.byId(classId);
        points.forEach((id, value) -> {
            StatType type = StatType.byId(id);
            if (type != null && value > 0) d.committed.put(type, value);
        });
        d.availablePoints = Math.max(0, available);
        d.level = Math.max(1, level);
        d.xp = Math.max(0, xp);
        d.currentMana = mana;
        d.currentAura = aura;
        d.demonicPower = demonicPower;
        d.courage = courage;
        d.awakeningStage = Math.max(1, Math.min(4, awakening));
        for (String id : revealed) {
            StatType type = StatType.byId(id);
            if (type != null) d.revealed.add(type);
        }
        if (unlockedTitles != null) d.unlockedTitles.addAll(unlockedTitles);
        d.activeTitle = activeTitle != null ? activeTitle : "";
        d.displayTitle = displayTitle != null ? displayTitle : "";

        // --- ASIGNACIÓN DE LAS SKILLS ---
        if (unlockedSkills != null) d.unlockedSkills.addAll(unlockedSkills);
        if (skillSlots != null) {
            for (int i = 0; i < 5 && i < skillSlots.size(); i++) {
                String s = skillSlots.get(i);
                d.skillSlots[i] = (s == null) ? "" : s; // Si es null, lo reemplaza por ""
            }
        }

        return d;
    }
    private Map<String, Integer> committedAsStrings() {
        Map<String, Integer> map = new HashMap<>();
        committed.forEach((type, value) -> map.put(type.id(), value));
        return map;
    }

    private List<String> revealedAsStrings() {
        List<String> list = new ArrayList<>();
        for (StatType type : revealed) list.add(type.id());
        return list;
    }

    private final Set<String> unlockedSkills = new HashSet<>();
    private final String[] skillSlots = {"", "", "", "", ""};
    private final Map<String, Integer> cooldowns = new HashMap<>();

    // --- MÉTODOS DE SKILLS ---
    public Set<String> unlockedSkills() { return unlockedSkills; }

    public void unlockSkill(String skillId) { unlockedSkills.add(skillId); }

    public String[] skillSlots() { return skillSlots; }

    public void assignSkill(int slot, String skillId) {
        if (slot >= 0 && slot < 5) {
            skillSlots[slot] = (skillId == null) ? "" : skillId;
        }
    }

    // --- COOLDOWNS ---
    public void setCooldown(String skillId, int ticks) {
        cooldowns.put(skillId, ticks);
    }

    public boolean isOnCooldown(String skillId) {
        return cooldowns.getOrDefault(skillId, 0) > 0;
    }

    public int getCooldown(String skillId) {
        return cooldowns.getOrDefault(skillId, 0);
    }

    public boolean tickCooldowns() {
        boolean changed = false;
        for (var iterator = cooldowns.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            int current = entry.getValue();
            if (current > 1) {
                entry.setValue(current - 1);
            } else {
                // El cooldown llegó a 0
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    public void copyFrom(PlayerStatsData other) {
        this.playerClass = other.playerClass;
        this.committed.clear();
        this.committed.putAll(other.committed);
        this.revealed.clear();
        this.revealed.addAll(other.revealed);
        this.availablePoints = other.availablePoints;
        this.level = other.level;
        this.xp = other.xp;
        this.currentMana = other.currentMana;
        this.currentAura = other.currentAura;
        this.demonicPower = other.demonicPower;
        this.courage = other.courage;
        this.awakeningStage = other.awakeningStage;

        this.unlockedTitles.clear();
        this.unlockedTitles.addAll(other.unlockedTitles);
        this.activeTitle = other.activeTitle;
        this.displayTitle = other.displayTitle;

        this.foodBuffPercent = other.foodBuffPercent;
        this.foodBuffTicks = other.foodBuffTicks;
        this.pending.clear();

        this.unlockedSkills.clear();
        this.unlockedSkills.addAll(other.unlockedSkills);
        System.arraycopy(other.skillSlots, 0, this.skillSlots, 0, 5);
        this.cooldowns.clear();
        this.cooldowns.putAll(other.cooldowns);
    }

    public PlayerStatsData copy() {
        PlayerStatsData copy = new PlayerStatsData();
        copy.copyFrom(this);
        return copy;
    }

    public float savedHealth() { return savedHealth; }
    public void setSavedHealth(float health) { this.savedHealth = health; }

    public ClassDefinition playerClass() { return playerClass; }
    public void setPlayerClass(ClassDefinition newClass) {
        refundAll();
        this.playerClass = newClass;
    }

    public boolean isMeleeFighter() {
        return "fighter".equalsIgnoreCase(playerClass.id()) || "warrior".equalsIgnoreCase(playerClass.id());
    }

    public void refundAll() {
        for (int value : committed.values()) availablePoints += value;
        committed.clear();
        pending.clear();
    }

    public int level() { return level; }
    public void setLevel(int newLevel) {
        this.level = Math.max(1, newLevel);
        this.xp = 0;
    }

    public int xp() { return xp; }
    public int xpToNext() {
        return Math.round((100 + (level - 1) * 50) * JobRarityRules.levelXpMultiplier(playerClass.rarity()));
    }
    public float xpProgress() { return Math.min(1f, xp / (float) xpToNext()); }
    public int pointsPerLevel() { return JobRarityRules.pointsPerLevel(playerClass.rarity()); }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNext()) {
            xp -= xpToNext();
            level++;
            availablePoints += pointsPerLevel();
        }
    }

    // --- Max Resource Calculations (Mana scales +3 per Intelligence point) ---
    public int maxMana(Player player) {
        int intelligence = appliedValue(StatType.INTELLIGENCE) + (player != null ? bonus(StatType.INTELLIGENCE, player) : 0);
        int baseMana = 100 + (intelligence * 3);
        int titleB = player != null ? titleBonus(StatType.MANA, player) : 0;
        int itemB = player != null ? itemBonus(StatType.MANA, player) : 0;
        return baseMana + titleB + itemB;
    }

    public int maxAura(Player player) {
        int titleB = player != null ? titleBonus(StatType.AURA, player) : 0;
        int itemB = player != null ? itemBonus(StatType.AURA, player) : 0;
        return 100 + titleB + itemB;
    }

    // --- Food Buff System ---
    public float foodBuffPercent() { return foodBuffTicks > 0 ? foodBuffPercent : 0.0f; }
    public int foodBuffTicks() { return foodBuffTicks; }

    public void applyFoodBuff(float percent, int durationTicks) {
        this.foodBuffPercent = percent;
        this.foodBuffTicks = durationTicks;
    }

    public void tickFoodBuff() {
        if (this.foodBuffTicks > 0) {
            this.foodBuffTicks--;
            if (this.foodBuffTicks <= 0) {
                this.foodBuffPercent = 0.0f;
            }
        }
    }

    public int courage() { return courage; }
    public void addCourage(int amount) { this.courage = Math.max(0, this.courage + amount); }

    public int demonicPower() { return demonicPower; }
    public void addDemonicPower(int amount) { this.demonicPower = Math.max(0, this.demonicPower + amount); }

    public int currentMana() { return currentMana; }
    public void setCurrentMana(int mana) { this.currentMana = Math.max(0, mana); }

    public int currentAura() { return currentAura; }
    public void setCurrentAura(int aura) { this.currentAura = Math.max(0, aura); }

    public int awakeningStage() { return awakeningStage; }
    public void setAwakeningStage(int stage) { this.awakeningStage = Math.max(1, Math.min(4, stage)); }

    public void addPoints(int amount) { availablePoints = Math.max(0, availablePoints + amount); }
    public void setAvailablePoints(int amount) { availablePoints = Math.max(0, amount); }

    public int committed(StatType type) { return committed.getOrDefault(type, 0); }
    public int pending(StatType type) { return pending.getOrDefault(type, 0); }

    private int pendingTotal() {
        int total = 0;
        for (int p : pending.values()) total += p;
        return total;
    }

    public int remainingPoints() { return availablePoints - pendingTotal(); }
    public boolean hasPending() { return pendingTotal() > 0; }

    public int maxPoints(StatType type) {
        ClassDefinition.StatRule rule = playerClass.rule(type);
        if (rule == null) return 0;
        return Math.max(0, (rule.max() - rule.base()) / rule.step());
    }

    public boolean canIncrease(StatType type) {
        return remainingPoints() > 0 && (committed(type) + pending(type)) < maxPoints(type);
    }

    public boolean canDecrease(StatType type) { return pending(type) > 0; }

    public void increase(StatType type) {
        if (!canIncrease(type)) return;
        pending.merge(type, 1, Integer::sum);
    }

    public void decrease(StatType type) {
        if (!canDecrease(type)) return;
        pending.merge(type, -1, Integer::sum);
    }

    public void clearPending() { pending.clear(); }

    public void setCommitted(StatType type, int points) {
        int clamped = Math.max(0, Math.min(points, maxPoints(type)));
        if (clamped == 0) {
            committed.remove(type);
        } else {
            committed.put(type, clamped);
        }
        pending.remove(type);
    }

    public boolean applyAllocation(Map<String, Integer> amounts) {
        Map<StatType, Integer> parsed = new EnumMap<>(StatType.class);
        List<StatType> visible = visibleStats();
        long total = 0;

        for (Map.Entry<String, Integer> e : amounts.entrySet()) {
            StatType type = StatType.byId(e.getKey());
            int value = e.getValue();
            if (type == null || value < 0 || !visible.contains(type)) return false;
            if (value == 0) continue;
            if ((long) committed(type) + value > maxPoints(type)) return false;
            parsed.put(type, value);
            total += value;
        }

        if (total <= 0 || total > availablePoints) return false;

        parsed.forEach((type, value) -> committed.merge(type, value, Integer::sum));
        availablePoints -= (int) total;
        pending.clear();
        return true;
    }

    public int baseValue(StatType type) {
        ClassDefinition.StatRule rule = playerClass.rule(type);
        return rule != null ? rule.base() + (committed(type) + pending(type)) * rule.step() : 0;
    }

    public int appliedValue(StatType type) {
        ClassDefinition.StatRule rule = playerClass.rule(type);
        return rule != null ? rule.base() + committed(type) * rule.step() : 0;
    }

    public int bonus(StatType type, Player player) {
        if (player == null) return 0;
        int total = 0;
        for (StatBonusSource source : BONUS_SOURCES) {
            total += source.bonus(player, type);
        }
        return total;
    }

    public int itemBonus(StatType type, Player player) {
        if (player == null) return 0;
        int total = 0;
        for (StatBonusSource source : BONUS_SOURCES) {
            if (source instanceof com.cuzzi.reforged.items.integration.ItemStatBonusSource) {
                total += source.bonus(player, type);
            }
        }
        return total;
    }

    public int titleBonus(StatType type, Player player) {
        if (player == null) return 0;
        int total = 0;
        for (StatBonusSource source : BONUS_SOURCES) {
            if (source instanceof TitleStatBonusSource) {
                total += source.bonus(player, type);
            }
        }
        return total;
    }

    public int current(StatType type, Player player) {
        return switch (type) {
            case HEALTH -> player != null ? (int) Math.ceil(player.getHealth()) : baseValue(type);
            case MANA -> currentMana;
            case AURA -> currentAura;
            case DEMONIC_POWER -> demonicPower;
            default -> baseValue(type);
        };
    }

    public void reveal(StatType type) { revealed.add(type); }

    /**
     * Checks title list dynamically: AURA disappears as soon as aura titles are removed.
     */
    public List<StatType> visibleStats() {
        List<StatType> list = new ArrayList<>();
        for (StatType type : StatType.values()) {
            if (type == StatType.COURAGE) continue;

            if (type == StatType.AURA && isMeleeFighter()) {
                list.add(type);
                continue;
            }
            if (type == StatType.MANA && isMeleeFighter()) {
                continue;
            }

            boolean unlockedByTitle = false;
            for (String titleId : unlockedTitles) {
                TitleDefinition def = TitleRegistry.get(titleId);
                if (def != null && def.unlockedStats().contains(type)) {
                    unlockedByTitle = true;
                    break;
                }
            }

            if (!type.isHidden() || revealed.contains(type) || unlockedByTitle) {
                list.add(type);
            }
        }
        return list;
    }

    public Set<String> unlockedTitles() { return unlockedTitles; }
    public String activeTitle() { return activeTitle; }
    public String displayTitle() { return displayTitle; }

    public void setActiveTitle(String titleId) {
        if (titleId == null || titleId.isEmpty() || unlockedTitles.contains(titleId)) {
            this.activeTitle = titleId != null ? titleId : "";
        }
    }

    public void setDisplayTitle(String titleId) {
        if (titleId == null || titleId.isEmpty() || unlockedTitles.contains(titleId)) {
            this.displayTitle = titleId != null ? titleId : "";
        }
    }

    public void unlockTitle(String titleId) {
        unlockedTitles.add(titleId);
    }

    public void removeTitle(String titleId) {
        unlockedTitles.remove(titleId);
        if (titleId.equals(activeTitle)) {
            activeTitle = "";
        }
        if (titleId.equals(displayTitle)) {
            displayTitle = activeTitle.isEmpty() ? "" : activeTitle;
        }
    }

    public TitleDefinition displayTitleDefinition() {
        return TitleRegistry.get(displayTitle);
    }

    public TitleDefinition activeTitleDefinition() {
        return TitleRegistry.get(activeTitle);
    }
}
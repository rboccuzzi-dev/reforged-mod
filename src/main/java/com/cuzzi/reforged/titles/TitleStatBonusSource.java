package com.cuzzi.reforged.titles;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatBonusSource;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.world.entity.player.Player;

public class TitleStatBonusSource implements StatBonusSource {

    public static void register() {
        PlayerStatsData.registerBonusSource(new TitleStatBonusSource());
    }

    @Override
    public int bonus(Player player, StatType stat) {
        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return 0;

        int total = 0;

        // 1. Inactive bonuses from ALL unlocked titles
        for (String titleId : data.unlockedTitles()) {
            TitleDefinition def = TitleRegistry.get(titleId);
            if (def != null) {
                total += def.inactiveBonuses().getOrDefault(stat, 0);
                total += def.inactiveAllStatsBonus();
            }
        }

        // 2. Active bonus from the SINGLE equipped active title
        String activeId = data.activeTitle();
        if (activeId != null && !activeId.isEmpty() && data.unlockedTitles().contains(activeId)) {
            TitleDefinition activeDef = TitleRegistry.get(activeId);
            if (activeDef != null) {
                total += activeDef.activeBonuses().getOrDefault(stat, 0);
                total += activeDef.activeAllStatsBonus();
            }
        }

        return total;
    }
}
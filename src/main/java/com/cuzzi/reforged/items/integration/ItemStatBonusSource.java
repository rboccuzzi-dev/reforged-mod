package com.cuzzi.reforged.items.integration;

import com.cuzzi.reforged.items.components.ReforgedItemData;
import com.cuzzi.reforged.items.requirement.RequirementValidator;
import com.cuzzi.reforged.registry.ModDataComponents;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatBonusSource;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemStatBonusSource implements StatBonusSource {

    public static void register() {
        PlayerStatsData.registerBonusSource(new ItemStatBonusSource());
    }

    @Override
    public int bonus(Player player, StatType type) {
        int totalBonus = 0;
        if (player == null) return 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);

            if (!stack.isEmpty() && stack.has(ModDataComponents.REFORGED_ITEM_DATA)) {
                if (RequirementValidator.canUseItem(player, stack)) {
                    ReforgedItemData data = stack.get(ModDataComponents.REFORGED_ITEM_DATA);
                    if (data != null) {
                        // Check exact ID and lowercase ID
                        Double val = data.finalCustomStats().get(type.id());
                        if (val == null) {
                            val = data.finalCustomStats().get(type.id().toLowerCase());
                        }
                        if (val != null) {
                            totalBonus += val.intValue();
                        }
                    }
                }
            }
        }
        return totalBonus;
    }
}
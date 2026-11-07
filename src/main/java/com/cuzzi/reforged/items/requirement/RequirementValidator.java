package com.cuzzi.reforged.items.requirement;

import com.cuzzi.reforged.items.components.ReforgedItemData;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.registry.ModDataComponents;
import com.cuzzi.reforged.stats.PlayerStatsData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RequirementValidator {

    public static boolean canUseItem(Player player, ItemStack stack) {
        if (stack.isEmpty() || !stack.has(ModDataComponents.REFORGED_ITEM_DATA)) {
            return true;
        }

        ReforgedItemData itemData = stack.get(ModDataComponents.REFORGED_ITEM_DATA);
        if (itemData == null) return true;

        // Admin/command items bypass level checks
        if (itemData.isAdminCreation()) {
            return true;
        }

        PlayerStatsData playerStats = ModAttachments.stats(player);
        return playerStats.level() >= itemData.requiredLevel();
    }
}
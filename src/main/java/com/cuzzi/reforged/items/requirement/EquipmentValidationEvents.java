package com.cuzzi.reforged.items.requirement;

import com.cuzzi.reforged.Reforged;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Reforged.MODID)
public class EquipmentValidationEvents {

    // 1. Warn the player when they equip an item they can't use
    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack newStack = event.getTo();
        if (!newStack.isEmpty() && !RequirementValidator.canUseItem(player, newStack)) {
            player.displayClientMessage(
                    Component.literal("You do not meet the requirements to wield this item!")
                            .withStyle(ChatFormatting.RED),
                    true // true displays it on the action bar (above hotbar) so it doesn't spam chat
            );
        }
    }

    // 2. Prevent attacking entities with an invalid weapon
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();

        if (!RequirementValidator.canUseItem(player, weapon)) {
            event.setCanceled(true); // Cancels the attack completely
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("Weapon requirements not met!").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    // 3. Prevent right-clicking/using items (like blocking with a shield or drawing a bow)
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (!RequirementValidator.canUseItem(player, item)) {
            event.setCanceled(true);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("Item requirements not met!").withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
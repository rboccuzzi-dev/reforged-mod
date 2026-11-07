package com.cuzzi.reforged.server;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class StatsEvents {

    private StatsEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatsService.refresh(player, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStatsData data = ModAttachments.stats(player);
            if (data != null) {
                data.setSavedHealth(player.getHealth());
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatsService.refresh(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatsService.refresh(player);
        }
    }

    // --- ASSIGN MOB LEVEL ON SPAWN ---
    @SubscribeEvent
    public static void onMobJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Mob mob) {
            MobLevels.ensureLevel(mob);
        }
    }

    // --- SERVER TICK: MANA REGEN & NO-HUNGER & FOOD BUFF TICK ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return;

        // 1. Freeze Food level at max (No hunger loss)
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0f);

        // 2. Tick Food Buff duration
        data.tickFoodBuff();

        // 3. Mana Regeneration (1% of max mana per second = every 20 ticks)
        if (player.tickCount % 20 == 0) {
            int maxMana = data.maxMana(player);
            if (data.currentMana() < maxMana) {
                int regen = Math.max(1, Math.round(maxMana * 0.02f));
                data.setCurrentMana(Math.min(maxMana, data.currentMana() + regen));
                StatsService.refresh(player);
            }
        }
        if (data.tickCooldowns()) {
            com.cuzzi.reforged.server.StatsService.refresh(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            if (event.getEntity().getFoodData().getFoodLevel() >= 20) {
                event.getEntity().startUsingItem(event.getHand());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
            }
        }
    }

    // --- APPLY FOOD BUFF UPON EATING (+1% per shank for 2 min) ---
    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack stack = event.getItem();
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                int nutrition = food.nutrition();
                int shanks = Math.max(1, nutrition / 2);
                float buffPercent = shanks * 0.01f;

                PlayerStatsData data = ModAttachments.stats(player);
                if (data != null) {
                    data.applyFoodBuff(buffPercent, 2400); // 2400 ticks = 2 minutes
                    player.sendSystemMessage(Component.literal("§a[Food Buff] §f+" + shanks + "% stats for 2 minutes!"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity().level().isClientSide()) return;
        int level = event.getEntity().getData(ModAttachments.PLAYER_LEVEL);
        if (level > 0) {
            event.setDisplayname(Component.literal("[Lv. " + level + "] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(event.getDisplayname()));
        }
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        int level = event.getEntity().getData(ModAttachments.PLAYER_LEVEL);
        if (level <= 0) return;
        Component base = event.getDisplayName() != null ? event.getDisplayName() : event.getEntity().getName();
        event.setDisplayName(Component.literal("[Lv. " + level + "] ")
                .withStyle(ChatFormatting.GOLD)
                .append(base));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        StatsCommands.register(event.getDispatcher());
        ItemCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // --- DEFENSIVE CALCULATIONS ---
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStatsData data = ModAttachments.stats(player);
            if (data != null) {
                float damage = event.getOriginalDamage();

                double def = data.appliedValue(StatType.DEFENSE) + data.bonus(StatType.DEFENSE, player);
                double maxReduction = data.level() < 200 ? 0.50 : 0.75;
                double defRatio = Math.min(def / 1000.0, 1.0);
                damage *= (float) (1.0 - (defRatio * maxReduction));

                double indomitable = data.appliedValue(StatType.INDOMITABLE);
                if (indomitable > 0) {
                    float missingHpRatio = 1.0f - (player.getHealth() / player.getMaxHealth());
                    double chance = (indomitable * 0.005) * missingHpRatio;
                    if (player.getRandom().nextDouble() < chance) {
                        damage *= 0.5f;
                    }
                }

                if (event.getSource().getEntity() instanceof Mob mob) {
                    int mobLevel = mob.getData(ModAttachments.MOB_LEVEL);
                    int levelGap = mobLevel - data.level();
                    if (levelGap > 10) {
                        float damageMultiplier = 1.0f + (levelGap * 0.05f);
                        damage *= damageMultiplier;
                    }
                }

                event.setNewDamage(damage);
            }
        }

        // --- OFFENSIVE CALCULATIONS ---
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            PlayerStatsData data = ModAttachments.stats(player);
            if (data != null) {
                float damage = event.getOriginalDamage();

                if (data.foodBuffPercent() > 0) {
                    damage *= (1.0f + data.foodBuffPercent());
                }

                int courageTier = (int) (data.appliedValue(StatType.COURAGE) / 10.0);
                damage *= (1.0f + (courageTier * 0.1f));

                double fightingSpirit = data.appliedValue(StatType.FIGHTING_SPIRIT);
                if (fightingSpirit > 0) {
                    float missingHpRatio = 1.0f - (player.getHealth() / player.getMaxHealth());
                    damage *= (float) (1.0 + (fightingSpirit * 0.01 * missingHpRatio));
                }

                double aura = data.appliedValue(StatType.AURA);
                if (aura > 0) {
                    damage += (float) (aura * 0.25);
                }

                if (event.getEntity() instanceof Mob mob) {
                    int mobLevel = mob.getData(ModAttachments.MOB_LEVEL);
                    int levelGap = mobLevel - data.level();
                    if (levelGap > 10) {
                        float reductionFactor = (float) Math.pow(0.96, levelGap - 10);
                        reductionFactor = Math.max(0.02f, reductionFactor);
                        damage *= reductionFactor;
                    }
                }

                event.setNewDamage(damage);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return;

        int mobLevel = mob.getData(ModAttachments.MOB_LEVEL);
        if (mobLevel <= 0) return;

        StatsService.handleMobKill(player, mobLevel);

        if (data.appliedValue(StatType.AURA) == 0) {
            data.addDemonicPower(1);
        }

        int levelDiff = mobLevel - data.level();
        if (levelDiff >= 50) {
            int pointsGained = (levelDiff >= 100) ? 2 : 1;
            data.addCourage(pointsGained);

            player.sendSystemMessage(Component.literal("§6[Courage] §fYou have shown bravery by defeating a superior foe! (+" + pointsGained + " Courage)"));

            if (data.courage() >= 10 && !data.unlockedTitles().contains("courageous_slayer")) {
                data.unlockTitle("courageous_slayer");
                player.sendSystemMessage(Component.literal("§a§lMILESTONE REACHED! §fYou have unlocked the legendary title §b[Courageous Slayer]§f."));
            }
        }

        StatsService.refresh(player);
    }
}
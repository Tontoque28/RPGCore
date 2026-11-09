package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.List;

public class DungeonRewardHandler {

    public static void register() {
        LivingDeathEvent.BUS.addListener(DungeonRewardHandler::onBossDefeatedForHearts);
    }


    public static void otorgarRecompensaMazmorra(Player player, int corazonesExtra) {
        if (player.level().isClientSide() || corazonesExtra <= 0) return;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
            cap.addLives(corazonesExtra);
            int totalVidas = cap.getLives();

            GhostDeathHandler.syncMaxHealthServer(player);
            player.setHealth(player.getMaxHealth());
            GhostDeathHandler.sendLivesSync(player, totalVidas);

            player.sendSystemMessage(
                    Component.literal("[SYSTEM] ¡Has obtenido +" + corazonesExtra + " corazones de vitalidad! (Total: " + totalVidas + ")")
                            .withStyle(ChatFormatting.GOLD));
        });
    }

    public static boolean onBossDefeatedForHearts(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return false;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return false;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (entityId == null) return false;

        BossReward reward = findReward(entityId.toString());
        if (reward != null) {
            event.getEntity().spawnAtLocation(
                    (net.minecraft.server.level.ServerLevel) event.getEntity().level(),
                    new ItemStack(RpgCore.LIFE_HEART.get(), reward.hearts),
                    0.5F
            );
            player.sendSystemMessage(Component.literal(
                    "[RPGCore] The boss dropped " + reward.hearts
                            + " Life Heart(s) [Tier " + reward.tier + "].")
                    .withStyle(ChatFormatting.GOLD));
        }
        return false;
    }

    private static BossReward findReward(String entityId) {
        List<? extends String> configuredRewards = RpgCoreConfig.BOSS_REWARDS.get();
        for (String entry : configuredRewards) {
            String[] parts = entry.split("\\|", -1);
            if (parts.length != 3 || !parts[0].equals(entityId)) {
                continue;
            }
            try {
                int hearts = Integer.parseInt(parts[2]);
                if (hearts > 0) {
                    return new BossReward(parts[1], hearts);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private record BossReward(String tier, int hearts) {
    }
}
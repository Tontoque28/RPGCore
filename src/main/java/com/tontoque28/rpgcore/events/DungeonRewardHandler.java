package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DungeonRewardHandler {

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(DungeonRewardHandler::onBossDefeatedForHearts);
    }

    public static void otorgarRecompensaMazmorra(Player player, int corazonesExtra) {
        if (player.level.isClientSide || corazonesExtra <= 0) return;

        PlayerLives.addLives(player, corazonesExtra);
        int totalVidas = PlayerLives.getLives(player);

        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        GhostDeathHandler.sendLivesSync(player, totalVidas);

        player.sendMessage(
                new TextComponent("[SYSTEM] ¡Has obtenido +" + corazonesExtra + " corazones de vitalidad! (Total: " + totalVidas + ")")
                        .withStyle(ChatFormatting.GOLD),
                Util.NIL_UUID
        );
    }

    public static void onBossDefeatedForHearts(ServerLevel world, Entity attacker, LivingEntity entity) {
        if (entity.level.isClientSide) return;
        if (!(attacker instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) attacker;

        ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) return;

        BossReward reward = findReward(entityId.toString());
        if (reward != null) {
            entity.spawnAtLocation(
                    new ItemStack(RpgCore.LIFE_HEART, reward.hearts),
                    0.5F
            );
            player.sendMessage(new TextComponent(
                    "[RPGCore] The boss dropped " + reward.hearts
                            + " Life Heart(s) [Tier " + reward.tier + "].")
                    .withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
        }
    }

    private static BossReward findReward(String entityId) {
        List<String> configuredRewards = RpgCoreConfig.getBossRewards();
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

    private static final class BossReward {
        final String tier;
        final int hearts;

        BossReward(String tier, int hearts) {
            this.tier = tier;
            this.hearts = hearts;
        }
    }
}

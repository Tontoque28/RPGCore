package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DungeonRewardHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(DungeonRewardHandler::onBossDefeatedForHearts);
    }

    public static void otorgarRecompensaMazmorra(Player player, int corazonesExtra) {
        if (player.level().isClientSide() || corazonesExtra <= 0) return;

        PlayerLives.addLives(player, corazonesExtra);
        int totalVidas = PlayerLives.getLives(player);

        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        GhostDeathHandler.sendLivesSync(player, totalVidas);

        player.sendSystemMessage(
                Component.literal("[SYSTEM] ¡Has obtenido +" + corazonesExtra + " corazones de vitalidad! (Total: " + totalVidas + ")")
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    public static void onBossDefeatedForHearts(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) return;

        BossReward reward = findReward(entityId.toString());
        if (reward != null) {
            entity.spawnAtLocation(
                    serverLevel,
                    new ItemStack(RpgCore.LIFE_HEART, reward.hearts),
                    0.5F
            );
            player.sendSystemMessage(Component.literal(
                    "[RPGCore] The boss dropped " + reward.hearts
                            + " Life Heart(s) [Tier " + reward.tier + "].")
                    .withStyle(ChatFormatting.GOLD));
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

    private record BossReward(String tier, int hearts) {
    }
}

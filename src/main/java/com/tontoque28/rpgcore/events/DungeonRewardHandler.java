package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class DungeonRewardHandler {

    public static void otorgarRecompensaMazmorra(PlayerEntity player, int corazonesExtra) {
        if (player.level.isClientSide() || corazonesExtra <= 0) return;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
            cap.addLives(corazonesExtra);
            int totalVidas = cap.getLives();

            GhostDeathHandler.syncMaxHealthServer(player);
            player.setHealth(player.getMaxHealth());
            GhostDeathHandler.sendLivesSync(player, totalVidas);

            player.sendMessage(
                    new StringTextComponent("[SYSTEM] ¡Has obtenido +" + corazonesExtra + " corazones de vitalidad! (Total: " + totalVidas + ")")
                            .withStyle(TextFormatting.GOLD), Util.NIL_UUID
            );
        });
    }

    @SubscribeEvent
    public static void onBossDefeatedForHearts(LivingDeathEvent event) {
        if (event.getEntity().level.isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getSource().getEntity();

        ResourceLocation entityId = ForgeRegistries.ENTITIES.getKey(event.getEntity().getType());
        if (entityId == null) return;

        BossReward reward = findReward(entityId.toString());
        if (reward != null) {
            event.getEntity().spawnAtLocation(
                    new ItemStack(RpgCore.LIFE_HEART.get(), reward.hearts),
                    0.5F
            );
            player.sendMessage(new StringTextComponent(
                    "[RPGCore] The boss dropped " + reward.hearts
                            + " Life Heart(s) [Tier " + reward.tier + "].")
                    .withStyle(TextFormatting.GOLD), Util.NIL_UUID);
        }
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

    private static final class BossReward {
        final String tier;
        final int hearts;

        BossReward(String tier, int hearts) {
            this.tier = tier;
            this.hearts = hearts;
        }
    }
}

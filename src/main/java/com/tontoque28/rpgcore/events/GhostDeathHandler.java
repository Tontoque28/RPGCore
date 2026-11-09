package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class GhostDeathHandler {

    static final String TAG_LIVES_SNAPSHOT = "rpgcore_lives_snapshot";
    static final String TAG_HAS_SNAPSHOT   = "rpgcore_has_snapshot";

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";

    public static void syncMaxHealthServer(Player player) {
        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
            int lives = cap.getLives();
            AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
                    attr.removeModifier(mod.id());
                }
                double maxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
                attr.setBaseValue(maxHp);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        });
    }

    public static void sendLivesSync(Player player, int lives) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer target : serverLevel.players()) {
            ModMessages.sendToPlayer(new SyncLivesPacket(player.getId(), lives), target);
        }
    }

    private static void setGhostAnchor(Player player) {
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());
    }

    private static void notifyLives(Player player, int lives) {
        player.displayClientMessage(
                Component.literal("[SYSTEM] " + player.getName().getString()
                        + " — vidas restantes: " + lives),
                false);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() > 1) {
                lives.removeLives(1);
                int livesNow = lives.getLives();

                player.getPersistentData().putInt(TAG_LIVES_SNAPSHOT, livesNow);
                player.getPersistentData().putBoolean(TAG_HAS_SNAPSHOT, true);

                syncMaxHealthServer(player);
                sendLivesSync(player, livesNow);
                notifyLives(player, livesNow);
                return;
            }

            event.setCanceled(true);
            lives.setLives(0);

            AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(2.0D);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                GhostRestrictionsHandler.applyGhostAbilities(serverPlayer);
            }

            setGhostAnchor(player);
            player.setHealth(1.0F);

            LostSoulEntity soul = LostSoulEntity.spawn(player);
            if (soul != null) {
                System.out.println("TINTRPG: " + player.getName().getString()
                        + " dejó su alma en " + soul.blockPosition());
            }
            System.out.println("TINTRPG: " + player.getName().getString() + " ahora es FANTASMA.");

            sendLivesSync(player, 0);
            notifyLives(player, 0);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        if (oldPlayer.getPersistentData().getBoolean(TAG_HAS_SNAPSHOT)) {
            int savedLives = oldPlayer.getPersistentData().getInt(TAG_LIVES_SNAPSHOT);
            newPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                cap.setLives(savedLives);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        int lives = player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(PlayerLives::getLives).orElse(10);

        syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        sendLivesSync(player, lives);
    }
}
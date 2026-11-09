package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
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

    public static void syncMaxHealthServer(PlayerEntity player) {
        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
            int lives = cap.getLives();
            ModifiableAttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
                    attr.removeModifier(mod.getId());
                }
                double maxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
                attr.setBaseValue(maxHp);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        });
    }

    public static void sendLivesSync(PlayerEntity player, int lives) {
        if (!(player.level instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverLevel = (ServerWorld) player.level;

        for (ServerPlayerEntity target : serverLevel.players()) {
            ModMessages.sendToPlayer(new SyncLivesPacket(player.getId(), lives), target);
        }
    }

    private static void setGhostAnchor(PlayerEntity player) {
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());
    }

    private static void notifyLives(PlayerEntity player, int lives) {
        player.displayClientMessage(
                new StringTextComponent("[SYSTEM] " + player.getName().getString()
                        + " — vidas restantes: " + lives),
                false);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity) || event.getEntity().level.isClientSide()) {
            return;
        }
        PlayerEntity player = (PlayerEntity) event.getEntity();

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

            ModifiableAttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(2.0D);
            }

            if (player instanceof ServerPlayerEntity) {
                GhostRestrictionsHandler.applyGhostAbilities((ServerPlayerEntity) player);
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

        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        if (oldPlayer.getPersistentData().getBoolean(TAG_HAS_SNAPSHOT)) {
            int savedLives = oldPlayer.getPersistentData().getInt(TAG_LIVES_SNAPSHOT);
            newPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                cap.setLives(savedLives);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide()) return;

        int lives = player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(PlayerLives::getLives).orElse(10);

        syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        sendLivesSync(player, lives);
    }
}

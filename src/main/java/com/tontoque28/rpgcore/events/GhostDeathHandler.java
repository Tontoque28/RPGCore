package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PersistentDataHolder;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.network.ModMessages;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class GhostDeathHandler {

    public static final String TAG_LIVES_SNAPSHOT = "rpgcore_lives_snapshot";
    public static final String TAG_HAS_SNAPSHOT = "rpgcore_has_snapshot";

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";

    public static void register() {
        ServerPlayerEvents.ALLOW_DEATH.register((player, source, amount) -> onPlayerDeath(player));

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> onPlayerCopy(oldPlayer, newPlayer, alive));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> onPlayerRespawn(newPlayer));
    }

    public static void syncMaxHealthServer(Player player) {
        int lives = PlayerLives.getLives(player);
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
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
    }

    public static void sendLivesSync(Player player, int lives) {
        if (!(player.level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) player.level;

        for (ServerPlayer target : serverLevel.players()) {
            ModMessages.sendSyncLives(target, player.getId(), lives);
        }
    }

    private static void setGhostAnchor(Player player) {
        CompoundTag data = PersistentDataHolder.of(player);
        data.putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        data.putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        data.putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());
    }

    private static void notifyLives(Player player, int lives) {
        player.displayClientMessage(
                new TextComponent("[SYSTEM] " + player.getName().getString()
                        + " — vidas restantes: " + lives),
                false);
    }

    private static boolean onPlayerDeath(Player player) {
        if (PlayerLives.getLives(player) > 1) {
            PlayerLives.removeLives(player, 1);
            int livesNow = PlayerLives.getLives(player);

            CompoundTag data = PersistentDataHolder.of(player);
            data.putInt(TAG_LIVES_SNAPSHOT, livesNow);
            data.putBoolean(TAG_HAS_SNAPSHOT, true);

            syncMaxHealthServer(player);
            sendLivesSync(player, livesNow);
            notifyLives(player, livesNow);
            return true;
        }

        PlayerLives.setLives(player, 0);

        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(2.0D);
        }

        if (player instanceof ServerPlayer) {
            GhostRestrictionsHandler.applyGhostAbilities((ServerPlayer) player);
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
        return false;
    }

    private static void onPlayerCopy(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        PlayerLives.copyFrom(oldPlayer, newPlayer);

        if (alive) {
            return;
        }

        CompoundTag oldData = PersistentDataHolder.of(oldPlayer);
        if (oldData.getBoolean(TAG_HAS_SNAPSHOT)) {
            PlayerLives.setLives(newPlayer, oldData.getInt(TAG_LIVES_SNAPSHOT));

            CompoundTag newData = PersistentDataHolder.of(newPlayer);
            newData.remove(TAG_LIVES_SNAPSHOT);
            newData.remove(TAG_HAS_SNAPSHOT);
        }
    }

    private static void onPlayerRespawn(ServerPlayer player) {
        int lives = PlayerLives.getLives(player);

        syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        sendLivesSync(player, lives);
    }
}

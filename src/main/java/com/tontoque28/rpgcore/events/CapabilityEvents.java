package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CapabilityEvents {

    public static void register() {
        AttachCapabilitiesEvent.Entities.BUS.addListener(CapabilityEvents::onAttachCapabilitiesPlayer);
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(CapabilityEvents::onPlayerLoggedIn);
        PlayerEvent.Clone.BUS.addListener(CapabilityEvents::onPlayerCloned);
    }

    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent.Entities event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerLivesProvider.PLAYER_LIVES).isPresent()) {
                event.addCapability(Identifier.fromNamespaceAndPath("rpgcore", "properties"), new PlayerLivesProvider());
            }
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joiningPlayer) {
            joiningPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                GhostDeathHandler.syncMaxHealthServer(joiningPlayer);
                GhostDeathHandler.sendLivesSync(joiningPlayer, cap.getLives());
            });

            for (ServerPlayer onlinePlayer : joiningPlayer.level().players()) {
                if (onlinePlayer == joiningPlayer) continue;
                onlinePlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap ->
                        ModMessages.sendToPlayer(new SyncLivesPacket(onlinePlayer.getId(), cap.getLives()), joiningPlayer)
                );
            }
        }
    }

    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone    = event.getEntity();

        original.reviveCaps();
        original.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(oldCap ->
                clone.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(newCap ->
                        newCap.copyFrom(oldCap)
                )
        );
        original.invalidateCaps();

        if (event.isWasDeath()
                && original.getPersistentData().getBooleanOr(GhostDeathHandler.TAG_HAS_SNAPSHOT, false)) {

            int snapshot = original.getPersistentData().getIntOr(GhostDeathHandler.TAG_LIVES_SNAPSHOT, 0);
            clone.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                    .ifPresent(cap -> cap.setLives(snapshot));

            clone.getPersistentData().remove(GhostDeathHandler.TAG_LIVES_SNAPSHOT);
            clone.getPersistentData().remove(GhostDeathHandler.TAG_HAS_SNAPSHOT);
        }
    }
}

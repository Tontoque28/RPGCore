package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.network.ModMessages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class CapabilityEvents {

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerLivesProvider.PLAYER_LIVES).isPresent()) {
                event.addCapability(new ResourceLocation("rpgcore", "properties"), new PlayerLivesProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joiningPlayer) {
            joiningPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                GhostDeathHandler.syncMaxHealthServer(joiningPlayer);
                GhostDeathHandler.sendLivesSync(joiningPlayer, cap.getLives());
            });

            for (ServerPlayer onlinePlayer : joiningPlayer.serverLevel().players()) {
                if (onlinePlayer == joiningPlayer) continue;
                onlinePlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap ->
                        ModMessages.sendToPlayer(new com.tontoque28.rpgcore.network.SyncLivesPacket(onlinePlayer.getId(), cap.getLives()), joiningPlayer)
                );
            }
        }
    }

    @SubscribeEvent
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
                && original.getPersistentData().getBoolean(GhostDeathHandler.TAG_HAS_SNAPSHOT)) {

            int snapshot = original.getPersistentData().getInt(GhostDeathHandler.TAG_LIVES_SNAPSHOT);
            clone.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                    .ifPresent(cap -> cap.setLives(snapshot));

            clone.getPersistentData().remove(GhostDeathHandler.TAG_LIVES_SNAPSHOT);
            clone.getPersistentData().remove(GhostDeathHandler.TAG_HAS_SNAPSHOT);
        }
    }
}
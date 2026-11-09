package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class CapabilityEvents {

    private static void invokeCaps(PlayerEntity player, String name) {
        try {
            Method method = CapabilityProvider.class.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(player);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            if (!event.getObject().getCapability(PlayerLivesProvider.PLAYER_LIVES).isPresent()) {
                event.addCapability(new ResourceLocation("rpgcore", "properties"), new PlayerLivesProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity joiningPlayer = (ServerPlayerEntity) event.getPlayer();
            joiningPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                GhostDeathHandler.syncMaxHealthServer(joiningPlayer);
                GhostDeathHandler.sendLivesSync(joiningPlayer, cap.getLives());
            });

            for (ServerPlayerEntity onlinePlayer : joiningPlayer.getLevel().players()) {
                if (onlinePlayer == joiningPlayer) continue;
                onlinePlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap ->
                        ModMessages.sendToPlayer(new SyncLivesPacket(onlinePlayer.getId(), cap.getLives()), joiningPlayer)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        PlayerEntity original = event.getOriginal();
        PlayerEntity clone = event.getPlayer();

        invokeCaps(original, "reviveCaps");
        original.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(oldCap ->
                clone.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(newCap ->
                        newCap.copyFrom(oldCap)
                )
        );
        invokeCaps(original, "invalidateCaps");

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

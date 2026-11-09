package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation("rpgcore", "properties"), new PlayerLivesProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            final EntityPlayerMP joiningPlayer = (EntityPlayerMP) event.player;
            PlayerLives own = PlayerLivesProvider.of(joiningPlayer);
            if (own != null) {
                GhostDeathHandler.syncMaxHealthServer(joiningPlayer);
                GhostDeathHandler.sendLivesSync(joiningPlayer, own.getLives());
            }

            for (EntityPlayer onlinePlayer : new ArrayList<EntityPlayer>(joiningPlayer.world.playerEntities)) {
                if (onlinePlayer == joiningPlayer) {
                    continue;
                }
                PlayerLives cap = PlayerLivesProvider.of(onlinePlayer);
                if (cap != null) {
                    ModMessages.sendToPlayer(new SyncLivesPacket(onlinePlayer.getEntityId(), cap.getLives()), joiningPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        EntityPlayer original = event.getOriginal();
        EntityPlayer clone = event.getEntityPlayer();

        PlayerLives oldCap = PlayerLivesProvider.of(original);
        PlayerLives newCap = PlayerLivesProvider.of(clone);
        if (oldCap != null && newCap != null) {
            newCap.copyFrom(oldCap);
        }

        if (event.isWasDeath()
                && original.getEntityData().getBoolean(GhostDeathHandler.TAG_HAS_SNAPSHOT)) {

            int snapshot = original.getEntityData().getInteger(GhostDeathHandler.TAG_LIVES_SNAPSHOT);
            if (newCap != null) {
                newCap.setLives(snapshot);
            }

            clone.getEntityData().removeTag(GhostDeathHandler.TAG_LIVES_SNAPSHOT);
            clone.getEntityData().removeTag(GhostDeathHandler.TAG_HAS_SNAPSHOT);
        }
    }
}

package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.network.ModMessages;
import com.tontoque28.rpgcore.network.SyncLivesPacket;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class GhostDeathHandler {

    static final String TAG_LIVES_SNAPSHOT = "rpgcore_lives_snapshot";
    static final String TAG_HAS_SNAPSHOT = "rpgcore_has_snapshot";

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";

    public static void syncMaxHealthServer(EntityPlayer player) {
        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap == null) {
            return;
        }
        int lives = cap.getLives();
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier mod : new ArrayList<AttributeModifier>(attr.getModifiers())) {
                attr.removeModifier(mod);
            }
            double maxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
            attr.setBaseValue(maxHp);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    public static void sendLivesSync(EntityPlayer player, int lives) {
        if (!(player.world instanceof WorldServer)) {
            return;
        }
        for (EntityPlayer target : new ArrayList<EntityPlayer>(player.world.playerEntities)) {
            if (target instanceof EntityPlayerMP) {
                ModMessages.sendToPlayer(new SyncLivesPacket(player.getEntityId(), lives), (EntityPlayerMP) target);
            }
        }
    }

    private static void setGhostAnchor(EntityPlayer player) {
        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_X, player.posX);
        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_Y, player.posY);
        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_Z, player.posZ);
    }

    private static void notifyLives(EntityPlayer player, int lives) {
        player.sendMessage(new TextComponentString("[SYSTEM] " + player.getName() + " — vidas restantes: " + lives));
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer) || event.getEntity().world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntity();

        PlayerLives lives = PlayerLivesProvider.of(player);
        if (lives == null) {
            return;
        }

        if (lives.getLives() > 1) {
            lives.removeLives(1);
            int livesNow = lives.getLives();

            player.getEntityData().setInteger(TAG_LIVES_SNAPSHOT, livesNow);
            player.getEntityData().setBoolean(TAG_HAS_SNAPSHOT, true);

            syncMaxHealthServer(player);
            sendLivesSync(player, livesNow);
            notifyLives(player, livesNow);
            return;
        }

        event.setCanceled(true);
        lives.setLives(0);

        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(2.0D);
        }

        if (player instanceof EntityPlayerMP) {
            GhostRestrictionsHandler.applyGhostAbilities((EntityPlayerMP) player);
        }

        setGhostAnchor(player);
        player.setHealth(1.0F);

        LostSoulEntity soul = LostSoulEntity.spawn(player);
        if (soul != null) {
            System.out.println("TINTRPG: " + player.getName() + " dejó su alma en " + soul.getPosition());
        }
        System.out.println("TINTRPG: " + player.getName() + " ahora es FANTASMA.");

        sendLivesSync(player, 0);
        notifyLives(player, 0);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();

        if (oldPlayer.getEntityData().getBoolean(TAG_HAS_SNAPSHOT)) {
            int savedLives = oldPlayer.getEntityData().getInteger(TAG_LIVES_SNAPSHOT);
            PlayerLives cap = PlayerLivesProvider.of(newPlayer);
            if (cap != null) {
                cap.setLives(savedLives);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) {
            return;
        }

        int lives = PlayerLivesProvider.livesOf(player, 10);

        syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        sendLivesSync(player, lives);
    }
}

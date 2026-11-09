package com.tontoque28.rpgcore.commands;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.UUID;

final class CommandSupport {

    static final UUID SACRIFICIO_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    private CommandSupport() {
    }

    static void success(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString(text));
    }

    static void failure(ICommandSender sender, String text) {
        TextComponentString message = new TextComponentString(text);
        message.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(message);
    }

    static EntityPlayerMP getPlayer(ICommandSender sender) {
        Entity entity = sender.getCommandSenderEntity();
        if (entity instanceof EntityPlayerMP) {
            return (EntityPlayerMP) entity;
        }
        failure(sender, "[RPGDebug] Solo disponible para jugadores.");
        return null;
    }

    static void give(EntityPlayerMP player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    static int getLives(EntityPlayerMP player) {
        return PlayerLivesProvider.livesOf(player, 0);
    }

    static void setLivesAndSync(EntityPlayerMP player, int lives) {
        if (lives > 0) {
            GhostRestrictionsHandler.removeGhostAbilities(player);
        }

        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap != null) {
            cap.setLives(lives);
        }
        GhostDeathHandler.syncMaxHealthServer(player);
        GhostDeathHandler.sendLivesSync(player, lives);

        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier mod : new ArrayList<AttributeModifier>(attr.getModifiers())) {
                attr.removeModifier(mod);
            }
            double targetMaxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
            attr.setBaseValue(targetMaxHp);
        }

        if (lives > 0) {
            player.setHealth(player.getMaxHealth());
        }
    }

    static void forceGhost(EntityPlayerMP player) {
        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap != null) {
            cap.setLives(0);
        }
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(SACRIFICIO_UUID);
            attr.setBaseValue(2.0D);
        }
        player.setHealth(1.0F);
        GhostRestrictionsHandler.applyGhostAbilities(player);
        GhostDeathHandler.sendLivesSync(player, 0);

        LostSoulEntity.spawn(player);
    }
}

package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class SpiritualVesselInteraction {

    public static void register() {
        PlayerInteractEvent.EntityInteractSpecific.BUS.addListener(SpiritualVesselInteraction::onEntityInteract);
    }

    public static boolean onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (!(stack.getItem() instanceof SpiritualVesselItem)) {
            return false;
        }

        if (!(target instanceof LostSoulEntity soul)) {
            return false;
        }

        if (player.level().isClientSide()) {
            return true;
        }

        if (SpiritualVesselItem.isBound(stack) || soul.getPlayerUuid() == null) {
            return true;
        }

        SpiritualVesselItem.bindSoul(stack, soul.getPlayerUuid(), soul.getPlayerName());
        soul.discard();

        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.rpgcore.soul_attracted", soul.getPlayerName()));

        event.setCancellationResult(InteractionResult.CONSUME);
        return true;
    }
}

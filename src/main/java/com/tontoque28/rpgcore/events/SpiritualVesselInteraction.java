package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SpiritualVesselInteraction {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                onEntityInteract(player, player.getItemInHand(hand), entity));
    }

    private static InteractionResult onEntityInteract(Player player, ItemStack stack, Entity target) {
        if (!(stack.getItem() instanceof SpiritualVesselItem)) {
            return InteractionResult.PASS;
        }

        if (!(target instanceof LostSoulEntity soul)) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        if (SpiritualVesselItem.isBound(stack) || soul.getPlayerUuid() == null) {
            return InteractionResult.FAIL;
        }

        SpiritualVesselItem.bindSoul(stack, soul.getPlayerUuid(), soul.getPlayerName());
        soul.discard();

        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.rpgcore.soul_attracted", soul.getPlayerName()));

        return InteractionResult.CONSUME;
    }
}

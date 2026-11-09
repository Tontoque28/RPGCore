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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class SpiritualVesselInteraction {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (!(stack.getItem() instanceof SpiritualVesselItem)) {
            return;
        }

        if (!(target instanceof LostSoulEntity soul)) {
            return;
        }

        if (player.level().isClientSide) {
            event.setCanceled(true);
            return;
        }

        if (SpiritualVesselItem.isBound(stack) || soul.getPlayerUuid() == null) {
            event.setCanceled(true);
            return;
        }

        SpiritualVesselItem.bindSoul(stack, soul.getPlayerUuid(), soul.getPlayerName());
        soul.discard();

        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.rpgcore.soul_attracted", soul.getPlayerName()));

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}
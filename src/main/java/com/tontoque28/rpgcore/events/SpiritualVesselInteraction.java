package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class SpiritualVesselInteraction {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        PlayerEntity player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (!(stack.getItem() instanceof SpiritualVesselItem)) {
            return;
        }

        if (!(target instanceof LostSoulEntity)) {
            return;
        }
        LostSoulEntity soul = (LostSoulEntity) target;

        if (player.level.isClientSide) {
            event.setCanceled(true);
            return;
        }

        if (SpiritualVesselItem.isBound(stack) || soul.getPlayerUuid() == null) {
            event.setCanceled(true);
            return;
        }

        SpiritualVesselItem.bindSoul(stack, soul.getPlayerUuid(), soul.getPlayerName());
        soul.remove();

        player.level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.sendMessage(new TranslationTextComponent("message.rpgcore.soul_attracted", soul.getPlayerName()), Util.NIL_UUID);

        event.setCancellationResult(ActionResultType.CONSUME);
        event.setCanceled(true);
    }
}

package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class SpiritualVesselInteraction {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (!(stack.getItem() instanceof SpiritualVesselItem)) {
            return;
        }

        if (!(target instanceof LostSoulEntity)) {
            return;
        }
        LostSoulEntity soul = (LostSoulEntity) target;

        if (player.world.isRemote) {
            event.setCanceled(true);
            return;
        }

        if (SpiritualVesselItem.isBound(stack) || soul.getPlayerUuid() == null) {
            event.setCanceled(true);
            return;
        }

        SpiritualVesselItem.bindSoul(stack, soul.getPlayerUuid(), soul.getPlayerName());
        soul.setDead();

        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.sendMessage(new TextComponentTranslation("message.rpgcore.soul_attracted", soul.getPlayerName()));

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }
}

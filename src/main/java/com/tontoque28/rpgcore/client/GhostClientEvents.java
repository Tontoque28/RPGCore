package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Side.CLIENT)
public class GhostClientEvents {

    private static boolean isGhost(EntityPlayer player) {
        return GhostRestrictionsHandler.isGhost(player);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }

        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH) {
            if (isGhost(player)) {
                event.setCanceled(true);
            }
        }

        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
            if (isGhost(player)) {
                event.setCanceled(true);
            }
        }
    }
}

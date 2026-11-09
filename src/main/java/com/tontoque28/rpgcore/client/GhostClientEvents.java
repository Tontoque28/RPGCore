package com.tontoque28.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Dist.CLIENT)
public class GhostClientEvents {

    private static boolean isGhost(PlayerEntity player) {
        return com.tontoque28.rpgcore.events.GhostRestrictionsHandler.isGhost(player);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGameOverlayEvent.Pre event) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

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

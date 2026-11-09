package com.tontoque28.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Dist.CLIENT)
public class GhostClientEvents {

    private static boolean isGhost(Player player) {
        return com.tontoque28.rpgcore.events.GhostRestrictionsHandler.isGhost(player);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            if (isGhost(player)) {
                event.setCanceled(true);
            }
        }
        
        if (event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            if (isGhost(player)) {
                event.setCanceled(true);
            }
        }
    }
}
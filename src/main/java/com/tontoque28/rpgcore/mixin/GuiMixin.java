package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void rpgcore$hideGhostHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        Player local = Minecraft.getInstance().player;
        if (local != null && GhostRestrictionsHandler.isGhost(local)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void rpgcore$hideGhostFood(GuiGraphics guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        Player local = Minecraft.getInstance().player;
        if (local != null && GhostRestrictionsHandler.isGhost(local)) {
            ci.cancel();
        }
    }
}

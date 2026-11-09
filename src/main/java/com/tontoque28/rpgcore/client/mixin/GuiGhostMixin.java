package com.tontoque28.rpgcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiGhostMixin {

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void rpgcore$hideGhostVitals(PoseStack poseStack, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            ci.cancel();
        }
    }
}

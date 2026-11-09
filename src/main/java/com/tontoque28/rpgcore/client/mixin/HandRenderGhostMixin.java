package com.tontoque28.rpgcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class HandRenderGhostMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void rpgcore$hideGhostHands(float partialTick, PoseStack poseStack, MultiBufferSource.BufferSource buffer, LocalPlayer player, int light, CallbackInfo ci) {
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            ci.cancel();
        }
    }
}

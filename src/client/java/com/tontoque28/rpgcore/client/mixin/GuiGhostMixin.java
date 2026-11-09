package com.tontoque28.rpgcore.client.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class GuiGhostMixin {

    @Inject(method = "extractPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void rpgcore$hideGhostVitals(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            ci.cancel();
        }
    }
}

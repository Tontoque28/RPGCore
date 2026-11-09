package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SpectatorVisibilityMixin {

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true, remap = false)
    private void rpgcore$makeGhostVisible(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (player.isSpectator() && GhostRestrictionsHandler.isGhost(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}

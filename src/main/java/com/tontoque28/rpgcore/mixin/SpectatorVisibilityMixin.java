package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SpectatorVisibilityMixin {

    @Inject(method = {"isInvisible", "func_82150_aj"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void rpgcore$makeGhostVisible(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) self;
            if (player.isSpectator() && GhostRestrictionsHandler.isGhost(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}

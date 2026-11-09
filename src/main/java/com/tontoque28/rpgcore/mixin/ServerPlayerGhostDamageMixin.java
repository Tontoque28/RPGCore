package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerGhostDamageMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void rpgcore$ghostImmune(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (GhostRestrictionsHandler.isGhost((ServerPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}

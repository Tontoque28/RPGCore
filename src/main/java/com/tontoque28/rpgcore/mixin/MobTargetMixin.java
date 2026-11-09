package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobTargetMixin {

    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void rpgcore$ignoreGhosts(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player player && GhostRestrictionsHandler.isGhost(player)) {
            ci.cancel();
        }
    }
}

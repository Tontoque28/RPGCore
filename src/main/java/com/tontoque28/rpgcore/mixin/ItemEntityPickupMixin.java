package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void rpgcore$ghostCannotPickUp(Player player, CallbackInfo ci) {
        if (GhostRestrictionsHandler.isGhost(player)) {
            ci.cancel();
        }
    }
}

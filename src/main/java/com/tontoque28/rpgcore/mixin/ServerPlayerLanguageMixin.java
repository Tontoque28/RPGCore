package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.capability.LanguageHolder;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerLanguageMixin implements LanguageHolder {

    @Unique
    private String rpgcore$language = "en_us";

    @Override
    public String rpgcore$getLanguage() {
        return this.rpgcore$language;
    }

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void rpgcore$captureLanguage(ClientInformation information, CallbackInfo ci) {
        this.rpgcore$language = information.language();
    }
}

package com.tontoque28.rpgcore.mixin;

import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundClientInformationPacket.class)
public interface ClientInformationPacketAccessor {

    @Accessor("language")
    String rpgcore$getLanguage();
}

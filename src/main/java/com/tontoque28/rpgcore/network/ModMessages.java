package com.tontoque28.rpgcore.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModMessages {

    public static final ResourceLocation SYNC_LIVES = new ResourceLocation("rpgcore", "sync_lives");

    private ModMessages() {
    }

    public static void sendSyncLives(ServerPlayer target, int entityId, int lives) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        buf.writeInt(lives);
        ServerPlayNetworking.send(target, SYNC_LIVES, buf);
    }
}

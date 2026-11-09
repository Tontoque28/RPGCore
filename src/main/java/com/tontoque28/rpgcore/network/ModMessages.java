package com.tontoque28.rpgcore.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModMessages {

    public static final ResourceLocation SYNC_LIVES = ResourceLocation.fromNamespaceAndPath("rpgcore", "sync_lives");

    private ModMessages() {
    }

    public record SyncLivesPayload(int entityId, int lives) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<SyncLivesPayload> TYPE = new CustomPacketPayload.Type<>(SYNC_LIVES);

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncLivesPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, SyncLivesPayload::entityId,
                ByteBufCodecs.INT, SyncLivesPayload::lives,
                SyncLivesPayload::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncLivesPayload.TYPE, SyncLivesPayload.CODEC);
    }

    public static void sendSyncLives(ServerPlayer target, int entityId, int lives) {
        ServerPlayNetworking.send(target, new SyncLivesPayload(entityId, lives));
    }
}

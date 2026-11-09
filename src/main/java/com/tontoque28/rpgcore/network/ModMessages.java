package com.tontoque28.rpgcore.network;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModMessages {
    private static final SimpleChannel INSTANCE = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath("rpgcore", "main"))
            .networkProtocolVersion(1)
            .optional()
            .simpleChannel()
                .play()
                    .clientbound()
                        .addMain(SyncLivesPacket.class, SyncLivesPacket.STREAM_CODEC, SyncLivesPacket::handle)
            .build();

    public static void register() {
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToAllTracking(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
    }
}

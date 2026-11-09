package com.tontoque28.rpgcore.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModMessages {
    private static SimpleNetworkWrapper INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("rpgcore");
        INSTANCE.registerMessage(SyncLivesPacket.Handler.class, SyncLivesPacket.class, id(), Side.CLIENT);
    }

    public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
        if (INSTANCE != null) {
            INSTANCE.sendTo(message, player);
        }
    }
}

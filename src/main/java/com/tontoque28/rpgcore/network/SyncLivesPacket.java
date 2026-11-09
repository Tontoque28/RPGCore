package com.tontoque28.rpgcore.network;

import com.tontoque28.rpgcore.client.ClientLivesHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncLivesPacket implements IMessage {
    private int entityId;
    private int lives;

    public SyncLivesPacket() {
    }

    public SyncLivesPacket(int entityId, int lives) {
        this.entityId = entityId;
        this.lives = lives;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.lives = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(lives);
    }

    public static class Handler implements IMessageHandler<SyncLivesPacket, IMessage> {
        @Override
        public IMessage onMessage(SyncLivesPacket message, MessageContext ctx) {
            ClientLivesHandler.schedule(message.entityId, message.lives);
            return null;
        }
    }
}

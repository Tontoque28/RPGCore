package com.tontoque28.rpgcore.network;

import com.tontoque28.rpgcore.client.ClientLivesHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLivesPacket {
    private final int entityId;
    private final int lives;

    public SyncLivesPacket(int entityId, int lives) {
        this.entityId = entityId;
        this.lives = lives;
    }

    public SyncLivesPacket(PacketBuffer buf) {
        this.entityId = buf.readInt();
        this.lives = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeInt(lives);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientLivesHandler.apply(this.entityId, this.lives)));
        context.setPacketHandled(true);
        return true;
    }
}

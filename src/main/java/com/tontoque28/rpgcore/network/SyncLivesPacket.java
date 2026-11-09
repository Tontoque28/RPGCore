package com.tontoque28.rpgcore.network;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncLivesPacket {
    private final int entityId;
    private final int lives;

    public SyncLivesPacket(int entityId, int lives) {
        this.entityId = entityId;
        this.lives = lives;
    }

    public SyncLivesPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.lives = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(lives);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            assert Minecraft.getInstance().level != null;
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);

            if (entity instanceof Player player) {
                player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                    cap.setLives(this.lives);
                });
                boolean ghost = this.lives <= 0;
                player.getPersistentData().putBoolean("rpgcore_is_ghost", ghost);
                player.setInvisible(ghost);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
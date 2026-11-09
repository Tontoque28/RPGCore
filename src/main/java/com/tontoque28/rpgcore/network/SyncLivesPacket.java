package com.tontoque28.rpgcore.network;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SyncLivesPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncLivesPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.toBytes(buf),
            buf -> new SyncLivesPacket(buf.readInt(), buf.readInt())
    );

    private final int entityId;
    private final int lives;

    public SyncLivesPacket(int entityId, int lives) {
        this.entityId = entityId;
        this.lives = lives;
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(lives);
    }

    public static void handle(SyncLivesPacket packet, CustomPayloadEvent.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.entityId);

        if (entity instanceof Player player) {
            player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                cap.setLives(packet.lives);
            });
            boolean ghost = packet.lives <= 0;
            player.getPersistentData().putBoolean("rpgcore_is_ghost", ghost);
            player.setInvisible(ghost);
        }
    }
}

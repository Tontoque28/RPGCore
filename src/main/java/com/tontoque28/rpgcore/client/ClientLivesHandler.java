package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ClientLivesHandler {

    public static void apply(int entityId, int lives) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                cap.setLives(lives);
            });
            boolean ghost = lives <= 0;
            player.getPersistentData().putBoolean("rpgcore_is_ghost", ghost);
            player.setInvisible(ghost);
        }
    }
}

package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientLivesHandler {

    public static void schedule(final int entityId, final int lives) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                apply(entityId, lives);
            }
        });
    }

    public static void apply(int entityId, int lives) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) {
            return;
        }
        Entity entity = minecraft.world.getEntityByID(entityId);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            PlayerLives cap = PlayerLivesProvider.of(player);
            if (cap != null) {
                cap.setLives(lives);
            }
            boolean ghost = lives <= 0;
            player.getEntityData().setBoolean("rpgcore_is_ghost", ghost);
            player.setInvisible(ghost);
        }
    }
}

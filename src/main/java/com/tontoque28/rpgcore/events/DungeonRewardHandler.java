package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.item.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class DungeonRewardHandler {

    public static void otorgarRecompensaMazmorra(EntityPlayer player, int corazonesExtra) {
        if (player.world.isRemote || corazonesExtra <= 0) {
            return;
        }

        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap == null) {
            return;
        }
        cap.addLives(corazonesExtra);
        int totalVidas = cap.getLives();

        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
        GhostDeathHandler.sendLivesSync(player, totalVidas);

        TextComponentString message = new TextComponentString("[SYSTEM] ¡Has obtenido +" + corazonesExtra + " corazones de vitalidad! (Total: " + totalVidas + ")");
        message.getStyle().setColor(TextFormatting.GOLD);
        player.sendMessage(message);
    }

    @SubscribeEvent
    public static void onBossDefeatedForHearts(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (!(event.getSource().getTrueSource() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();

        ResourceLocation entityId = EntityList.getKey(event.getEntity());
        if (entityId == null) {
            return;
        }

        BossReward reward = findReward(entityId.toString());
        if (reward != null) {
            event.getEntity().entityDropItem(new ItemStack(ModItems.LIFE_HEART, reward.hearts), 0.5F);
            TextComponentString message = new TextComponentString("[RPGCore] The boss dropped " + reward.hearts
                    + " Life Heart(s) [Tier " + reward.tier + "].");
            message.getStyle().setColor(TextFormatting.GOLD);
            player.sendMessage(message);
        }
    }

    private static BossReward findReward(String entityId) {
        List<String> configuredRewards = RpgCoreConfig.getBossRewards();
        for (String entry : configuredRewards) {
            String[] parts = entry.split("\\|", -1);
            if (parts.length != 3 || !parts[0].equals(entityId)) {
                continue;
            }
            try {
                int hearts = Integer.parseInt(parts[2]);
                if (hearts > 0) {
                    return new BossReward(parts[1], hearts);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static final class BossReward {
        final String tier;
        final int hearts;

        BossReward(String tier, int hearts) {
            this.tier = tier;
            this.hearts = hearts;
        }
    }
}

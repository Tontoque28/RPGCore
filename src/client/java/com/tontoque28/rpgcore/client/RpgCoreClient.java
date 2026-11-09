package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PersistentDataHolder;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.network.ModMessages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class RpgCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RpgCore.LOST_SOUL, LostSoulRenderer::new);
        ModelLayerRegistry.registerModelLayer(LostSoulModel.LAYER_LOCATION, LostSoulModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(LostSoulAuraModel.LAYER_LOCATION, LostSoulAuraModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(GhostHeadModel.LAYER_LOCATION, GhostHeadModel::createBodyLayer);

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.SyncLivesPayload.TYPE, (payload, context) ->
                context.client().execute(() -> applySync(context.client(), payload.entityId(), payload.lives())));
    }

    private static void applySync(Minecraft client, int entityId, int lives) {
        if (client.level == null) {
            return;
        }
        Entity entity = client.level.getEntity(entityId);
        if (entity instanceof Player player) {
            PlayerLives.setLives(player, lives);
            boolean ghost = lives <= 0;
            PersistentDataHolder.of(player).putBoolean(GhostRestrictionsHandler.TAG_GHOST_STATE, ghost);
            player.setInvisible(ghost);
        }
    }
}

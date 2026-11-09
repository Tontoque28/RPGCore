package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.RpgCore;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientModEvents {

    public static void register() {
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientModEvents::onRegisterRenderers);
        EntityRenderersEvent.RegisterLayerDefinitions.BUS.addListener(ClientModEvents::onRegisterLayerDefinitions);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RpgCore.LOST_SOUL.get(), LostSoulRenderer::new);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LostSoulModel.LAYER_LOCATION, LostSoulModel::createBodyLayer);
        event.registerLayerDefinition(LostSoulAuraModel.LAYER_LOCATION, LostSoulAuraModel::createBodyLayer);
    }
}

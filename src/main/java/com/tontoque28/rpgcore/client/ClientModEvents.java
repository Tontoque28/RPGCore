package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Side.CLIENT)
public class ClientModEvents {

    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        registerModel(ModItems.SPIRITUAL_VESSEL);
        registerModel(ModItems.LIFE_HEART);
        registerModel(ModItems.GUIDE_BOOK);

        RenderingRegistry.registerEntityRenderingHandler(LostSoulEntity.class, new IRenderFactory<LostSoulEntity>() {
            @Override
            public Render<? super LostSoulEntity> createRenderFor(RenderManager manager) {
                return new LostSoulRenderer(manager);
            }
        });
    }
}

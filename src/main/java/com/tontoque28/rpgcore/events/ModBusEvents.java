package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(RpgCore.LOST_SOUL.get(), LostSoulEntity.createAttributes().build());
    }
}

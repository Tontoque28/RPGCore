package com.tontoque28.rpgcore.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.ENTITIES, "rpgcore");

    public static final RegistryObject<EntityType<LostSoulEntity>> LOST_SOUL = ENTITY_TYPES.register("lost_soul",
            () -> EntityType.Builder.of(LostSoulEntity::new, EntityClassification.CREATURE)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    .build("rpgcore:lost_soul"));
}

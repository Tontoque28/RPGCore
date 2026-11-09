package com.tontoque28.rpgcore.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(ForgeRegistries.ENTITY_TYPES, "rpgcore");

        public static final RegistryObject<EntityType<LostSoulEntity>> LOST_SOUL = ENTITY_TYPES.register("lost_soul",
                        () -> EntityType.Builder.of(LostSoulEntity::new, MobCategory.CREATURE)
                                        .sized(0.5f, 0.5f)
                                        .clientTrackingRange(8)
                                        .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("rpgcore", "lost_soul"))));
}
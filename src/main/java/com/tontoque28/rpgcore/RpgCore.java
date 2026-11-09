package com.tontoque28.rpgcore;

import com.mojang.logging.LogUtils;
import com.tontoque28.rpgcore.client.ClientModEvents;
import com.tontoque28.rpgcore.client.GhostRenderHandler;
import com.tontoque28.rpgcore.commands.DebugCommands;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.events.CapabilityEvents;
import com.tontoque28.rpgcore.events.DungeonRewardHandler;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.events.RitualSystem;
import com.tontoque28.rpgcore.events.SpiritualVesselInteraction;
import com.tontoque28.rpgcore.item.GuideBookItem;
import com.tontoque28.rpgcore.item.LifeHeartItem;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import com.tontoque28.rpgcore.network.ModMessages;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(RpgCore.MOD_ID)
public class RpgCore {

    public static final String MOD_ID = "rpgcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static final RegistryObject<EntityType<LostSoulEntity>> LOST_SOUL = ENTITY_TYPES.register(
            "lost_soul",
            () -> EntityType.Builder.<LostSoulEntity>of(LostSoulEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "lost_soul")))
    );

    public static final RegistryObject<SpiritualVesselItem> SPIRITUAL_VESSEL = ITEMS.register(
            "spiritual_vessel",
            () -> new SpiritualVesselItem(new Item.Properties().setId(itemKey("spiritual_vessel")).stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final RegistryObject<LifeHeartItem> LIFE_HEART = ITEMS.register(
            "life_heart",
            () -> new LifeHeartItem(new Item.Properties().setId(itemKey("life_heart")).stacksTo(16).rarity(Rarity.RARE))
    );

    public static final RegistryObject<GuideBookItem> GUIDE_BOOK = ITEMS.register(
            "guide_book",
            () -> new GuideBookItem(new Item.Properties().setId(itemKey("guide_book")).stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final RegistryObject<CreativeModeTab> RPGCORE_TAB = CREATIVE_MODE_TABS.register(
            "rpgcore_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.rpgcore"))
                    .icon(() -> new ItemStack(GUIDE_BOOK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SPIRITUAL_VESSEL.get());
                        output.accept(LIFE_HEART.get());
                        output.accept(GUIDE_BOOK.get());
                    })
                    .build()
    );

    public RpgCore(FMLJavaModLoadingContext context) {
        BusGroup modBus = context.getModBusGroup();
        context.registerConfig(ModConfig.Type.SERVER, RpgCoreConfig.SERVER_SPEC);
        ModMessages.register();

        ENTITY_TYPES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);

        EntityAttributeCreationEvent.BUS.addListener(RpgCore::registerAttributes);

        CapabilityEvents.register();
        GhostDeathHandler.register();
        GhostRestrictionsHandler.register();
        RitualSystem.register();
        SpiritualVesselInteraction.register();
        DungeonRewardHandler.register();
        DebugCommands.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientModEvents.register();
            GhostRenderHandler.register();
        }

        LOGGER.info("TINTRPG: Canal de Red Registrado Exitosamente.");
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LOST_SOUL.get(), LostSoulEntity.createAttributes().build());
    }
}

package com.tontoque28.rpgcore;

import com.mojang.logging.LogUtils;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.GuideBookItem;
import com.tontoque28.rpgcore.item.LifeHeartItem;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import com.tontoque28.rpgcore.network.ModMessages;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(RpgCore.MOD_ID)
public class RpgCore {

    public static final String MOD_ID = "rpgcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static final RegistryObject<EntityType<LostSoulEntity>> LOST_SOUL = ENTITY_TYPES.register(
            "lost_soul",
            () -> EntityType.Builder.<LostSoulEntity>of(LostSoulEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "lost_soul")))
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
            () -> CreativeModeTab.builder()
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
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.SERVER, RpgCoreConfig.SERVER_SPEC);
        ModMessages.register();

        ENTITY_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("TINTRPG: Canal de Red Registrado Exitosamente.");
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerLives.class);
        LOGGER.info("TINTRPG: Capability de Vidas Registrada.");
    }
}
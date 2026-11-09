package com.tontoque28.rpgcore;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.GuideBookItem;
import com.tontoque28.rpgcore.item.LifeHeartItem;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import com.tontoque28.rpgcore.network.ModMessages;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RpgCore.MOD_ID)
public class RpgCore {

    public static final String MOD_ID = "rpgcore";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final ItemGroup RPGCORE_TAB = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RpgCore.GUIDE_BOOK.get());
        }
    };

    public static final RegistryObject<EntityType<LostSoulEntity>> LOST_SOUL = ENTITY_TYPES.register(
            "lost_soul",
            () -> EntityType.Builder.<LostSoulEntity>of(LostSoulEntity::new, EntityClassification.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("rpgcore:lost_soul")
    );

    public static final RegistryObject<SpiritualVesselItem> SPIRITUAL_VESSEL = ITEMS.register(
            "spiritual_vessel",
            () -> new SpiritualVesselItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(RPGCORE_TAB))
    );

    public static final RegistryObject<LifeHeartItem> LIFE_HEART = ITEMS.register(
            "life_heart",
            () -> new LifeHeartItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE).tab(RPGCORE_TAB))
    );

    public static final RegistryObject<GuideBookItem> GUIDE_BOOK = ITEMS.register(
            "guide_book",
            () -> new GuideBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(RPGCORE_TAB))
    );

    public RpgCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RpgCoreConfig.SERVER_SPEC);

        ENTITY_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
            LOGGER.info("TINTRPG: Canal de Red Registrado Exitosamente.");
            registerCapabilities();
        });
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(PlayerLives.class, new Capability.IStorage<PlayerLives>() {
            @Override
            public INBT writeNBT(Capability<PlayerLives> capability, PlayerLives instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                instance.saveNBTData(nbt);
                return nbt;
            }

            @Override
            public void readNBT(Capability<PlayerLives> capability, PlayerLives instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) {
                    instance.loadNBTData((CompoundNBT) nbt);
                }
            }
        }, PlayerLives::new);
        LOGGER.info("TINTRPG: Capability de Vidas Registrada.");
    }
}

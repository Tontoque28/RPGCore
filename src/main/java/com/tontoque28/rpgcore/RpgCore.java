package com.tontoque28.rpgcore;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.commands.RpgCoreCommand;
import com.tontoque28.rpgcore.commands.RpgDebugCommand;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.item.ModItems;
import com.tontoque28.rpgcore.network.ModMessages;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = RpgCore.MOD_ID, name = "RPG Core", version = "1.0.2", acceptedMinecraftVersions = "[1.12.2]")
@Mod.EventBusSubscriber(modid = RpgCore.MOD_ID)
public class RpgCore {

    public static final String MOD_ID = "rpgcore";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.Instance(MOD_ID)
    public static RpgCore instance;

    public static final CreativeTabs RPGCORE_TAB = new CreativeTabs(MOD_ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModItems.GUIDE_BOOK);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RpgCoreConfig.load(new File(event.getModConfigurationDirectory(), "rpgcore-server.cfg"));

        ModItems.SPIRITUAL_VESSEL.setCreativeTab(RPGCORE_TAB);
        ModItems.LIFE_HEART.setCreativeTab(RPGCORE_TAB);
        ModItems.GUIDE_BOOK.setCreativeTab(RPGCORE_TAB);

        EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "lost_soul"), LostSoulEntity.class, "lost_soul", 0, this, 160, 20, false);

        ModMessages.register();
        LOGGER.info("TINTRPG: Canal de Red Registrado Exitosamente.");

        CapabilityManager.INSTANCE.register(PlayerLives.class, new PlayerLivesProvider.Storage(), new PlayerLivesProvider.Factory());
        LOGGER.info("TINTRPG: Capability de Vidas Registrada.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new RpgDebugCommand());
        event.registerServerCommand(new RpgCoreCommand());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.SPIRITUAL_VESSEL, ModItems.LIFE_HEART, ModItems.GUIDE_BOOK);
    }
}

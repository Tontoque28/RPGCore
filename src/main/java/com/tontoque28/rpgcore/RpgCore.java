package com.tontoque28.rpgcore;

import com.mojang.logging.LogUtils;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.commands.DebugCommands;
import com.tontoque28.rpgcore.config.RpgCoreConfig;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.events.DungeonRewardHandler;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.events.RitualSystem;
import com.tontoque28.rpgcore.events.SpiritualVesselInteraction;
import com.tontoque28.rpgcore.item.GuideBookItem;
import com.tontoque28.rpgcore.item.LifeHeartItem;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import com.tontoque28.rpgcore.network.ModMessages;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;

public class RpgCore implements ModInitializer {

    public static final String MOD_ID = "rpgcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final EntityType<LostSoulEntity> LOST_SOUL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id("lost_soul"),
            FabricEntityTypeBuilder.<LostSoulEntity>create(MobCategory.MISC, LostSoulEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .fireImmune()
                    .trackRangeChunks(12)
                    .trackedUpdateRate(Integer.MAX_VALUE)
                    .build()
    );

    public static final SpiritualVesselItem SPIRITUAL_VESSEL = Registry.register(
            BuiltInRegistries.ITEM,
            id("spiritual_vessel"),
            new SpiritualVesselItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final LifeHeartItem LIFE_HEART = Registry.register(
            BuiltInRegistries.ITEM,
            id("life_heart"),
            new LifeHeartItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE))
    );

    public static final GuideBookItem GUIDE_BOOK = Registry.register(
            BuiltInRegistries.ITEM,
            id("guide_book"),
            new GuideBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final CreativeModeTab RPGCORE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            id("rpgcore_tab"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.rpgcore"))
                    .icon(() -> new ItemStack(GUIDE_BOOK))
                    .displayItems((parameters, output) -> {
                        output.accept(SPIRITUAL_VESSEL);
                        output.accept(LIFE_HEART);
                        output.accept(GUIDE_BOOK);
                    })
                    .build()
    );

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RpgCoreConfig.load();

        FabricDefaultAttributeRegistry.register(LOST_SOUL, LostSoulEntity.createAttributes());

        GhostDeathHandler.register();
        GhostRestrictionsHandler.register();
        DungeonRewardHandler.register();
        RitualSystem.register();
        SpiritualVesselInteraction.register();
        DebugCommands.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLoggedIn(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RitualSystem.clear());

        LOGGER.info("TINTRPG: Canal de Red Registrado Exitosamente.");
        LOGGER.info("TINTRPG: Datos de Vidas Registrados.");
    }

    private static void onPlayerLoggedIn(ServerPlayer joiningPlayer) {
        GhostDeathHandler.syncMaxHealthServer(joiningPlayer);
        GhostDeathHandler.sendLivesSync(joiningPlayer, PlayerLives.getLives(joiningPlayer));

        for (ServerPlayer onlinePlayer : joiningPlayer.serverLevel().players()) {
            if (onlinePlayer == joiningPlayer) continue;
            ModMessages.sendSyncLives(joiningPlayer, onlinePlayer.getId(), PlayerLives.getLives(onlinePlayer));
        }
    }
}

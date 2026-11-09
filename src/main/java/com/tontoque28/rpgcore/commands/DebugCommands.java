package com.tontoque28.rpgcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class DebugCommands {

    private static final UUID SACRIFICIO_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
        registerPublic(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("rpgdebug")
                        .requires(src -> src.hasPermission(2))

                        .then(Commands.literal("lives")
                                .executes(ctx -> cmdLives(ctx.getSource())))

                        .then(Commands.literal("heart")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> cmdHeart(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "amount")))))

                        .then(Commands.literal("debuff")
                            .executes(ctx -> cmdDebuff(ctx.getSource())))

                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> cmdSet(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))

                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> cmdAdd(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))

                        .then(Commands.literal("remove")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> cmdRemove(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))

                        .then(Commands.literal("kill")
                                .executes(ctx -> cmdKill(ctx.getSource())))

                        .then(Commands.literal("ghost")
                                .executes(ctx -> cmdGhost(ctx.getSource())))

                        .then(Commands.literal("revive")
                                .executes(ctx -> cmdRevive(ctx.getSource())))

                        .then(Commands.literal("soul")
                                .executes(ctx -> cmdSoul(ctx.getSource())))

                        .then(Commands.literal("reset")
                                .executes(ctx -> cmdReset(ctx.getSource())))

                        .then(Commands.literal("clear_debuff")
                            .executes(ctx -> cmdClearDebuff(ctx.getSource())))

                        .then(Commands.literal("vessel")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrException();
                                    ItemStack vessel = new ItemStack(RpgCore.SPIRITUAL_VESSEL.get());

                                    if (!player.inventory.add(vessel)) {
                                        player.drop(vessel, false);
                                    }

                                    context.getSource().sendSuccess(new StringTextComponent("[RPGDebug] Vasija Espiritual obtenida."), false);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("altar")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrException();
                                    ServerWorld level = player.getLevel();
                                    BlockPos center = player.blockPosition();

                                    AxisAlignedBB area = new AxisAlignedBB(center).inflate(10.0D);
                                    for (EnderCrystalEntity old : level.getEntitiesOfClass(EnderCrystalEntity.class, area)) {
                                        old.remove();
                                    }

                                    for (int dx = -4; dx <= 4; dx++) {
                                        for (int dz = -4; dz <= 4; dz++) {
                                            level.setBlockAndUpdate(center.offset(dx, -1, dz), Blocks.GRASS_BLOCK.defaultBlockState());
                                            for (int dy = 0; dy <= 4; dy++) {
                                                level.setBlockAndUpdate(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                                            }
                                        }
                                    }

                                    level.setBlockAndUpdate(center, Blocks.RESPAWN_ANCHOR.defaultBlockState());

                                    int[][] offsets = {{4, 4}, {4, -4}, {-4, 4}, {-4, -4}};
                                    for (int[] offset : offsets) {
                                        BlockPos base = center.offset(offset[0], 0, offset[1]);

                                        level.setBlockAndUpdate(base, Blocks.NETHERITE_BLOCK.defaultBlockState());
                                        level.setBlockAndUpdate(base.above(1), Blocks.CRYING_OBSIDIAN.defaultBlockState());
                                        level.setBlockAndUpdate(base.above(2), Blocks.OBSIDIAN.defaultBlockState());

                                        BlockPos crystalPos = base.above(3);
                                        EnderCrystalEntity crystal = new EnderCrystalEntity(
                                                level,
                                                crystalPos.getX() + 0.5D,
                                                crystalPos.getY(),
                                                crystalPos.getZ() + 0.5D
                                        );
                                        crystal.setShowBottom(false);
                                        level.addFreshEntity(crystal);
                                    }

                                    player.teleportTo(center.getX() + 0.5D, center.getY(), center.getZ() + 2.5D);

                                    context.getSource().sendSuccess(new StringTextComponent("[RPGDebug] Altar de resurrección generado."), false);
                                    return 1;
                                })
                        )
        );
    }

    private static void registerPublic(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("rpgcore")
            .then(Commands.literal("debuffs")
                .executes(ctx -> cmdPublicDebuffs(ctx.getSource())))
            .then(Commands.literal("book")
                .executes(ctx -> cmdBook(ctx.getSource())))
            .then(Commands.literal("revive_all")
                .executes(ctx -> cmdReviveAll(ctx.getSource())))
            .then(Commands.literal("lose_world")
                .executes(ctx -> cmdLoseWorld(ctx.getSource())))
        );
    }


    private static int cmdLives(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        int lives = getLives(player);
        boolean isGhost = lives <= 0;
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        src.sendSuccess(new StringTextComponent(
                "[RPGDebug] Vidas (Corazones): " + lives
                        + " | HP: " + hp + "/" + maxHp
                        + " | Fantasma: " + isGhost
                        + " | Invisible: " + player.isInvisible()
                        + " | Invulnerable: " + player.isInvulnerable()), false);
        return 1;
    }

    private static int cmdHeart(CommandSource src, int amount) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        ItemStack hearts = new ItemStack(RpgCore.LIFE_HEART.get(), amount);
        if (!player.inventory.add(hearts)) {
            player.drop(hearts, false);
        }
        src.sendSuccess(new StringTextComponent(
                "[RPGDebug] Given " + amount + " Life Heart(s)."), false);
        return 1;
    }

    private static int cmdDebuff(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        player.sendMessage(GhostRestrictionsHandler.getActiveWorldDebuffMessage(player), Util.NIL_UUID);
        return 1;
    }

    private static int cmdPublicDebuffs(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        for (net.minecraft.util.text.ITextComponent row : GhostRestrictionsHandler.getActiveWorldDebuffTable(player)) {
            player.sendMessage(row, Util.NIL_UUID);
        }
        return 1;
    }

    private static int cmdBook(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        ItemStack book = new ItemStack(RpgCore.GUIDE_BOOK.get());
        if (!player.inventory.add(book)) {
            player.drop(book, false);
        }

        src.sendSuccess(new StringTextComponent("[RPGCore] RPGCore Book entregado."), false);
        return 1;
    }

    private static int cmdReviveAll(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.reviveAll(player.getLevel());
        return 1;
    }

    private static int cmdLoseWorld(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.loseWorld(player.getLevel());
        return 1;
    }

    private static int cmdSet(CommandSource src, int amount) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        setLivesAndSync(player, amount);
        src.sendSuccess(new StringTextComponent("[RPGDebug] Vidas establecidas a " + amount + " (" + amount + " corazones)."), false);
        return 1;
    }

    private static int cmdAdd(CommandSource src, int amount) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> cap.addLives(amount));
        int now = getLives(player);
        setLivesAndSync(player, now);
        src.sendSuccess(new StringTextComponent("[RPGDebug] +" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)"), false);
        return 1;
    }

    private static int cmdRemove(CommandSource src, int amount) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> cap.removeLives(amount));
        int now = getLives(player);
        setLivesAndSync(player, now);
        src.sendSuccess(new StringTextComponent("[RPGDebug] -" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)"), false);
        return 1;
    }

    private static int cmdKill(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        int lives = getLives(player);
        if (lives <= 0) {
            src.sendSuccess(new StringTextComponent("[RPGDebug] Ya eres fantasma (0 vidas)."), false);
            return 0;
        }

        if (lives > 1) {
            setLivesAndSync(player, lives - 1);
            player.kill();
            src.sendSuccess(new StringTextComponent("[RPGDebug] Muerte forzada. Vidas restantes: " + (lives - 1)), false);
        } else {
            forceGhost(player);
            src.sendSuccess(new StringTextComponent("[RPGDebug] Última vida agotada → FANTASMA."), false);
        }
        return 1;
    }

    private static int cmdGhost(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        forceGhost(player);
        src.sendSuccess(new StringTextComponent("[RPGDebug] Estado FANTASMA activado."), false);
        return 1;
    }

    private static int cmdRevive(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.removeGhostAbilities(player);
        setLivesAndSync(player, 10);
        player.clearFire();

        src.sendSuccess(new StringTextComponent("[RPGDebug] Estado normal restaurado (10 corazones)."), false);
        return 1;
    }

    private static int cmdSoul(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        LostSoulEntity soul = LostSoulEntity.spawn(player);
        if (soul != null) {
            src.sendSuccess(new StringTextComponent(
                    "[RPGDebug] Alma spawneada en " + soul.blockPosition()), false);
        } else {
            src.sendFailure(new StringTextComponent("[RPGDebug] Error al spawnear el alma."));
        }
        return 1;
    }

    private static int cmdReset(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.removeGhostAbilities(player);
        setLivesAndSync(player, 10);
        player.clearFire();
        GhostRestrictionsHandler.clearWorldDebuff(player.getLevel());

        src.sendSuccess(new StringTextComponent("[RPGDebug] Sistema reseteado a 10 vidas (10 corazones completos)."), false);
        return 1;
    }

    private static int cmdClearDebuff(CommandSource src) {
        ServerPlayerEntity player = getPlayer(src);
        if (player == null) return 0;

        if (ModList.get().isLoaded("ftbquests") || ModList.get().isLoaded("heracles")) {
            src.sendFailure(new StringTextComponent(
                    "[RPGDebug] A quest mod is installed; complete the configured mission instead."));
            return 0;
        }

        GhostRestrictionsHandler.clearWorldDebuff(player.getLevel());
        player.getServer().getPlayerList().broadcastMessage(
                new StringTextComponent("[RPGCore] Fallback purification completed. The world debuff was removed."),
                net.minecraft.util.text.ChatType.SYSTEM, Util.NIL_UUID
        );
        return 1;
    }


    private static ServerPlayerEntity getPlayer(CommandSource src) {
        try {
            return src.getPlayerOrException();
        } catch (Exception e) {
            src.sendFailure(new StringTextComponent("[RPGDebug] Solo disponible para jugadores."));
            return null;
        }
    }

    private static int getLives(ServerPlayerEntity player) {
        return player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(PlayerLives::getLives).orElse(0);
    }

    private static void setLivesAndSync(ServerPlayerEntity player, int lives) {
        if (lives > 0) {
            GhostRestrictionsHandler.removeGhostAbilities(player);
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .ifPresent(cap -> cap.setLives(lives));
        GhostDeathHandler.syncMaxHealthServer(player);
        GhostDeathHandler.sendLivesSync(player, lives);

        ModifiableAttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
                attr.removeModifier(mod.getId());
            }
            double targetMaxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
            attr.setBaseValue(targetMaxHp);
        }

        if (lives > 0) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void forceGhost(ServerPlayerEntity player) {
        player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .ifPresent(cap -> cap.setLives(0));
        ModifiableAttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(SACRIFICIO_UUID);
            attr.setBaseValue(2.0D);
        }
        player.setHealth(1.0F);
        GhostRestrictionsHandler.applyGhostAbilities(player);
        GhostDeathHandler.sendLivesSync(player, 0);

        LostSoulEntity.spawn(player);
    }
}

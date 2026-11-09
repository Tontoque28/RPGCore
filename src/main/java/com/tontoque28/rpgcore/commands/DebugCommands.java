package com.tontoque28.rpgcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.events.RitualSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class DebugCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
        registerPublic(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ItemStack vessel = new ItemStack(RpgCore.SPIRITUAL_VESSEL.get());

                                    if (!player.getInventory().add(vessel)) {
                                        player.drop(vessel, false);
                                    }

                                    context.getSource().sendSuccess(() -> Component.literal("[RPGDebug] Vasija Espiritual obtenida."), false);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("altar")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    BlockPos center = player.blockPosition();

                                    AABB area = new AABB(center).inflate(10.0D);
                                    level.getEntitiesOfClass(EndCrystal.class, area).forEach(EndCrystal::discard);

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
                                        EndCrystal crystal = new EndCrystal(
                                                level,
                                                crystalPos.getX() + 0.5D,
                                                crystalPos.getY(),
                                                crystalPos.getZ() + 0.5D
                                        );
                                        crystal.setShowBottom(false);
                                        level.addFreshEntity(crystal);
                                    }

                                    player.teleportTo(center.getX() + 0.5D, center.getY(), center.getZ() + 2.5D);

                                    context.getSource().sendSuccess(() -> Component.literal("[RPGDebug] Altar de resurrección generado."), false);
                                    return 1;
                                })
                        )
        );
    }

        private static void registerPublic(CommandDispatcher<CommandSourceStack> dispatcher) {
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


    private static int cmdLives(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        int lives = getLives(player);
        boolean isGhost = lives <= 0;
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        src.sendSuccess(() -> Component.literal(
                "[RPGDebug] Vidas (Corazones): " + lives
                        + " | HP: " + hp + "/" + maxHp
                        + " | Fantasma: " + isGhost
                        + " | Invisible: " + player.isInvisible()
                        + " | Invulnerable: " + player.isInvulnerable()), false);
        return 1;
    }

    private static int cmdHeart(CommandSourceStack src, int amount) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        ItemStack hearts = new ItemStack(RpgCore.LIFE_HEART.get(), amount);
        if (!player.getInventory().add(hearts)) {
            player.drop(hearts, false);
        }
        src.sendSuccess(() -> Component.literal(
                "[RPGDebug] Given " + amount + " Life Heart(s)."), false);
        return 1;
    }

    private static int cmdDebuff(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        player.sendSystemMessage(GhostRestrictionsHandler.getActiveWorldDebuffMessage(player));
        return 1;
    }

    private static int cmdPublicDebuffs(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        for (Component row : GhostRestrictionsHandler.getActiveWorldDebuffTable(player)) {
            player.sendSystemMessage(row);
        }
        return 1;
    }

    private static int cmdBook(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        ItemStack book = new ItemStack(RpgCore.GUIDE_BOOK.get());
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        src.sendSuccess(() -> Component.literal("[RPGCore] RPGCore Book entregado."), false);
        return 1;
    }

    private static int cmdReviveAll(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.reviveAll(player.serverLevel());
        return 1;
    }

    private static int cmdLoseWorld(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.loseWorld(player.serverLevel());
        return 1;
    }

    private static int cmdSet(CommandSourceStack src, int amount) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        setLivesAndSync(player, amount);
        src.sendSuccess(() -> Component.literal("[RPGDebug] Vidas establecidas a " + amount + " (" + amount + " corazones)."), false);
        return 1;
    }

    private static int cmdAdd(CommandSourceStack src, int amount) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> cap.addLives(amount));
        int now = getLives(player);
        setLivesAndSync(player, now);
        src.sendSuccess(() -> Component.literal("[RPGDebug] +" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)"), false);
        return 1;
    }

    private static int cmdRemove(CommandSourceStack src, int amount) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> cap.removeLives(amount));
        int now = getLives(player);
        setLivesAndSync(player, now);
        src.sendSuccess(() -> Component.literal("[RPGDebug] -" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)"), false);
        return 1;
    }

    private static int cmdKill(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        int lives = getLives(player);
        if (lives <= 0) {
            src.sendSuccess(() -> Component.literal("[RPGDebug] Ya eres fantasma (0 vidas)."), false);
            return 0;
        }

        if (lives > 1) {
            setLivesAndSync(player, lives - 1);
            player.kill();
            src.sendSuccess(() -> Component.literal("[RPGDebug] Muerte forzada. Vidas restantes: " + (lives - 1)), false);
        } else {
            forceGhost(player);
            src.sendSuccess(() -> Component.literal("[RPGDebug] Última vida agotada → FANTASMA."), false);
        }
        return 1;
    }

    private static int cmdGhost(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        forceGhost(player);
        src.sendSuccess(() -> Component.literal("[RPGDebug] Estado FANTASMA activado."), false);
        return 1;
    }

    private static int cmdRevive(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.removeGhostAbilities(player);
        setLivesAndSync(player, 10);
        player.clearFire();

        src.sendSuccess(() -> Component.literal("[RPGDebug] Estado normal restaurado (10 corazones)."), false);
        return 1;
    }

    private static int cmdSoul(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        LostSoulEntity soul = LostSoulEntity.spawn(player);
        if (soul != null) {
            src.sendSuccess(() -> Component.literal(
                    "[RPGDebug] Alma spawneada en " + soul.blockPosition()), false);
        } else {
            src.sendFailure(Component.literal("[RPGDebug] Error al spawnear el alma."));
        }
        return 1;
    }

    private static int cmdReset(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        GhostRestrictionsHandler.removeGhostAbilities(player);
        setLivesAndSync(player, 10);
        player.clearFire();
        GhostRestrictionsHandler.clearWorldDebuff(player.serverLevel());

        src.sendSuccess(() -> Component.literal("[RPGDebug] Sistema reseteado a 10 vidas (10 corazones completos)."), false);
        return 1;
    }

    private static int cmdClearDebuff(CommandSourceStack src) {
        ServerPlayer player = getPlayer(src);
        if (player == null) return 0;

        if (ModList.get().isLoaded("ftbquests") || ModList.get().isLoaded("heracles")) {
            src.sendFailure(Component.literal(
                    "[RPGDebug] A quest mod is installed; complete the configured mission instead."));
            return 0;
        }

        GhostRestrictionsHandler.clearWorldDebuff(player.serverLevel());
        player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("[RPGCore] Fallback purification completed. The world debuff was removed."),
                false
        );
        return 1;
    }


    private static ServerPlayer getPlayer(CommandSourceStack src) {
        try {
            return src.getPlayerOrException();
        } catch (Exception e) {
            src.sendFailure(Component.literal("[RPGDebug] Solo disponible para jugadores."));
            return null;
        }
    }

    private static int getLives(ServerPlayer player) {
        return player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(PlayerLives::getLives).orElse(0);
    }

    private static void setLivesAndSync(ServerPlayer player, int lives) {
        if (lives > 0) {
            GhostRestrictionsHandler.removeGhostAbilities(player);
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .ifPresent(cap -> cap.setLives(lives));
        GhostDeathHandler.syncMaxHealthServer(player);
        GhostDeathHandler.sendLivesSync(player, lives);

        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            for (var mod : new java.util.ArrayList<>(attr.getModifiers())) {
                attr.removeModifier(mod.id());
            }
            double targetMaxHp = lives > 0 ? (lives * 2.0D) : 2.0D;
            attr.setBaseValue(targetMaxHp);
        }

        if (lives > 0) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void forceGhost(ServerPlayer player) {
        player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .ifPresent(cap -> cap.setLives(0));
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(RitualSystem.SACRIFICIO_ID);
            attr.setBaseValue(2.0D);
        }
        player.setHealth(1.0F);
        GhostRestrictionsHandler.applyGhostAbilities(player);
        GhostDeathHandler.sendLivesSync(player, 0);

        LostSoulEntity.spawn(player);
    }
}
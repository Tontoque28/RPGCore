package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.LanguageHolder;
import com.tontoque28.rpgcore.capability.PersistentDataHolder;
import com.tontoque28.rpgcore.capability.PlayerLives;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GhostRestrictionsHandler {

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";
    public static final String TAG_GHOST_STATE = "rpgcore_is_ghost";

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
                onPlayerTick(player);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isGhost(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                isGhost(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        UseItemCallback.EVENT.register((player, world, hand) ->
                isGhost(player) ? InteractionResultHolder.fail(player.getItemInHand(hand)) : InteractionResultHolder.pass(player.getItemInHand(hand)));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                isGhost(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !isGhost(player));
    }

    private static WorldDebuffs.State getWorldDebuffState(ServerLevel level) {
        return WorldDebuffs.getState(level);
    }

    public static boolean isSpanish(ServerPlayer player) {
        String language = ((LanguageHolder) player).rpgcore$getLanguage();
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("es");
    }

    public static boolean isGhost(Player player) {
        boolean livesGhost = PlayerLives.getLives(player) <= 0;
        return livesGhost || PersistentDataHolder.of(player).getBoolean(TAG_GHOST_STATE);
    }

    public static void applyGhostAbilities(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.noPhysics = true;
        player.setBoundingBox(new AABB(player.getX() - 0.2D, player.getY(), player.getZ() - 0.2D,
                player.getX() + 0.2D, player.getY() + 1.8D, player.getZ() + 0.2D));
        player.setSilent(true);

        player.abilities.mayfly = true;
        player.abilities.flying = true;
        player.abilities.invulnerable = true;
        player.abilities.mayBuild = false;
        player.onUpdateAbilities();

        PlayerLives.setLives(player, 0);
        PersistentDataHolder.of(player).putBoolean(TAG_GHOST_STATE, true);
        player.setHealth(1.0F);

        PersistentDataHolder.of(player).putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        PersistentDataHolder.of(player).putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        PersistentDataHolder.of(player).putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());

        clearGhostTargets(player.getLevel(), player);
    }

    public static void removeGhostAbilities(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setNoGravity(false);
        player.noPhysics = false;
        player.setSilent(false);

        player.abilities.mayfly = false;
        player.abilities.flying = false;
        player.abilities.invulnerable = false;
        player.abilities.mayBuild = true;
        player.onUpdateAbilities();
        PersistentDataHolder.of(player).putBoolean(TAG_GHOST_STATE, false);

        if (PlayerLives.getLives(player) <= 0) {
            PlayerLives.setLives(player, 1);
        }
        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
    }

    private static void onPlayerTick(ServerPlayer player) {
        if (player.level instanceof ServerLevel) {
            ServerLevel level = (ServerLevel) player.level;
            WorldDebuffs.State state = getWorldDebuffState(level);
            if (state.worldLost) {
                if (!player.isSpectator()) {
                    player.setGameMode(GameType.SPECTATOR);
                }
                return;
            }

            applyWorldDebuff(level, player);

            if (countLivingPlayers(level) > 0 && state.allDeadHandled) {
                state.allDeadHandled = false;
                state.setDirty();
            }
        }

        if (isGhost(player)) {
            player.noPhysics = true;
            player.setInvulnerable(true);

            if (!player.abilities.mayfly || !player.abilities.flying) {
                player.abilities.mayfly = true;
                player.abilities.flying = true;
                player.onUpdateAbilities();
            }

            player.setBoundingBox(new AABB(player.getX() - 0.08D, player.getY(), player.getZ() - 0.08D,
                    player.getX() + 0.08D, player.getY() + 0.25D, player.getZ() + 0.08D));

            clearGhostTargets(player.getLevel(), player);

            if (player.level instanceof ServerLevel) {
                ServerLevel level = (ServerLevel) player.level;
                if (level.getGameTime() % 40L == 0L) {
                    level.sendParticles(
                            ParticleTypes.SOUL,
                            player.getX(),
                            player.getY() + 1.1D,
                            player.getZ(),
                            2, 0.2D, 0.2D, 0.2D, 0.02D
                    );
                }

                if (countLivingPlayers(level) == 0) {
                    triggerWorldDebuff(level);
                }
            }
        } else {
            player.noPhysics = false;
            player.setSilent(false);
        }
    }

    private static void applyWorldDebuff(ServerLevel level, ServerPlayer player) {
        WorldDebuffs.State state = getWorldDebuffState(level);
        if (state.debuffIndices.isEmpty() || isGhost(player)) {
            return;
        }

        for (int debuffIndex : state.debuffIndices) {
            MobEffect effect = WorldDebuffs.getEffect(debuffIndex);
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect, 80, 0, true, false));
            }
        }
    }

    public static void clearGhostTargets(ServerLevel level, Player ghostPlayer) {
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, ghostPlayer.getBoundingBox().inflate(32.0D))) {
            if (living == ghostPlayer) {
                continue;
            }

            if (living instanceof Mob && ((Mob) living).getTarget() == ghostPlayer) {
                ((Mob) living).setTarget(null);
            }
        }
    }

    public static int countLivingPlayers(ServerLevel level) {
        int count = 0;
        for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            if (!isGhost(serverPlayer)) {
                count++;
            }
        }
        return count;
    }

    public static void triggerWorldDebuff(ServerLevel level) {
        WorldDebuffs.State state = getWorldDebuffState(level);
        if (state.worldLost || state.allDeadHandled) {
            return;
        }

        state.allDeadHandled = true;
        List<Integer> available = new ArrayList<>();
        for (int index = 0; index < WorldDebuffs.WORLD_DEBUFFS.length; index++) {
            if (!state.hasDebuff(index)) {
                available.add(index);
            }
        }

        if (!available.isEmpty()) {
            int selectedIndex = available.get(new Random().nextInt(available.size()));
            state.debuffIndices.add(selectedIndex);
        }
        state.setDirty();

        for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            boolean spanish = isSpanish(serverPlayer);
            serverPlayer.sendMessage(new TextComponent(spanish
                    ? "[MUNDO] Todos murieron. Elige una opcion:"
                    : "[WORLD] Everyone died. Choose an option:").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Util.NIL_UUID);
            serverPlayer.sendMessage(choiceButton(spanish ? "[REVIVIR A TODOS]" : "[REVIVE ALL]", "/rpgcore revive_all", ChatFormatting.GREEN), Util.NIL_UUID);
            serverPlayer.sendMessage(choiceButton(spanish ? "[PERDER EL MUNDO]" : "[LOSE WORLD]", "/rpgcore lose_world", ChatFormatting.DARK_RED), Util.NIL_UUID);
        }
    }

    public static Component getActiveWorldDebuffMessage(ServerPlayer player) {
        WorldDebuffs.State state = getWorldDebuffState(player.getLevel());
        boolean spanish = isSpanish(player);
        if (state.debuffIndices.isEmpty()) {
            return new TextComponent(spanish ? "[RPGCore] No hay debuffs mundiales activos." : "[RPGCore] No active world debuffs.");
        }

        return new TextComponent(spanish ? "[MUNDO] Debuffs activos" : "[WORLD] Active debuffs")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static List<Component> getActiveWorldDebuffTable(ServerPlayer player) {
        WorldDebuffs.State state = getWorldDebuffState(player.getLevel());
        boolean spanish = isSpanish(player);
        List<Component> rows = new ArrayList<>();
        rows.add(new TextComponent(spanish ? "┌ Debuffs mundiales ┐" : "┌ Active world debuffs ┐")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (state.debuffIndices.isEmpty()) {
            rows.add(new TextComponent(spanish ? "└ Ninguno activo ┘" : "└ None active ┘").withStyle(ChatFormatting.GRAY));
            return rows;
        }
        for (int i = 0; i < state.debuffIndices.size(); i++) {
            WorldDebuffs.WorldDebuff debuff = WorldDebuffs.WORLD_DEBUFFS[state.debuffIndices.get(i)];
            String description = spanish ? debuff.spanish : debuff.english;
            String name = description.split(" — ", 2)[0];
            String effect = WorldDebuffs.effectName(WorldDebuffs.getEffect(state.debuffIndices.get(i)), spanish);
            rows.add(new TextComponent("│ " + (i + 1) + ". " + name + " -> " + effect + " │")
                    .withStyle(ChatFormatting.RED));
        }
        rows.add(new TextComponent("└ " + (spanish ? "Persistentes" : "Persistent") + " ┘")
                .withStyle(ChatFormatting.DARK_GRAY));
        return rows;
    }

    public static void clearWorldDebuff(ServerLevel level) {
        WorldDebuffs.State state = getWorldDebuffState(level);
        if (!state.debuffIndices.isEmpty()) {
            for (int debuffIndex : state.debuffIndices) {
                MobEffect effect = WorldDebuffs.getEffect(debuffIndex);
                if (effect != null) {
                    for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                        player.removeEffect(effect);
                    }
                }
            }
            state.debuffIndices.clear();
            state.setDirty();
        }
    }

    public static void reviveAll(ServerLevel level) {
        WorldDebuffs.State state = getWorldDebuffState(level);
        if (countLivingPlayers(level) > 0 || state.worldLost) {
            return;
        }
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (isGhost(player)) {
                removeGhostAbilities(player);
                level.getServer().getPlayerList().broadcastMessage(
                        new TextComponent(player.getName().getString() + " ha sido revivido")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), ChatType.SYSTEM, Util.NIL_UUID);
                for (ServerPlayer listener : level.getServer().getPlayerList().getPlayers()) {
                    listener.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
        state.allDeadHandled = false;
        state.setDirty();
    }

    public static void loseWorld(ServerLevel level) {
        WorldDebuffs.State state = getWorldDebuffState(level);
        if (state.worldLost) {
            return;
        }
        state.worldLost = true;
        state.setDirty();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.setGameMode(GameType.SPECTATOR);
        }
        level.getServer().getPlayerList().broadcastMessage(
                new TextComponent("[RPGCore] The world has been lost. This save is now locked in hardcore mode."), ChatType.SYSTEM, Util.NIL_UUID);
    }

    private static Component choiceButton(String label, String command, ChatFormatting color) {
        return new TextComponent(label).withStyle(style -> style
                .withColor(color)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to choose"))));
    }
}

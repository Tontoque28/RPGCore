package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RitualSystem {

    public static final ResourceLocation SACRIFICIO_ID = ResourceLocation.fromNamespaceAndPath("rpgcore", "sacrificio");
    private static final List<ActiveRitual> activeRituals = new ArrayList<>();

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                onRightClickAnchor(player, level, hand, hitResult.getBlockPos()));
        ServerTickEvents.END_WORLD_TICK.register(RitualSystem::onLevelTick);
    }

    public static void clear() {
        activeRituals.clear();
    }

    private static InteractionResult onRightClickAnchor(Player player, Level level, InteractionHand hand, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (!state.is(Blocks.RESPAWN_ANCHOR)) return InteractionResult.PASS;

        if (esRitualActivo(pos)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack mainHandStack = player.getMainHandItem();

        if (mainHandStack.getItem() instanceof SpiritualVesselItem && SpiritualVesselItem.isBound(mainHandStack)) {

            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.SUCCESS;
            }

            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth == null || maxHealth.getValue() < 40.0D) {
                player.sendSystemMessage(Component.literal("[SYSTEM] Eres demasiado debil para realizar este ritual"));
                return InteractionResult.SUCCESS;
            }

            if (!verificarPilares(level, pos)) {
                player.sendSystemMessage(Component.literal("[SYSTEM] La estructura del altar está incompleta o faltan los cristales."));
                return InteractionResult.SUCCESS;
            }

            if (state.getValue(RespawnAnchorBlock.CHARGE) > 0) {
                player.sendSystemMessage(Component.literal("[SYSTEM] El nexo ya está en uso."));
                return InteractionResult.SUCCESS;
            }

            UUID soulUuid = SpiritualVesselItem.getSoulUuid(mainHandStack);
            SpiritualVesselItem.unbindSoul(mainHandStack);

            drenarVida(player);
            level.setBlockAndUpdate(pos, state.setValue(RespawnAnchorBlock.CHARGE, 1));
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);

            apuntarCristales(level, pos, pos);

            activeRituals.add(new ActiveRitual(pos, player.getUUID(), soulUuid, level.dimension()));
            player.sendSystemMessage(Component.literal("[SYSTEM] El nexo empieza a absorber tu energía vital..."));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static void onLevelTick(ServerLevel level) {
        List<ActiveRitual> toRemove = new ArrayList<>();

        for (ActiveRitual ritual : new ArrayList<>(activeRituals)) {
            if (level.dimension() != ritual.dimension) continue;

            ritual.ticksElapsed++;

            apuntarCristales(level, ritual.pos, ritual.pos);

            if (ritual.ticksElapsed % 60 == 0) {
                ritual.chargeLevel++;

                if (ritual.chargeLevel <= 4) {
                    Player reviver = level.getPlayerByUUID(ritual.reviverUuid);
                    if (reviver != null) {
                        drenarVida(reviver);
                    }

                    BlockState currentState = level.getBlockState(ritual.pos);
                    if (currentState.is(Blocks.RESPAWN_ANCHOR)) {
                        level.setBlockAndUpdate(ritual.pos, currentState.setValue(RespawnAnchorBlock.CHARGE, ritual.chargeLevel));
                        level.playSound(null, ritual.pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
                else {
                    apuntarCristales(level, ritual.pos, null);

                    if (level.getBlockState(ritual.pos).is(Blocks.RESPAWN_ANCHOR)) {
                        level.destroyBlock(ritual.pos, false);
                    }

                    level.sendParticles(
                            ParticleTypes.EXPLOSION_EMITTER,
                            ritual.pos.getX() + 0.5,
                            ritual.pos.getY() + 0.5,
                            ritual.pos.getZ() + 0.5,
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                    level.playSound(
                            null,
                            ritual.pos,
                            SoundEvents.GENERIC_EXPLODE.value(),
                            SoundSource.BLOCKS,
                            4.0F,
                            1.0F
                    );

                    if (level.getServer() != null) {
                        ServerPlayer soulPlayer = level.getServer().getPlayerList().getPlayer(ritual.soulUuid);
                        if (soulPlayer != null) {
                            if (PlayerLives.getLives(soulPlayer) <= 0) {
                                PlayerLives.setLives(soulPlayer, 10);
                            }
                            GhostRestrictionsHandler.removeGhostAbilities(soulPlayer);
                            soulPlayer.setHealth(soulPlayer.getMaxHealth());
                            GhostDeathHandler.sendLivesSync(soulPlayer, PlayerLives.getLives(soulPlayer));

                            soulPlayer.teleportTo(ritual.pos.getX() + 0.5, ritual.pos.getY(), ritual.pos.getZ() + 0.5);
                            soulPlayer.sendSystemMessage(Component.literal("[SYSTEM] Has regresado al mundo de los vivos."));
                        }
                    }

                    toRemove.add(ritual);
                }
            }
        }

        activeRituals.removeAll(toRemove);
    }

    private static boolean esRitualActivo(BlockPos pos) {
        for (ActiveRitual ritual : activeRituals) {
            if (ritual.pos.equals(pos)) return true;
        }
        return false;
    }

    private static void apuntarCristales(Level level, BlockPos center, BlockPos target) {
        AABB searchArea = new AABB(center).inflate(10.0D);
        List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, searchArea);
        for (EndCrystal crystal : crystals) {
            crystal.setBeamTarget(target != null ? target.below() : null);
        }
    }

    private static void drenarVida(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(SACRIFICIO_ID);

            double nuevaVidaMax = Math.max(2.0D, maxHealth.getBaseValue() - 5.0D);
            maxHealth.setBaseValue(nuevaVidaMax);

            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }

            int nuevasVidas = (int) Math.round(nuevaVidaMax / 2.0D);
            PlayerLives.setLives(player, nuevasVidas);

            if (player instanceof ServerPlayer sp) {
                GhostDeathHandler.sendLivesSync(sp, nuevasVidas);
            }
        }
    }

    private static boolean verificarPilares(Level level, BlockPos center) {
        int[][] offsets = {{4, 4}, {4, -4}, {-4, 4}, {-4, -4}};
        for (int[] offset : offsets) {
            BlockPos base = center.offset(offset[0], 0, offset[1]);

            boolean bloquesValidos = level.getBlockState(base).is(Blocks.NETHERITE_BLOCK)
                    && level.getBlockState(base.above(1)).is(Blocks.CRYING_OBSIDIAN)
                    && level.getBlockState(base.above(2)).is(Blocks.OBSIDIAN);

            if (!bloquesValidos) {
                return false;
            }
        }

        AABB searchArea = new AABB(center).inflate(10.0D);
        List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, searchArea);
        return crystals.size() >= 4;
    }

    private static class ActiveRitual {
        BlockPos pos;
        UUID reviverUuid;
        UUID soulUuid;
        ResourceKey<Level> dimension;
        int ticksElapsed = 0;
        int chargeLevel = 1;

        ActiveRitual(BlockPos pos, UUID reviver, UUID soul, ResourceKey<Level> dim) {
            this.pos = pos;
            this.reviverUuid = reviver;
            this.soulUuid = soul;
            this.dimension = dim;
        }
    }
}

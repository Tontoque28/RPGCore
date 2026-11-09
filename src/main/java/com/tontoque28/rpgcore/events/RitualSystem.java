package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class RitualSystem {

    public static final UUID SACRIFICIO_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final List<ActiveRitual> activeRituals = new ArrayList<>();

    private static void tell(PlayerEntity player, String text) {
        player.sendMessage(new StringTextComponent(text), Util.NIL_UUID);
    }

    @SubscribeEvent
    public static void onRightClickAnchor(PlayerInteractEvent.RightClickBlock event) {
        World level = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(Blocks.RESPAWN_ANCHOR)) return;

        if (esRitualActivo(pos)) {
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
            return;
        }

        PlayerEntity player = event.getPlayer();
        ItemStack mainHandStack = player.getMainHandItem();

        if (mainHandStack.getItem() instanceof SpiritualVesselItem && SpiritualVesselItem.isBound(mainHandStack)) {

            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);

            if (level.isClientSide() || event.getHand() != Hand.MAIN_HAND) {
                return;
            }

            ModifiableAttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth == null || maxHealth.getValue() < 40.0D) {
                tell(player, "[SYSTEM] Eres demasiado debil para realizar este ritual");
                return;
            }

            if (!verificarPilares(level, pos)) {
                tell(player, "[SYSTEM] La estructura del altar está incompleta o faltan los cristales.");
                return;
            }

            if (state.getValue(RespawnAnchorBlock.CHARGE) > 0) {
                tell(player, "[SYSTEM] El nexo ya está en uso.");
                return;
            }

            UUID soulUuid = mainHandStack.getTag().getUUID("SoulUUID");
            mainHandStack.removeTagKey("SoulUUID");
            mainHandStack.removeTagKey("SoulName");

            drenarVida(player);
            level.setBlockAndUpdate(pos, state.setValue(RespawnAnchorBlock.CHARGE, 1));
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);

            apuntarCristales(level, pos, pos);

            activeRituals.add(new ActiveRitual(pos, player.getUUID(), soulUuid, level.dimension()));
            tell(player, "[SYSTEM] El nexo empieza a absorber tu energía vital...");
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) return;

        List<ActiveRitual> toRemove = new ArrayList<>();

        for (ActiveRitual ritual : activeRituals) {
            if (event.world.dimension() != ritual.dimension) continue;

            ritual.ticksElapsed++;

            apuntarCristales(event.world, ritual.pos, ritual.pos);

            if (ritual.ticksElapsed % 60 == 0) {
                ritual.chargeLevel++;

                if (ritual.chargeLevel <= 4) {
                    PlayerEntity reviver = event.world.getPlayerByUUID(ritual.reviverUuid);
                    if (reviver != null) {
                        drenarVida(reviver);
                    }

                    BlockState currentState = event.world.getBlockState(ritual.pos);
                    if (currentState.is(Blocks.RESPAWN_ANCHOR)) {
                        event.world.setBlockAndUpdate(ritual.pos, currentState.setValue(RespawnAnchorBlock.CHARGE, ritual.chargeLevel));
                        event.world.playSound(null, ritual.pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
                else {
                    apuntarCristales(event.world, ritual.pos, null);

                    if (event.world.getBlockState(ritual.pos).is(Blocks.RESPAWN_ANCHOR)) {
                        event.world.destroyBlock(ritual.pos, false);
                    }

                    if (event.world instanceof ServerWorld) {
                        ServerWorld serverLevel = (ServerWorld) event.world;
                        serverLevel.sendParticles(
                                ParticleTypes.EXPLOSION_EMITTER,
                                ritual.pos.getX() + 0.5,
                                ritual.pos.getY() + 0.5,
                                ritual.pos.getZ() + 0.5,
                                1, 0.0, 0.0, 0.0, 0.0
                        );
                    }
                    event.world.playSound(
                            null,
                            ritual.pos,
                            SoundEvents.GENERIC_EXPLODE,
                            SoundCategory.BLOCKS,
                            4.0F,
                            1.0F
                    );

                    if (event.world.getServer() != null) {
                        ServerPlayerEntity soulPlayer = event.world.getServer().getPlayerList().getPlayer(ritual.soulUuid);
                        if (soulPlayer != null) {
                            soulPlayer.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                                if (cap.getLives() <= 0) {
                                    cap.setLives(10);
                                }
                                GhostRestrictionsHandler.removeGhostAbilities(soulPlayer);
                                soulPlayer.setHealth(soulPlayer.getMaxHealth());
                                GhostDeathHandler.sendLivesSync(soulPlayer, cap.getLives());
                            });

                            soulPlayer.teleportTo(ritual.pos.getX() + 0.5, ritual.pos.getY(), ritual.pos.getZ() + 0.5);
                            tell(soulPlayer, "[SYSTEM] Has regresado al mundo de los vivos.");
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

    private static void apuntarCristales(World level, BlockPos center, BlockPos target) {
        AxisAlignedBB searchArea = new AxisAlignedBB(center).inflate(10.0D);
        List<EnderCrystalEntity> crystals = level.getEntitiesOfClass(EnderCrystalEntity.class, searchArea);
        for (EnderCrystalEntity crystal : crystals) {
            crystal.setBeamTarget(target != null ? target.below() : null);
        }
    }

    private static void drenarVida(PlayerEntity player) {
        ModifiableAttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(SACRIFICIO_UUID);

            double nuevaVidaMax = Math.max(2.0D, maxHealth.getBaseValue() - 5.0D);
            maxHealth.setBaseValue(nuevaVidaMax);

            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }

            int nuevasVidas = (int) Math.round(nuevaVidaMax / 2.0D);
            player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(cap -> {
                cap.setLives(nuevasVidas);
            });

            if (player instanceof ServerPlayerEntity) {
                GhostDeathHandler.sendLivesSync((ServerPlayerEntity) player, nuevasVidas);
            }
        }
    }

    private static boolean verificarPilares(World level, BlockPos center) {
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

        AxisAlignedBB searchArea = new AxisAlignedBB(center).inflate(10.0D);
        List<EnderCrystalEntity> crystals = level.getEntitiesOfClass(EnderCrystalEntity.class, searchArea);
        return crystals.size() >= 4;
    }

    private static class ActiveRitual {
        BlockPos pos;
        UUID reviverUuid;
        UUID soulUuid;
        RegistryKey<World> dimension;
        int ticksElapsed = 0;
        int chargeLevel = 1;

        ActiveRitual(BlockPos pos, UUID reviver, UUID soul, RegistryKey<World> dim) {
            this.pos = pos;
            this.reviverUuid = reviver;
            this.soulUuid = soul;
            this.dimension = dim;
        }
    }
}

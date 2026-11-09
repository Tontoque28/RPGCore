package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class RitualSystem {

    public static final UUID SACRIFICIO_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final List<ActiveRitual> activeRituals = new ArrayList<ActiveRitual>();

    private static void tell(EntityPlayer player, String text) {
        player.sendMessage(new TextComponentString(text));
    }

    @SubscribeEvent
    public static void onRightClickAnchor(PlayerInteractEvent.RightClickBlock event) {
        World level = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = level.getBlockState(pos);

        if (state.getBlock() != Blocks.BEACON) {
            return;
        }

        if (esRitualActivo(pos)) {
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack mainHandStack = player.getHeldItemMainhand();

        if (mainHandStack.getItem() instanceof SpiritualVesselItem && SpiritualVesselItem.isBound(mainHandStack)) {

            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);

            if (level.isRemote || event.getHand() != EnumHand.MAIN_HAND) {
                return;
            }

            IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            if (maxHealth == null || maxHealth.getAttributeValue() < 40.0D) {
                tell(player, "[SYSTEM] Eres demasiado debil para realizar este ritual");
                return;
            }

            if (!verificarPilares(level, pos)) {
                tell(player, "[SYSTEM] La estructura del altar está incompleta o faltan los cristales.");
                return;
            }

            UUID soulUuid = mainHandStack.getTagCompound().getUniqueId("SoulUUID");
            mainHandStack.getTagCompound().removeTag("SoulUUIDMost");
            mainHandStack.getTagCompound().removeTag("SoulUUIDLeast");
            mainHandStack.getTagCompound().removeTag("SoulName");

            drenarVida(player);
            level.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

            apuntarCristales(level, pos, pos);

            activeRituals.add(new ActiveRitual(pos, player.getUniqueID(), soulUuid, level.provider.getDimension()));
            tell(player, "[SYSTEM] El nexo empieza a absorber tu energía vital...");
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }

        List<ActiveRitual> toRemove = new ArrayList<ActiveRitual>();

        for (ActiveRitual ritual : activeRituals) {
            if (event.world.provider.getDimension() != ritual.dimension) {
                continue;
            }

            ritual.ticksElapsed++;

            apuntarCristales(event.world, ritual.pos, ritual.pos);

            if (ritual.ticksElapsed % 60 == 0) {
                ritual.chargeLevel++;

                if (ritual.chargeLevel <= 4) {
                    EntityPlayer reviver = event.world.getPlayerEntityByUUID(ritual.reviverUuid);
                    if (reviver != null) {
                        drenarVida(reviver);
                    }

                    IBlockState currentState = event.world.getBlockState(ritual.pos);
                    if (currentState.getBlock() == Blocks.BEACON) {
                        event.world.playSound(null, ritual.pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    apuntarCristales(event.world, ritual.pos, null);

                    if (event.world.getBlockState(ritual.pos).getBlock() == Blocks.BEACON) {
                        event.world.destroyBlock(ritual.pos, false);
                    }

                    if (event.world instanceof WorldServer) {
                        WorldServer serverLevel = (WorldServer) event.world;
                        serverLevel.spawnParticle(
                                EnumParticleTypes.EXPLOSION_HUGE,
                                ritual.pos.getX() + 0.5,
                                ritual.pos.getY() + 0.5,
                                ritual.pos.getZ() + 0.5,
                                1, 0.0, 0.0, 0.0, 0.0
                        );
                    }
                    event.world.playSound(
                            null,
                            ritual.pos,
                            SoundEvents.ENTITY_GENERIC_EXPLODE,
                            SoundCategory.BLOCKS,
                            4.0F,
                            1.0F
                    );

                    if (event.world.getMinecraftServer() != null) {
                        final EntityPlayerMP soulPlayer = event.world.getMinecraftServer().getPlayerList().getPlayerByUUID(ritual.soulUuid);
                        if (soulPlayer != null) {
                            PlayerLives cap = PlayerLivesProvider.of(soulPlayer);
                            if (cap != null) {
                                if (cap.getLives() <= 0) {
                                    cap.setLives(10);
                                }
                                GhostRestrictionsHandler.removeGhostAbilities(soulPlayer);
                                soulPlayer.setHealth(soulPlayer.getMaxHealth());
                                GhostDeathHandler.sendLivesSync(soulPlayer, cap.getLives());
                            }

                            soulPlayer.connection.setPlayerLocation(ritual.pos.getX() + 0.5, ritual.pos.getY(), ritual.pos.getZ() + 0.5,
                                    soulPlayer.rotationYaw, soulPlayer.rotationPitch);
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
            if (ritual.pos.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private static void apuntarCristales(World level, BlockPos center, BlockPos target) {
        AxisAlignedBB searchArea = new AxisAlignedBB(center).grow(10.0D);
        List<EntityEnderCrystal> crystals = level.getEntitiesWithinAABB(EntityEnderCrystal.class, searchArea);
        for (EntityEnderCrystal crystal : crystals) {
            crystal.setBeamTarget(target != null ? target.down() : null);
        }
    }

    private static void drenarVida(EntityPlayer player) {
        IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(SACRIFICIO_UUID);

            double nuevaVidaMax = Math.max(2.0D, maxHealth.getBaseValue() - 5.0D);
            maxHealth.setBaseValue(nuevaVidaMax);

            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }

            int nuevasVidas = (int) Math.round(nuevaVidaMax / 2.0D);
            PlayerLives cap = PlayerLivesProvider.of(player);
            if (cap != null) {
                cap.setLives(nuevasVidas);
            }

            if (player instanceof EntityPlayerMP) {
                GhostDeathHandler.sendLivesSync(player, nuevasVidas);
            }
        }
    }

    private static boolean verificarPilares(World level, BlockPos center) {
        int[][] offsets = {{4, 4}, {4, -4}, {-4, 4}, {-4, -4}};
        for (int[] offset : offsets) {
            BlockPos base = center.add(offset[0], 0, offset[1]);

            boolean bloquesValidos = level.getBlockState(base).getBlock() == Blocks.DIAMOND_BLOCK
                    && level.getBlockState(base.up(1)).getBlock() == Blocks.GLOWSTONE
                    && level.getBlockState(base.up(2)).getBlock() == Blocks.OBSIDIAN;

            if (!bloquesValidos) {
                return false;
            }
        }

        AxisAlignedBB searchArea = new AxisAlignedBB(center).grow(10.0D);
        List<EntityEnderCrystal> crystals = level.getEntitiesWithinAABB(EntityEnderCrystal.class, searchArea);
        return crystals.size() >= 4;
    }

    private static class ActiveRitual {
        BlockPos pos;
        UUID reviverUuid;
        UUID soulUuid;
        int dimension;
        int ticksElapsed = 0;
        int chargeLevel = 1;

        ActiveRitual(BlockPos pos, UUID reviver, UUID soul, int dim) {
            this.pos = pos;
            this.reviverUuid = reviver;
            this.soulUuid = soul;
            this.dimension = dim;
        }
    }
}

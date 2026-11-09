package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class GhostRestrictionsHandler {

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";
    private static final String TAG_GHOST_STATE = "rpgcore_is_ghost";

    private static WorldDebuffState getWorldDebuffState(WorldServer level) {
        return WorldDebuffState.get(level.getMinecraftServer());
    }

    public static boolean isGhost(EntityPlayer player) {
        PlayerLives lives = PlayerLivesProvider.of(player);
        boolean capabilityGhost = lives != null && lives.getLives() <= 0;
        return capabilityGhost || player.getEntityData().getBoolean(TAG_GHOST_STATE);
    }

    public static void applyGhostAbilities(EntityPlayerMP player) {
        player.setGameType(GameType.SURVIVAL);
        player.setInvisible(true);
        player.setEntityInvulnerable(true);
        player.setNoGravity(true);
        player.noClip = true;
        player.setEntityBoundingBox(new AxisAlignedBB(player.posX - 0.2D, player.posY, player.posZ - 0.2D,
                player.posX + 0.2D, player.posY + 1.8D, player.posZ + 0.2D));
        player.setSilent(true);

        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        player.capabilities.disableDamage = true;
        player.capabilities.allowEdit = false;
        player.sendPlayerAbilities();

        PlayerLives lives = PlayerLivesProvider.of(player);
        if (lives != null) {
            lives.setLives(0);
        }
        player.getEntityData().setBoolean(TAG_GHOST_STATE, true);
        player.setHealth(1.0F);

        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_X, player.posX);
        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_Y, player.posY);
        player.getEntityData().setDouble(TAG_GHOST_ANCHOR_Z, player.posZ);

        clearGhostTargets(player.getServerWorld(), player);
    }

    public static void removeGhostAbilities(EntityPlayerMP player) {
        player.setGameType(GameType.SURVIVAL);
        player.setInvisible(false);
        player.setEntityInvulnerable(false);
        player.setNoGravity(false);
        player.noClip = false;
        player.setSilent(false);

        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.capabilities.disableDamage = false;
        player.capabilities.allowEdit = true;
        player.sendPlayerAbilities();
        player.getEntityData().setBoolean(TAG_GHOST_STATE, false);

        PlayerLives lives = PlayerLivesProvider.of(player);
        if (lives != null && lives.getLives() <= 0) {
            lives.setLives(1);
        }
        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        WorldServer serverLevel = player.getServerWorld();
        WorldDebuffState state = getWorldDebuffState(serverLevel);
        if (state.worldLost) {
            if (!player.isSpectator()) {
                player.setGameType(GameType.SPECTATOR);
            }
            if (isGhost(player) && player.isInvisible()) {
                player.setInvisible(false);
            }
            return;
        }

        applyWorldDebuff(serverLevel, player);

        if (countLivingPlayers(serverLevel) > 0 && state.allDeadHandled) {
            state.allDeadHandled = false;
            state.markDirty();
        }

        if (isGhost(player)) {
            player.noClip = true;
            player.setEntityInvulnerable(true);

            if (player.isSpectator() && player.isInvisible()) {
                player.setInvisible(false);
            }

            if (!player.capabilities.allowFlying || !player.capabilities.isFlying) {
                player.capabilities.allowFlying = true;
                player.capabilities.isFlying = true;
                player.sendPlayerAbilities();
            }

            player.setEntityBoundingBox(new AxisAlignedBB(player.posX - 0.08D, player.posY, player.posZ - 0.08D,
                    player.posX + 0.08D, player.posY + 0.25D, player.posZ + 0.08D));

            clearGhostTargets(serverLevel, player);

            if (serverLevel.getTotalWorldTime() % 40L == 0L) {
                serverLevel.spawnParticle(
                        EnumParticleTypes.END_ROD,
                        player.posX,
                        player.posY + 1.1D,
                        player.posZ,
                        2, 0.2D, 0.2D, 0.2D, 0.02D
                );
            }

            if (countLivingPlayers(serverLevel) == 0) {
                triggerWorldDebuff(serverLevel);
            }
        } else {
            player.noClip = false;
            player.setSilent(false);
        }
    }

    private static void applyWorldDebuff(WorldServer level, EntityPlayerMP player) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.debuffIndices.isEmpty() || isGhost(player)) {
            return;
        }

        for (int debuffIndex : state.debuffIndices) {
            Potion effect = WorldDebuffs.effectFor(debuffIndex);
            if (effect != null) {
                player.addPotionEffect(new PotionEffect(effect, 80, 0, true, false));
            }
        }
    }

    public static void clearGhostTargets(WorldServer level, EntityPlayer ghostPlayer) {
        for (EntityLivingBase living : level.getEntitiesWithinAABB(EntityLivingBase.class, ghostPlayer.getEntityBoundingBox().grow(32.0D))) {
            if (living == ghostPlayer) {
                continue;
            }

            if (living instanceof EntityLiving && ((EntityLiving) living).getAttackTarget() == ghostPlayer) {
                ((EntityLiving) living).setAttackTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (isGhost(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isGhost(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isGhost(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isGhost(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGhostDamaged(LivingAttackEvent event) {
        if (event.getEntity() instanceof EntityPlayer && isGhost((EntityPlayer) event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGhostHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof EntityPlayer && isGhost((EntityPlayer) event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMobTargetGhost(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayer && isGhost((EntityPlayer) event.getTarget())
                && event.getEntityLiving() instanceof EntityLiving) {
            ((EntityLiving) event.getEntityLiving()).setAttackTarget(null);
        }
    }

    public static int countLivingPlayers(WorldServer level) {
        int count = 0;
        for (EntityPlayerMP serverPlayer : level.getMinecraftServer().getPlayerList().getPlayers()) {
            if (!isGhost(serverPlayer)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isSpanish(EntityPlayerMP player) {
        String language = null;
        try {
            language = ObfuscationReflectionHelper.getPrivateValue(EntityPlayerMP.class, player, "field_71148_cg", "language");
        } catch (RuntimeException ignored) {
        }
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("es");
    }

    private static void tell(EntityPlayerMP player, ITextComponent component) {
        player.sendMessage(component);
    }

    public static void triggerWorldDebuff(WorldServer level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.worldLost || state.allDeadHandled) {
            return;
        }

        state.allDeadHandled = true;
        List<Integer> available = new ArrayList<Integer>();
        for (int index = 0; index < WorldDebuffs.ALL.length; index++) {
            if (!state.hasDebuff(index)) {
                available.add(index);
            }
        }

        if (!available.isEmpty()) {
            int selectedIndex = available.get(new Random().nextInt(available.size()));
            state.debuffIndices.add(selectedIndex);
        }
        state.markDirty();

        for (EntityPlayerMP serverPlayer : level.getMinecraftServer().getPlayerList().getPlayers()) {
            boolean spanish = isSpanish(serverPlayer);
            TextComponentString title = new TextComponentString(spanish
                    ? "[MUNDO] Todos murieron. Elige una opcion:"
                    : "[WORLD] Everyone died. Choose an option:");
            title.getStyle().setColor(TextFormatting.DARK_RED).setBold(true);
            tell(serverPlayer, title);
            tell(serverPlayer, choiceButton(spanish ? "[REVIVIR A TODOS]" : "[REVIVE ALL]", "/rpgcore revive_all", TextFormatting.GREEN));
            tell(serverPlayer, choiceButton(spanish ? "[PERDER EL MUNDO]" : "[LOSE WORLD]", "/rpgcore lose_world", TextFormatting.DARK_RED));
        }
    }

    public static ITextComponent getActiveWorldDebuffMessage(EntityPlayerMP player) {
        WorldDebuffState state = getWorldDebuffState(player.getServerWorld());
        boolean spanish = isSpanish(player);
        if (state.debuffIndices.isEmpty()) {
            return new TextComponentString(spanish ? "[RPGCore] No hay debuffs mundiales activos." : "[RPGCore] No active world debuffs.");
        }

        TextComponentString message = new TextComponentString(spanish ? "[MUNDO] Debuffs activos" : "[WORLD] Active debuffs");
        message.getStyle().setColor(TextFormatting.GOLD).setBold(true);
        return message;
    }

    public static List<ITextComponent> getActiveWorldDebuffTable(EntityPlayerMP player) {
        WorldDebuffState state = getWorldDebuffState(player.getServerWorld());
        boolean spanish = isSpanish(player);
        List<ITextComponent> rows = new ArrayList<ITextComponent>();
        TextComponentString header = new TextComponentString(spanish ? "┌ Debuffs mundiales ┐" : "┌ Active world debuffs ┐");
        header.getStyle().setColor(TextFormatting.GOLD).setBold(true);
        rows.add(header);
        if (state.debuffIndices.isEmpty()) {
            TextComponentString none = new TextComponentString(spanish ? "└ Ninguno activo ┘" : "└ None active ┘");
            none.getStyle().setColor(TextFormatting.GRAY);
            rows.add(none);
            return rows;
        }
        for (int i = 0; i < state.debuffIndices.size(); i++) {
            WorldDebuffs.Entry debuff = WorldDebuffs.ALL[state.debuffIndices.get(i)];
            String description = spanish ? debuff.spanish : debuff.english;
            String name = description.split(" — ", 2)[0];
            String effect = WorldDebuffs.effectName(WorldDebuffs.effectFor(state.debuffIndices.get(i)), spanish);
            TextComponentString row = new TextComponentString("│ " + (i + 1) + ". " + name + " -> " + effect + " │");
            row.getStyle().setColor(TextFormatting.RED);
            rows.add(row);
        }
        TextComponentString footer = new TextComponentString("└ " + (spanish ? "Persistentes" : "Persistent") + " ┘");
        footer.getStyle().setColor(TextFormatting.DARK_GRAY);
        rows.add(footer);
        return rows;
    }

    public static void clearWorldDebuff(WorldServer level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (!state.debuffIndices.isEmpty()) {
            for (int debuffIndex : state.debuffIndices) {
                Potion effect = WorldDebuffs.effectFor(debuffIndex);
                if (effect != null) {
                    for (EntityPlayerMP player : level.getMinecraftServer().getPlayerList().getPlayers()) {
                        player.removePotionEffect(effect);
                    }
                }
            }
            state.debuffIndices.clear();
            state.markDirty();
        }
    }

    public static void reviveAll(WorldServer level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (countLivingPlayers(level) > 0 || state.worldLost) {
            return;
        }
        for (EntityPlayerMP player : new ArrayList<EntityPlayerMP>(level.getMinecraftServer().getPlayerList().getPlayers())) {
            if (isGhost(player)) {
                removeGhostAbilities(player);
                TextComponentString message = new TextComponentString(player.getName() + " ha sido revivido");
                message.getStyle().setColor(TextFormatting.GOLD).setBold(true);
                level.getMinecraftServer().getPlayerList().sendMessage(message);
                for (EntityPlayerMP listener : level.getMinecraftServer().getPlayerList().getPlayers()) {
                    listener.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS,
                            listener.posX, listener.posY, listener.posZ, 1.0F, 1.0F));
                }
            }
        }
        state.allDeadHandled = false;
        state.markDirty();
    }

    public static void loseWorld(WorldServer level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.worldLost) {
            return;
        }
        state.worldLost = true;
        state.markDirty();
        for (EntityPlayerMP player : level.getMinecraftServer().getPlayerList().getPlayers()) {
            player.setGameType(GameType.SPECTATOR);
        }
        level.getMinecraftServer().getPlayerList().sendMessage(
                new TextComponentString("[RPGCore] The world has been lost. This save is now locked in hardcore mode."));
    }

    private static ITextComponent choiceButton(String label, String command, TextFormatting color) {
        TextComponentString button = new TextComponentString(label);
        button.getStyle().setColor(color).setBold(true)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to choose")));
        return button;
    }
}

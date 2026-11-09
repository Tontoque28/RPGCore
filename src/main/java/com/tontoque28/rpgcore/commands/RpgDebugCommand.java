package com.tontoque28.rpgcore.commands;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.item.ModItems;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class RpgDebugCommand extends CommandBase {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "lives", "heart", "debuff", "set", "add", "remove", "kill", "ghost", "revive", "soul", "reset",
            "clear_debuff", "vessel", "altar");

    @Override
    public String getName() {
        return "rpgdebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/rpgdebug <lives|heart|debuff|set|add|remove|kill|ghost|revive|soul|reset|clear_debuff|vessel|altar>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || !SUBCOMMANDS.contains(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        String sub = args[0];
        boolean needsAmount = sub.equals("heart") || sub.equals("set") || sub.equals("add") || sub.equals("remove");
        int amount = 0;
        if (needsAmount) {
            if (args.length != 2) {
                throw new WrongUsageException(getUsage(sender));
            }
            if (sub.equals("heart")) {
                amount = parseInt(args[1], 1, 64);
            } else if (sub.equals("set")) {
                amount = parseInt(args[1], 0, 100);
            } else {
                amount = parseInt(args[1], 1, 100);
            }
        } else if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayerMP player = CommandSupport.getPlayer(sender);
        if (player == null) {
            return;
        }

        switch (sub) {
            case "lives":
                cmdLives(sender, player);
                break;
            case "heart":
                CommandSupport.give(player, new ItemStack(ModItems.LIFE_HEART, amount));
                CommandSupport.success(sender, "[RPGDebug] Given " + amount + " Life Heart(s).");
                break;
            case "debuff":
                player.sendMessage(GhostRestrictionsHandler.getActiveWorldDebuffMessage(player));
                break;
            case "set":
                CommandSupport.setLivesAndSync(player, amount);
                CommandSupport.success(sender, "[RPGDebug] Vidas establecidas a " + amount + " (" + amount + " corazones).");
                break;
            case "add":
                cmdAdd(sender, player, amount);
                break;
            case "remove":
                cmdRemove(sender, player, amount);
                break;
            case "kill":
                cmdKill(sender, player);
                break;
            case "ghost":
                CommandSupport.forceGhost(player);
                CommandSupport.success(sender, "[RPGDebug] Estado FANTASMA activado.");
                break;
            case "revive":
                GhostRestrictionsHandler.removeGhostAbilities(player);
                CommandSupport.setLivesAndSync(player, 10);
                player.extinguish();
                CommandSupport.success(sender, "[RPGDebug] Estado normal restaurado (10 corazones).");
                break;
            case "soul":
                cmdSoul(sender, player);
                break;
            case "reset":
                GhostRestrictionsHandler.removeGhostAbilities(player);
                CommandSupport.setLivesAndSync(player, 10);
                player.extinguish();
                GhostRestrictionsHandler.clearWorldDebuff(player.getServerWorld());
                CommandSupport.success(sender, "[RPGDebug] Sistema reseteado a 10 vidas (10 corazones completos).");
                break;
            case "clear_debuff":
                cmdClearDebuff(server, sender, player);
                break;
            case "vessel":
                CommandSupport.give(player, new ItemStack(ModItems.SPIRITUAL_VESSEL));
                CommandSupport.success(sender, "[RPGDebug] Vasija Espiritual obtenida.");
                break;
            case "altar":
                cmdAltar(sender, player);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private static void cmdLives(ICommandSender sender, EntityPlayerMP player) {
        int lives = CommandSupport.getLives(player);
        boolean isGhost = lives <= 0;
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        CommandSupport.success(sender, "[RPGDebug] Vidas (Corazones): " + lives
                + " | HP: " + hp + "/" + maxHp
                + " | Fantasma: " + isGhost
                + " | Invisible: " + player.isInvisible()
                + " | Invulnerable: " + player.getIsInvulnerable());
    }

    private static void cmdAdd(ICommandSender sender, EntityPlayerMP player, int amount) {
        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap != null) {
            cap.addLives(amount);
        }
        int now = CommandSupport.getLives(player);
        CommandSupport.setLivesAndSync(player, now);
        CommandSupport.success(sender, "[RPGDebug] +" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)");
    }

    private static void cmdRemove(ICommandSender sender, EntityPlayerMP player, int amount) {
        PlayerLives cap = PlayerLivesProvider.of(player);
        if (cap != null) {
            cap.removeLives(amount);
        }
        int now = CommandSupport.getLives(player);
        CommandSupport.setLivesAndSync(player, now);
        CommandSupport.success(sender, "[RPGDebug] -" + amount + " corazones → total: " + now + " (" + (now * 2) + " HP)");
    }

    private static void cmdKill(ICommandSender sender, EntityPlayerMP player) {
        int lives = CommandSupport.getLives(player);
        if (lives <= 0) {
            CommandSupport.success(sender, "[RPGDebug] Ya eres fantasma (0 vidas).");
            return;
        }

        if (lives > 1) {
            CommandSupport.setLivesAndSync(player, lives - 1);
            player.onKillCommand();
            CommandSupport.success(sender, "[RPGDebug] Muerte forzada. Vidas restantes: " + (lives - 1));
        } else {
            CommandSupport.forceGhost(player);
            CommandSupport.success(sender, "[RPGDebug] Última vida agotada → FANTASMA.");
        }
    }

    private static void cmdSoul(ICommandSender sender, EntityPlayerMP player) {
        LostSoulEntity soul = LostSoulEntity.spawn(player);
        if (soul != null) {
            CommandSupport.success(sender, "[RPGDebug] Alma spawneada en " + soul.getPosition());
        } else {
            CommandSupport.failure(sender, "[RPGDebug] Error al spawnear el alma.");
        }
    }

    private static void cmdClearDebuff(MinecraftServer server, ICommandSender sender, EntityPlayerMP player) {
        if (Loader.isModLoaded("ftbquests") || Loader.isModLoaded("heracles")) {
            CommandSupport.failure(sender, "[RPGDebug] A quest mod is installed; complete the configured mission instead.");
            return;
        }

        GhostRestrictionsHandler.clearWorldDebuff(player.getServerWorld());
        server.getPlayerList().sendMessage(
                new TextComponentString("[RPGCore] Fallback purification completed. The world debuff was removed."));
    }

    private static void cmdAltar(ICommandSender sender, EntityPlayerMP player) {
        WorldServer level = player.getServerWorld();
        BlockPos center = player.getPosition();

        AxisAlignedBB area = new AxisAlignedBB(center).grow(10.0D);
        for (EntityEnderCrystal old : level.getEntitiesWithinAABB(EntityEnderCrystal.class, area)) {
            old.setDead();
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlockState(center.add(dx, -1, dz), Blocks.GRASS.getDefaultState(), 3);
                for (int dy = 0; dy <= 4; dy++) {
                    level.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState(), 3);
                }
            }
        }

        level.setBlockState(center, Blocks.BEACON.getDefaultState(), 3);

        int[][] offsets = {{4, 4}, {4, -4}, {-4, 4}, {-4, -4}};
        for (int[] offset : offsets) {
            BlockPos base = center.add(offset[0], 0, offset[1]);

            level.setBlockState(base, Blocks.DIAMOND_BLOCK.getDefaultState(), 3);
            level.setBlockState(base.up(1), Blocks.GLOWSTONE.getDefaultState(), 3);
            level.setBlockState(base.up(2), Blocks.OBSIDIAN.getDefaultState(), 3);

            BlockPos crystalPos = base.up(3);
            EntityEnderCrystal crystal = new EntityEnderCrystal(
                    level,
                    crystalPos.getX() + 0.5D,
                    crystalPos.getY(),
                    crystalPos.getZ() + 0.5D
            );
            crystal.setShowBottom(false);
            level.spawnEntity(crystal);
        }

        player.connection.setPlayerLocation(center.getX() + 0.5D, center.getY(), center.getZ() + 2.5D,
                player.rotationYaw, player.rotationPitch);

        CommandSupport.success(sender, "[RPGDebug] Altar de resurrección generado.");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        return java.util.Collections.emptyList();
    }
}

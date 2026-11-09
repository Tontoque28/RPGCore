package com.tontoque28.rpgcore.commands;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import com.tontoque28.rpgcore.item.ModItems;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class RpgCoreCommand extends CommandBase {

    private static final List<String> SUBCOMMANDS = Arrays.asList("debuffs", "book", "revive_all", "lose_world");

    @Override
    public String getName() {
        return "rpgcore";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/rpgcore <debuffs|book|revive_all|lose_world>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1 || !SUBCOMMANDS.contains(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayerMP player = CommandSupport.getPlayer(sender);
        if (player == null) {
            return;
        }

        switch (args[0]) {
            case "debuffs":
                for (ITextComponent row : GhostRestrictionsHandler.getActiveWorldDebuffTable(player)) {
                    player.sendMessage(row);
                }
                break;
            case "book":
                CommandSupport.give(player, new ItemStack(ModItems.GUIDE_BOOK));
                CommandSupport.success(sender, "[RPGCore] RPGCore Book entregado.");
                break;
            case "revive_all":
                GhostRestrictionsHandler.reviveAll(player.getServerWorld());
                break;
            case "lose_world":
                GhostRestrictionsHandler.loseWorld(player.getServerWorld());
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        return java.util.Collections.emptyList();
    }
}

package com.tontoque28.rpgcore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(Component.literal("§6[RPGCore] Guide").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("§7• Every death removes 1 life.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• At 0 lives you become a ghost.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• Ghosts float, ignore collisions, and mobs do not target them.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• Use a Spiritual Vessel on a Soul Campfire while crouching to revive a soul.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• Consume Empty Hearts to gain 1 extra life and +2 max health.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• Bosses can drop Empty Hearts as rewards.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("§7• If all players die, a world debuff is applied until it is cleared.").withStyle(ChatFormatting.GRAY));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.literal("RPGCore survival guide").withStyle(ChatFormatting.GOLD));
        builder.accept(Component.literal("Lives, ghosts, souls, bosses and world debuffs").withStyle(ChatFormatting.GRAY));
    }
}

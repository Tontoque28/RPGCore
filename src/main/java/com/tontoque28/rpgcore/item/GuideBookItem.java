package com.tontoque28.rpgcore.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    private static void say(PlayerEntity player, String text, TextFormatting color) {
        player.sendMessage(new StringTextComponent(text).withStyle(color), Util.NIL_UUID);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return ActionResult.sidedSuccess(stack, true);
        }

        say(player, "§6[RPGCore] Guide", TextFormatting.GOLD);
        say(player, "§7• Every death removes 1 life.", TextFormatting.GRAY);
        say(player, "§7• At 0 lives you become a ghost.", TextFormatting.GRAY);
        say(player, "§7• Ghosts float, ignore collisions, and mobs do not target them.", TextFormatting.GRAY);
        say(player, "§7• Use a Spiritual Vessel on a Soul Campfire while crouching to revive a soul.", TextFormatting.GRAY);
        say(player, "§7• Consume Empty Hearts to gain 1 extra life and +2 max health.", TextFormatting.GRAY);
        say(player, "§7• Bosses can drop Empty Hearts as rewards.", TextFormatting.GRAY);
        say(player, "§7• If all players die, a world debuff is applied until it is cleared.", TextFormatting.GRAY);

        return ActionResult.sidedSuccess(stack, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltipComponents, ITooltipFlag isAdvanced) {
        tooltipComponents.add(new StringTextComponent("RPGCore survival guide").withStyle(TextFormatting.GOLD));
        tooltipComponents.add(new StringTextComponent("Lives, ghosts, souls, bosses and world debuffs").withStyle(TextFormatting.GRAY));
    }
}

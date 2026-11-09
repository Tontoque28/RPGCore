package com.tontoque28.rpgcore.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class GuideBookItem extends Item {

    public GuideBookItem() {
        this.setMaxStackSize(1);
    }

    private static void say(EntityPlayer player, String text, TextFormatting color) {
        TextComponentString component = new TextComponentString(text);
        component.getStyle().setColor(color);
        player.sendMessage(component);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World level, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (level.isRemote) {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        say(player, "§6[RPGCore] Guide", TextFormatting.GOLD);
        say(player, "§7• Every death removes 1 life.", TextFormatting.GRAY);
        say(player, "§7• At 0 lives you become a ghost.", TextFormatting.GRAY);
        say(player, "§7• Ghosts float, ignore collisions, and mobs do not target them.", TextFormatting.GRAY);
        say(player, "§7• Use a Spiritual Vessel on Soul Sand while crouching to attract a soul.", TextFormatting.GRAY);
        say(player, "§7• Consume Empty Hearts to gain 1 extra life and +2 max health.", TextFormatting.GRAY);
        say(player, "§7• Bosses can drop Empty Hearts as rewards.", TextFormatting.GRAY);
        say(player, "§7• If all players die, a world debuff is applied until it is cleared.", TextFormatting.GRAY);

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World level, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GOLD + "RPGCore survival guide");
        tooltip.add(TextFormatting.GRAY + "Lives, ghosts, souls, bosses and world debuffs");
    }
}

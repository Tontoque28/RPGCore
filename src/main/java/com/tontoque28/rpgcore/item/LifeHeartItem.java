package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LifeHeartItem extends Item {

    public LifeHeartItem() {
        this.setMaxStackSize(16);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    private static void consume(EntityPlayer player, PlayerLives lives, ItemStack stack) {
        lives.addLives(1);
        GhostDeathHandler.syncMaxHealthServer(player);
        GhostDeathHandler.sendLivesSync(player, lives.getLives());
        ITextComponent message = new TextComponentString(player.getName() + " ha consumido 1 corazón");
        message.getStyle().setColor(TextFormatting.GOLD);
        for (EntityPlayer recipient : new ArrayList<EntityPlayer>(player.world.playerEntities)) {
            recipient.sendMessage(message);
        }
        stack.shrink(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World level, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        PlayerLives lives = PlayerLivesProvider.of(player);
        if (lives != null && lives.getLives() <= 0) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (level.isRemote) {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        if (lives != null) {
            consume(player, lives, stack);
            level.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, player.getSoundCategory(), 0.8F, 1.3F);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.EAT;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World level, EntityLivingBase entity) {
        if (level.isRemote || !(entity instanceof EntityPlayer)) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;

        PlayerLives lives = PlayerLivesProvider.of(player);
        if (lives != null) {
            if (lives.getLives() <= 0) {
                player.sendMessage(new TextComponentTranslation("message.rpgcore.life_heart_ghost"));
                return stack;
            }
            consume(player, lives, stack);
        }
        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World level, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.RED + I18n.format("tooltip.rpgcore.life_heart_add"));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.rpgcore.life_heart_no_heal"));
    }
}

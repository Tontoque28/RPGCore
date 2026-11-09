package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SpiritualVesselItem extends Item {

    public SpiritualVesselItem() {
        this.setMaxStackSize(1);
    }

    public static boolean isBound(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasUniqueId("SoulUUID");
    }

    public static void bindSoul(ItemStack stack, UUID uuid, String name) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setUniqueId("SoulUUID", uuid);
        if (name != null) {
            stack.getTagCompound().setString("SoulName", name);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isBound(stack) || super.hasEffect(stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World level, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, level, tooltip, flag);

        if (isBound(stack) && stack.getTagCompound().hasKey("SoulName")) {
            String soulName = stack.getTagCompound().getString("SoulName");
            tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.rpgcore.soul_bound", soulName));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.rpgcore.soul_empty"));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World level, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (!level.isRemote && player.isSneaking()) {
            if (level.getBlockState(pos).getBlock() == Blocks.SOUL_SAND) {

                if (!isBound(stack)) {
                    for (EntityPlayerMP serverPlayer : level.getMinecraftServer().getPlayerList().getPlayers()) {
                        if (GhostRestrictionsHandler.isGhost(serverPlayer)) {
                            bindSoul(stack, serverPlayer.getUniqueID(), serverPlayer.getName());

                            player.sendMessage(new TextComponentTranslation("message.rpgcore.soul_attracted", serverPlayer.getName()));
                            level.playSound(null, pos, SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);

                            return EnumActionResult.SUCCESS;
                        }
                    }
                    player.sendMessage(new TextComponentTranslation("message.rpgcore.no_souls"));
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return super.onItemUse(player, level, pos, hand, facing, hitX, hitY, hitZ);
    }
}

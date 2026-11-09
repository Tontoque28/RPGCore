package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SpiritualVesselItem extends Item {

    public SpiritualVesselItem(Properties properties) {
        super(properties);
    }

    public static boolean isBound(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("SoulUUID");
    }

    public static void bindSoul(ItemStack stack, UUID uuid, String name) {
        stack.getOrCreateTag().putUUID("SoulUUID", uuid);
        if (name != null) {
            stack.getOrCreateTag().putString("SoulName", name);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isBound(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltipComponents, ITooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (isBound(stack) && stack.getTag() != null && stack.getTag().contains("SoulName")) {
            String soulName = stack.getTag().getString("SoulName");
            tooltipComponents.add(new TranslationTextComponent("tooltip.rpgcore.soul_bound", soulName).withStyle(TextFormatting.AQUA));
        } else {
            tooltipComponents.add(new TranslationTextComponent("tooltip.rpgcore.soul_empty").withStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && player != null && player.isCrouching()) {
            if (level.getBlockState(pos).is(Blocks.SOUL_CAMPFIRE)) {

                if (!isBound(stack)) {
                    for (ServerPlayerEntity serverPlayer : level.getServer().getPlayerList().getPlayers()) {
                        if (GhostRestrictionsHandler.isGhost(serverPlayer)) {
                            bindSoul(stack, serverPlayer.getUUID(), serverPlayer.getName().getString());

                            player.sendMessage(new TranslationTextComponent("message.rpgcore.soul_attracted", serverPlayer.getName().getString()), Util.NIL_UUID);
                            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);

                            return ActionResultType.SUCCESS;
                        }
                    }
                    player.sendMessage(new TranslationTextComponent("message.rpgcore.no_souls"), Util.NIL_UUID);
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }
}

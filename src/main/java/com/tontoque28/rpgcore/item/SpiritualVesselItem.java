package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (isBound(stack) && stack.getTag() != null && stack.getTag().contains("SoulName")) {
            String soulName = stack.getTag().getString("SoulName");
            tooltipComponents.add(new TranslatableComponent("tooltip.rpgcore.soul_bound", soulName).withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.add(new TranslatableComponent("tooltip.rpgcore.soul_empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && player != null && player.isCrouching()) {
            if (level.getBlockState(pos).is(Blocks.SOUL_CAMPFIRE)) {

                if (!isBound(stack)) {
                    for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
                        if (GhostRestrictionsHandler.isGhost(serverPlayer)) {
                            bindSoul(stack, serverPlayer.getUUID(), serverPlayer.getName().getString());

                            player.sendMessage(new TranslatableComponent("message.rpgcore.soul_attracted", serverPlayer.getName().getString()), Util.NIL_UUID);
                            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);

                            return InteractionResult.SUCCESS;
                        }
                    }
                    player.sendMessage(new TranslatableComponent("message.rpgcore.no_souls"), Util.NIL_UUID);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }
}

package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;
import java.util.UUID;

public class SpiritualVesselItem extends Item {

    public SpiritualVesselItem(Properties properties) {
        super(properties);
    }

    public static boolean isBound(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains("SoulUUID");
    }

    public static UUID getSoulUuid(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag().read("SoulUUID", UUIDUtil.CODEC).orElse(null);
    }

    public static void bindSoul(ItemStack stack, UUID uuid, String name) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.store("SoulUUID", UUIDUtil.CODEC, uuid);
            if (name != null) {
                tag.putString("SoulName", name);
            }
        });
    }

    public static void unbindSoul(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return;
        }
        CompoundTag tag = data.copyTag();
        tag.remove("SoulUUID");
        tag.remove("SoulName");
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isBound(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (isBound(stack) && data != null && data.copyTag().contains("SoulName")) {
            String soulName = data.copyTag().getStringOr("SoulName", "");
            builder.accept(Component.translatable("tooltip.rpgcore.soul_bound", soulName).withStyle(ChatFormatting.AQUA));
        } else {
            builder.accept(Component.translatable("tooltip.rpgcore.soul_empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide() && player != null && player.isCrouching()) {
            if (level.getBlockState(pos).is(Blocks.SOUL_CAMPFIRE)) {

                if (!isBound(stack)) {
                    for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
                        if (GhostRestrictionsHandler.isGhost(serverPlayer)) {
                            bindSoul(stack, serverPlayer.getUUID(), serverPlayer.getName().getString());

                            player.sendSystemMessage(Component.translatable("message.rpgcore.soul_attracted", serverPlayer.getName().getString()));
                            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

                            return InteractionResult.SUCCESS;
                        }
                    }
                    player.sendSystemMessage(Component.translatable("message.rpgcore.no_souls"));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }
}

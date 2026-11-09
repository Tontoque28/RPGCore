package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemUseAnimation;


import java.util.List;

public class LifeHeartItem extends Item {

    public LifeHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(lives -> lives.getLives() <= 0)
                .orElse(false)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() <= 0) {
                player.displayClientMessage(Component.translatable("message.rpgcore.life_heart_ghost"), false);
                return;
            }

            lives.addLives(1);
            GhostDeathHandler.syncMaxHealthServer(player);
            GhostDeathHandler.sendLivesSync(player, lives.getLives());
            Component message = Component.literal(player.getName().getString() + " ha consumido 1 corazón").withStyle(ChatFormatting.GOLD);
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (net.minecraft.server.level.ServerPlayer recipient : serverLevel.players()) {
                    recipient.sendSystemMessage(message, false);
                }
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.8F, 1.3F);
            stack.shrink(1);
        });

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return stack;
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() <= 0) {
                player.displayClientMessage(Component.translatable("message.rpgcore.life_heart_ghost"), false);
                return;
            }

            lives.addLives(1);
            GhostDeathHandler.syncMaxHealthServer(player);
            GhostDeathHandler.sendLivesSync(player, lives.getLives());
            Component message = Component.literal(player.getName().getString() + " ha consumido 1 corazón").withStyle(ChatFormatting.GOLD);
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (net.minecraft.server.level.ServerPlayer recipient : serverLevel.players()) {
                    recipient.sendSystemMessage(message, false);
                }
            }
            stack.shrink(1);
        });
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.rpgcore.life_heart_add").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.translatable("tooltip.rpgcore.life_heart_no_heal").withStyle(ChatFormatting.GRAY));
    }
}

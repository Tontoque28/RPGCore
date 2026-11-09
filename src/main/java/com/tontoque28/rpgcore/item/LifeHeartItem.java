package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class LifeHeartItem extends Item {

    public LifeHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(lives -> lives.getLives() <= 0)
                .orElse(false)) {
            return ActionResult.fail(stack);
        }

        if (level.isClientSide()) {
            return ActionResult.consume(stack);
        }

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() <= 0) {
                player.sendMessage(new TranslationTextComponent("message.rpgcore.life_heart_ghost"), Util.NIL_UUID);
                return;
            }

            lives.addLives(1);
            GhostDeathHandler.syncMaxHealthServer(player);
            GhostDeathHandler.sendLivesSync(player, lives.getLives());
            ITextComponent message = new StringTextComponent(player.getName().getString() + " ha consumido 1 corazón").withStyle(TextFormatting.GOLD);
            if (player.level instanceof ServerWorld) {
                for (ServerPlayerEntity recipient : ((ServerWorld) player.level).players()) {
                    recipient.sendMessage(message, Util.NIL_UUID);
                }
            }
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.8F, 1.3F);
            stack.shrink(1);
        });

        return ActionResult.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof PlayerEntity)) {
            return stack;
        }
        PlayerEntity player = (PlayerEntity) entity;

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() <= 0) {
                player.sendMessage(new TranslationTextComponent("message.rpgcore.life_heart_ghost"), Util.NIL_UUID);
                return;
            }

            lives.addLives(1);
            GhostDeathHandler.syncMaxHealthServer(player);
            GhostDeathHandler.sendLivesSync(player, lives.getLives());
            ITextComponent message = new StringTextComponent(player.getName().getString() + " ha consumido 1 corazón").withStyle(TextFormatting.GOLD);
            if (player.level instanceof ServerWorld) {
                for (ServerPlayerEntity recipient : ((ServerWorld) player.level).players()) {
                    recipient.sendMessage(message, Util.NIL_UUID);
                }
            }
            stack.shrink(1);
        });
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltipComponents, ITooltipFlag isAdvanced) {
        tooltipComponents.add(new TranslationTextComponent("tooltip.rpgcore.life_heart_add").withStyle(TextFormatting.RED));
        tooltipComponents.add(new TranslationTextComponent("tooltip.rpgcore.life_heart_no_heal").withStyle(TextFormatting.GRAY));
    }
}

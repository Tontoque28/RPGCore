package com.tontoque28.rpgcore.item;

import com.tontoque28.rpgcore.capability.PlayerLives;
import com.tontoque28.rpgcore.events.GhostDeathHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LifeHeartItem extends Item {

    public LifeHeartItem(Properties properties) {
        super(properties);
    }

    private static void consume(Player player, ItemStack stack, boolean playSound) {
        if (PlayerLives.getLives(player) <= 0) {
            player.sendMessage(new TranslatableComponent("message.rpgcore.life_heart_ghost"), Util.NIL_UUID);
            return;
        }

        PlayerLives.addLives(player, 1);
        GhostDeathHandler.syncMaxHealthServer(player);
        GhostDeathHandler.sendLivesSync(player, PlayerLives.getLives(player));
        Component message = new TextComponent(player.getName().getString() + " ha consumido 1 corazón").withStyle(ChatFormatting.GOLD);
        if (player.level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) player.level;
            for (ServerPlayer recipient : serverLevel.players()) {
                recipient.sendMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
            }
        }
        if (playSound) {
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.8F, 1.3F);
        }
        stack.shrink(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (PlayerLives.getLives(player) <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }

        consume(player, stack, true);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player)) {
            return stack;
        }

        consume((Player) entity, stack, false);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(new TranslatableComponent("tooltip.rpgcore.life_heart_add").withStyle(ChatFormatting.RED));
        tooltipComponents.add(new TranslatableComponent("tooltip.rpgcore.life_heart_no_heal").withStyle(ChatFormatting.GRAY));
    }
}

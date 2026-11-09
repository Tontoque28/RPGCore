package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.lang.reflect.Field;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Dist.CLIENT)
public class GhostRenderHandler {

    private static void disableGhostShadow(RenderPlayerEvent.Pre event) {
        try {
            Field shadowRadiusField = EntityRenderer.class.getDeclaredField("shadowRadius");
            shadowRadiusField.setAccessible(true);
            shadowRadiusField.setFloat(event.getRenderer(), 0.0F);

            Field shadowStrengthField = LivingEntityRenderer.class.getDeclaredField("shadowStrength");
            shadowStrengthField.setAccessible(true);
            shadowStrengthField.setFloat(event.getRenderer(), 0.0F);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        if (!GhostRestrictionsHandler.isGhost(player)) {
            return;
        }

        event.setCanceled(true);
        disableGhostShadow(event);

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = false;
        model.leftArm.visible = false;
        model.rightArm.visible = false;
        model.leftLeg.visible = false;
        model.rightLeg.visible = false;

        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        poseStack.pushPose();

        float ageInTicks = player.tickCount + partialTick;
        float bobbing = Mth.sin(ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(headYaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        ResourceLocation skinTexture = clientPlayer.getSkin().texture();

        model.head.visible = true;
        model.hat.visible = true;

        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.head.zRot = 0.0F;
        model.head.x = 0.0F;
        model.head.y = 0.0F;
        model.head.z = 0.0F;
        model.hat.copyFrom(model.head);

        VertexConsumer vertexConsumer = event.getMultiBufferSource()
                .getBuffer(RenderType.entityTranslucent(skinTexture));

        int light = LightTexture.FULL_BRIGHT;
        float red = 0.80F;
        float green = 0.95F;
        float blue = 1.0F;
        float alpha = 0.55F;

        int color = FastColor.ARGB32.color((int) (alpha * 255.0F), (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));

        model.head.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
        model.hat.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            event.setCanceled(true);
        }
    }
}
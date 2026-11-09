package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Dist.CLIENT)
public class GhostRenderHandler {

    private static void disableGhostShadow(RenderPlayerEvent.Pre event) {
        try {
            Field shadowRadiusField = EntityRenderer.class.getDeclaredField("shadowRadius");
            shadowRadiusField.setAccessible(true);
            shadowRadiusField.setFloat(event.getRenderer(), 0.0F);

            Field shadowStrengthField = LivingRenderer.class.getDeclaredField("shadowStrength");
            shadowStrengthField.setAccessible(true);
            shadowStrengthField.setFloat(event.getRenderer(), 0.0F);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();

        if (!GhostRestrictionsHandler.isGhost(player)) {
            return;
        }

        event.setCanceled(true);
        disableGhostShadow(event);

        PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getModel();
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = false;
        model.leftArm.visible = false;
        model.rightArm.visible = false;
        model.leftLeg.visible = false;
        model.rightLeg.visible = false;

        if (!(player instanceof AbstractClientPlayerEntity)) {
            return;
        }
        AbstractClientPlayerEntity clientPlayer = (AbstractClientPlayerEntity) player;

        MatrixStack poseStack = event.getMatrixStack();
        float partialTick = event.getPartialRenderTick();

        poseStack.pushPose();

        float ageInTicks = player.tickCount + partialTick;
        float bobbing = MathHelper.sin(ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = MathHelper.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float headPitch = MathHelper.lerp(partialTick, player.xRotO, player.xRot);

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(headYaw - 180.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(headPitch));

        ResourceLocation skinTexture = clientPlayer.getSkinTextureLocation();

        model.head.visible = true;
        model.hat.visible = true;

        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.head.zRot = 0.0F;
        model.head.x = 0.0F;
        model.head.y = 0.0F;
        model.head.z = 0.0F;
        model.hat.copyFrom(model.head);

        IVertexBuilder vertexConsumer = event.getBuffers()
                .getBuffer(RenderType.entityTranslucent(skinTexture));

        int light = LightTexture.pack(15, 15);
        float red = 0.80F;
        float green = 0.95F;
        float blue = 1.0F;
        float alpha = 0.55F;

        model.head.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.hat.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            event.setCanceled(true);
        }
    }
}

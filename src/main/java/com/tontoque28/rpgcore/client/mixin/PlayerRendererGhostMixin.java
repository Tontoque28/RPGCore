package com.tontoque28.rpgcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererGhostMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rpgcore$renderGhost(AbstractClientPlayer clientPlayer, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!GhostRestrictionsHandler.isGhost(clientPlayer)) {
            return;
        }

        ci.cancel();

        PlayerRenderer renderer = (PlayerRenderer) (Object) this;
        ((EntityRendererAccessor) renderer).rpgcore$setShadowRadius(0.0F);
        ((EntityRendererAccessor) renderer).rpgcore$setShadowStrength(0.0F);

        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = false;
        model.leftArm.visible = false;
        model.rightArm.visible = false;
        model.leftLeg.visible = false;
        model.rightLeg.visible = false;

        poseStack.pushPose();

        float ageInTicks = clientPlayer.tickCount + partialTick;
        float bobbing = Mth.sin(ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = Mth.rotLerp(partialTick, clientPlayer.yHeadRotO, clientPlayer.yHeadRot);
        float headPitch = Mth.lerp(partialTick, clientPlayer.xRotO, clientPlayer.xRot);

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

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(skinTexture));

        int light = LightTexture.pack(15, 15);
        float red = 0.80F;
        float green = 0.95F;
        float blue = 1.0F;
        float alpha = 0.55F;

        model.head.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.hat.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);

        poseStack.popPose();
    }
}

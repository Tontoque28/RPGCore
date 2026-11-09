package com.tontoque28.rpgcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerRendererGhostMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rpgcore$renderGhost(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!((Object) this instanceof PlayerRenderer renderer) || !(state instanceof PlayerRenderState playerState)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(playerState.id);
        EntityRendererAccessor accessor = (EntityRendererAccessor) renderer;
        if (!(entity instanceof Player clientPlayer) || !GhostRestrictionsHandler.isGhost(clientPlayer)) {
            accessor.rpgcore$setShadowRadius(0.5F);
            accessor.rpgcore$setShadowStrength(1.0F);
            return;
        }

        ci.cancel();

        accessor.rpgcore$setShadowRadius(0.0F);
        accessor.rpgcore$setShadowStrength(0.0F);

        PlayerModel model = renderer.getModel();
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.body.visible = false;
        model.leftArm.visible = false;
        model.rightArm.visible = false;
        model.leftLeg.visible = false;
        model.rightLeg.visible = false;

        poseStack.pushPose();

        float ageInTicks = playerState.ageInTicks;
        float bobbing = Mth.sin(ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = playerState.bodyRot + playerState.yRot;
        float headPitch = playerState.xRot;

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(headYaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        ResourceLocation skinTexture = playerState.skin.texture();

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

        int light = LightTexture.FULL_BRIGHT;
        int color = ARGB.color((int) (0.55F * 255.0F), (int) (0.80F * 255.0F), (int) (0.95F * 255.0F), 255);

        model.head.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
        model.hat.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }
}

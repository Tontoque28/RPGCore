package com.tontoque28.rpgcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tontoque28.rpgcore.client.GhostHeadModel;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerRendererGhostMixin {

    @Unique
    private static GhostHeadModel rpgcore$ghostModel;

    @Unique
    private static EntityModelSet rpgcore$ghostModelSet;

    @Unique
    private static GhostHeadModel rpgcore$getGhostModel(Minecraft minecraft) {
        EntityModelSet modelSet = minecraft.getEntityModels();
        if (rpgcore$ghostModel == null || rpgcore$ghostModelSet != modelSet) {
            rpgcore$ghostModel = new GhostHeadModel(modelSet.bakeLayer(GhostHeadModel.LAYER_LOCATION));
            rpgcore$ghostModelSet = modelSet;
        }
        return rpgcore$ghostModel;
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rpgcore$renderGhost(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (!((Object) this instanceof AvatarRenderer<?>) || !(state instanceof AvatarRenderState avatarState)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(avatarState.id);
        if (!(entity instanceof Player clientPlayer) || !GhostRestrictionsHandler.isGhost(clientPlayer)) {
            return;
        }

        ci.cancel();

        poseStack.pushPose();

        float bobbing = Mth.sin(avatarState.ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = avatarState.bodyRot + avatarState.yRot;
        float headPitch = avatarState.xRot;

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.rotateDegrees(Axis.YP, headYaw - 180.0F);
        poseStack.rotateDegrees(Axis.XP, headPitch);

        Identifier skinTexture = avatarState.skin.body().texturePath();
        int color = ARGB.color((int) (0.55F * 255.0F), (int) (0.80F * 255.0F), (int) (0.95F * 255.0F), 255);

        collector.submitModel(
                rpgcore$getGhostModel(minecraft),
                avatarState,
                poseStack,
                RenderTypes.entityTranslucent(skinTexture),
                LightCoordsUtil.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                color,
                null,
                avatarState.outlineColor
        );

        poseStack.popPose();
    }
}

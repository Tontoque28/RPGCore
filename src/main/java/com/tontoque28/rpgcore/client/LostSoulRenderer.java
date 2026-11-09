package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;

public class LostSoulRenderer extends EntityRenderer<LostSoulEntity, LostSoulRenderState> {

    private static final int CORE_COLOR = argb(0.95F, 0.85F, 0.98F, 1.0F);
    private static final int AURA_COLOR = argb(0.40F, 0.25F, 0.80F, 1.0F);

    private final LostSoulModel model;
    private final LostSoulAuraModel auraModel;

    public LostSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LostSoulModel(context.bakeLayer(LostSoulModel.LAYER_LOCATION));
        this.auraModel = new LostSoulAuraModel(context.bakeLayer(LostSoulAuraModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    private static int argb(float alpha, float red, float green, float blue) {
        return ARGB.color((int) (alpha * 255.0F), (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
    }

    @Override
    public LostSoulRenderState createRenderState() {
        return new LostSoulRenderState();
    }

    @Override
    public void extractRenderState(LostSoulEntity entity, LostSoulRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        String ownerName = entity.getPlayerName();
        if (ownerName != null && !ownerName.isEmpty() && state.distanceToCameraSq < 64.0D) {
            state.nameTag = Component.literal("Alma de " + ownerName).withStyle(ChatFormatting.AQUA);
            Vec3 attachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
            state.nameTagAttachment = attachment != null ? attachment : new Vec3(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
        } else {
            state.nameTag = null;
        }
    }

    @Override
    public void submit(LostSoulRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        float pulse = 0.75F + Mth.sin(state.ageInTicks * 0.15F) * 0.06F;

        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.scale(-pulse, -pulse, pulse);

        int fullBright = LightCoordsUtil.FULL_BRIGHT;
        RenderType renderType = RenderTypes.entityTranslucentEmissive(BeaconRenderer.BEAM_LOCATION);

        collector.submitModel(this.model, state, poseStack, renderType, fullBright, OverlayTexture.NO_OVERLAY, CORE_COLOR, null, state.outlineColor);
        collector.submitModel(this.auraModel, state, poseStack, renderType, fullBright, OverlayTexture.NO_OVERLAY, AURA_COLOR, null, state.outlineColor);

        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }
}

package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;

public class LostSoulRenderer extends EntityRenderer<LostSoulEntity, LostSoulRenderState> {

    private static final Identifier SOUL_TEXTURE = Identifier.fromNamespaceAndPath("minecraft",
            "textures/entity/beacon/beacon_beam.png");
    private static final int CORE_COLOR = ARGB.color(242, 216, 249, 255);
    private static final int AURA_COLOR = ARGB.color(102, 63, 204, 255);

    private final LostSoulModel coreModel;
    private final LostSoulAuraModel auraModel;

    public LostSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.coreModel = new LostSoulModel(context.bakeLayer(LostSoulModel.LAYER_LOCATION));
        this.auraModel = new LostSoulAuraModel(context.bakeLayer(LostSoulAuraModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public LostSoulRenderState createRenderState() {
        return new LostSoulRenderState();
    }

    @Override
    public void extractRenderState(LostSoulEntity entity, LostSoulRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ownerName = entity.getPlayerName();
        Vec3 attachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
        state.ownerNameAttachment = attachment != null ? attachment : new Vec3(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
    }

    @Override
    public void submit(LostSoulRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        float pulse = 0.75F + Mth.sin(state.ageInTicks * 0.15F) * 0.06F;

        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.scale(-pulse, -pulse, pulse);

        RenderType renderType = RenderTypes.entityTranslucentEmissive(SOUL_TEXTURE);
        int fullBright = LightCoordsUtil.FULL_BRIGHT;

        collector.submitModel(this.coreModel, state, poseStack, renderType, fullBright, OverlayTexture.NO_OVERLAY, CORE_COLOR, null, state.outlineColor);
        collector.submitModel(this.auraModel, state, poseStack, renderType, fullBright, OverlayTexture.NO_OVERLAY, AURA_COLOR, null, state.outlineColor);

        poseStack.popPose();

        String ownerName = state.ownerName;
        if (ownerName != null && !ownerName.isEmpty() && state.distanceToCameraSq < 64.0D) {
            collector.submitNameTag(
                    poseStack,
                    state.ownerNameAttachment,
                    0,
                    Component.literal("Alma de " + ownerName).withStyle(ChatFormatting.AQUA),
                    !state.isDiscrete,
                    fullBright,
                    camera);
        }

        super.submit(state, poseStack, collector, camera);
    }
}

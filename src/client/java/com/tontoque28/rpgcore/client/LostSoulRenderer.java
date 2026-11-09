package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LostSoulRenderer extends EntityRenderer<LostSoulEntity, LostSoulRenderState> {

    private static final ResourceLocation SOUL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/entity/beacon_beam.png");
    private final LostSoulModel model;

    public LostSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LostSoulModel(context.bakeLayer(LostSoulModel.LAYER_LOCATION));
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
    }

    @Override
    public void render(LostSoulRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = state.ageInTicks;
        float pulse = 0.75F + Mth.sin(ageInTicks * 0.15F) * 0.06F;

        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.scale(-pulse, -pulse, pulse);

        this.model.setupAnim(state);

        int fullBright = LightTexture.FULL_BRIGHT;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(SOUL_TEXTURE));

        this.model.renderCore(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        this.model.renderAura(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        String ownerName = state.ownerName;
        if (ownerName != null && !ownerName.isEmpty() && state.distanceToCameraSq < 64.0D) {
            this.renderNameTag(
                    state,
                    Component.literal("Alma de " + ownerName).withStyle(ChatFormatting.AQUA),
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT);
        }

        super.render(state, poseStack, buffer, packedLight);
    }
}

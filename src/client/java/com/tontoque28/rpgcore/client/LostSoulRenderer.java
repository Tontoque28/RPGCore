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

public class LostSoulRenderer extends EntityRenderer<LostSoulEntity> {

    private static final ResourceLocation SOUL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/entity/beacon_beam.png");
    private final LostSoulModel model;

    public LostSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LostSoulModel(context.bakeLayer(LostSoulModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LostSoulEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float pulse = 0.75F + Mth.sin(ageInTicks * 0.15F) * 0.06F;

        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.scale(-pulse, -pulse, pulse);

        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        int fullBright = LightTexture.FULL_BRIGHT;
        VertexConsumer vertexConsumer = buffer
                .getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(entity)));

        this.model.renderCore(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        this.model.renderAura(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        String ownerName = entity.getPlayerName();
        if (ownerName != null && !ownerName.isEmpty() && this.entityRenderDispatcher.distanceToSqr(entity) < 64.0D) {
            this.renderNameTag(
                    entity,
                    Component.literal("Alma de " + ownerName).withStyle(ChatFormatting.AQUA),
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT,
                    partialTicks);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LostSoulEntity entity) {
        return SOUL_TEXTURE;
    }
}
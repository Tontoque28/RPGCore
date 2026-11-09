package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class LostSoulRenderer extends EntityRenderer<LostSoulEntity> {

    private static final ResourceLocation SOUL_TEXTURE = new ResourceLocation("minecraft",
            "textures/entity/beacon_beam.png");
    private final LostSoulModel model;

    public LostSoulRenderer(EntityRendererManager context) {
        super(context);
        this.model = new LostSoulModel();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LostSoulEntity entity, float entityYaw, float partialTicks, MatrixStack poseStack,
            IRenderTypeBuffer buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float pulse = 0.75F + MathHelper.sin(ageInTicks * 0.15F) * 0.06F;

        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.scale(-pulse, -pulse, pulse);

        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        int fullBright = LightTexture.pack(15, 15);
        IVertexBuilder vertexConsumer = buffer
                .getBuffer(RenderType.entityTranslucentCull(getTextureLocation(entity)));

        this.model.renderCore(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        this.model.renderAura(poseStack, vertexConsumer, fullBright, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        String ownerName = entity.getPlayerName();
        if (ownerName != null && !ownerName.isEmpty() && this.entityRenderDispatcher.distanceToSqr(entity) < 64.0D) {
            this.renderNameTag(
                    entity,
                    new StringTextComponent("Alma de " + ownerName).withStyle(TextFormatting.AQUA),
                    poseStack,
                    buffer,
                    LightTexture.pack(15, 15));
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LostSoulEntity entity) {
        return SOUL_TEXTURE;
    }
}

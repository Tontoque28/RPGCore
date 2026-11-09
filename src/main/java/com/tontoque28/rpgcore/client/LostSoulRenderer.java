package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class LostSoulRenderer extends Render<LostSoulEntity> {

    private static final ResourceLocation SOUL_TEXTURE = new ResourceLocation("minecraft", "textures/entity/beacon_beam.png");
    private final LostSoulModel model;

    public LostSoulRenderer(RenderManager manager) {
        super(manager);
        this.model = new LostSoulModel();
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(LostSoulEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float ageInTicks = entity.ticksExisted + partialTicks;
        float pulse = 0.75F + MathHelper.sin(ageInTicks * 0.15F) * 0.06F;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 1.35F, (float) z);
        GlStateManager.scale(-pulse, -pulse, pulse);

        this.model.setupAnim(ageInTicks);
        this.bindEntityTexture(entity);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        this.model.renderCore(0.0625F);
        this.model.renderAura(0.0625F);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        String ownerName = entity.getPlayerName();
        if (ownerName != null && !ownerName.isEmpty() && entity.getDistanceSq(this.renderManager.renderViewEntity) < 64.0D) {
            this.renderLivingLabel(entity, TextFormatting.AQUA + "Alma de " + ownerName, x, y, z, 64);
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(LostSoulEntity entity) {
        return SOUL_TEXTURE;
    }
}

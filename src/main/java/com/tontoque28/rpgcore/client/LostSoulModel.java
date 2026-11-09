package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class LostSoulModel extends EntityModel<LostSoulEntity> {

    private final ModelRenderer root;
    private final ModelRenderer core;
    private final ModelRenderer outerShell;
    private final ModelRenderer tail;
    private final ModelRenderer orbitRing;

    public LostSoulModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        this.root = new ModelRenderer(this);
        this.root.setPos(0.0F, 16.0F, 0.0F);

        this.core = new ModelRenderer(this, 0, 0);
        this.core.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        this.root.addChild(this.core);

        this.outerShell = new ModelRenderer(this, 0, 0);
        this.outerShell.addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F);
        this.root.addChild(this.outerShell);

        this.tail = new ModelRenderer(this, 0, 0);
        this.tail.addBox(-2.0F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F);
        this.root.addChild(this.tail);

        this.orbitRing = new ModelRenderer(this);
        this.orbitRing.setPos(0.0F, 0.0F, 0.0F);
        ModelRenderer wisp1 = new ModelRenderer(this, 0, 0);
        wisp1.addBox(11.0F, -1.5F, -1.0F, 2.5F, 2.5F, 2.5F);
        ModelRenderer wisp2 = new ModelRenderer(this, 0, 0);
        wisp2.addBox(-6.5F, -2.5F, 9.5F, 2.5F, 2.5F, 2.5F);
        ModelRenderer wisp3 = new ModelRenderer(this, 0, 0);
        wisp3.addBox(-6.5F, 1.5F, -11.5F, 2.5F, 2.5F, 2.5F);
        this.orbitRing.addChild(wisp1);
        this.orbitRing.addChild(wisp2);
        this.orbitRing.addChild(wisp3);
        this.root.addChild(this.orbitRing);
    }

    @Override
    public void setupAnim(LostSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.y = 16.0F + MathHelper.sin(ageInTicks * 0.1F) * 2.0F;

        this.core.yRot = ageInTicks * 0.06F;
        this.core.xRot = ageInTicks * 0.04F;

        this.outerShell.yRot = -ageInTicks * 0.035F;
        this.outerShell.zRot = MathHelper.sin(ageInTicks * 0.05F) * 0.15F;

        this.tail.xRot = MathHelper.cos(ageInTicks * 0.1F) * 0.25F;
        this.tail.zRot = MathHelper.sin(ageInTicks * 0.1F) * 0.25F;

        this.orbitRing.yRot = ageInTicks * 0.12F;
        this.orbitRing.zRot = MathHelper.sin(ageInTicks * 0.06F) * 0.3F;
    }

    public void renderCore(MatrixStack poseStack, IVertexBuilder buffer, int packedLight, int packedOverlay) {
        this.outerShell.visible = false;
        this.root.render(poseStack, buffer, packedLight, packedOverlay, 0.85F, 0.98F, 1.0F, 0.95F);
        this.outerShell.visible = true;
    }

    public void renderAura(MatrixStack poseStack, IVertexBuilder buffer, int packedLight, int packedOverlay) {
        this.core.visible = false;
        this.tail.visible = false;
        this.orbitRing.visible = false;
        this.root.render(poseStack, buffer, packedLight, packedOverlay, 0.25F, 0.80F, 1.0F, 0.40F);
        this.core.visible = true;
        this.tail.visible = true;
        this.orbitRing.visible = true;
    }

    @Override
    public void renderToBuffer(MatrixStack poseStack, IVertexBuilder vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

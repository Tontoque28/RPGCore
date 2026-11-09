package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tontoque28.rpgcore.entity.LostSoulEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class LostSoulModel extends EntityModel<LostSoulEntity> {

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart outerShell;
    private final ModelPart tail;
    private final ModelPart orbitRing;

    public LostSoulModel() {
        this.texWidth = 64;
        this.texHeight = 64;

        this.root = new ModelPart(this, 0, 0);
        this.root.setPos(0.0F, 16.0F, 0.0F);

        this.core = new ModelPart(this, 0, 0);
        this.core.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);

        this.outerShell = new ModelPart(this, 0, 0);
        this.outerShell.addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F);

        this.tail = new ModelPart(this, 0, 0);
        this.tail.addBox(-2.0F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F);

        this.orbitRing = new ModelPart(this, 0, 0);
        ModelPart wisp1 = new ModelPart(this, 0, 0);
        wisp1.addBox(11.0F, -1.5F, -1.0F, 2.5F, 2.5F, 2.5F);
        ModelPart wisp2 = new ModelPart(this, 0, 0);
        wisp2.addBox(-6.5F, -2.5F, 9.5F, 2.5F, 2.5F, 2.5F);
        ModelPart wisp3 = new ModelPart(this, 0, 0);
        wisp3.addBox(-6.5F, 1.5F, -11.5F, 2.5F, 2.5F, 2.5F);
        this.orbitRing.addChild(wisp1);
        this.orbitRing.addChild(wisp2);
        this.orbitRing.addChild(wisp3);

        this.root.addChild(this.core);
        this.root.addChild(this.outerShell);
        this.root.addChild(this.tail);
        this.root.addChild(this.orbitRing);
    }

    @Override
    public void setupAnim(LostSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.y = 16.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;

        this.core.yRot = ageInTicks * 0.06F;
        this.core.xRot = ageInTicks * 0.04F;

        this.outerShell.yRot = -ageInTicks * 0.035F;
        this.outerShell.zRot = Mth.sin(ageInTicks * 0.05F) * 0.15F;

        this.tail.xRot = Mth.cos(ageInTicks * 0.1F) * 0.25F;
        this.tail.zRot = Mth.sin(ageInTicks * 0.1F) * 0.25F;

        this.orbitRing.yRot = ageInTicks * 0.12F;
        this.orbitRing.zRot = Mth.sin(ageInTicks * 0.06F) * 0.3F;
    }

    public void renderCore(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.outerShell.visible = false;
        this.root.render(poseStack, buffer, packedLight, packedOverlay, 0.85F, 0.98F, 1.0F, 0.95F);
        this.outerShell.visible = true;
    }

    public void renderAura(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.core.visible = false;
        this.tail.visible = false;
        this.orbitRing.visible = false;
        this.root.render(poseStack, buffer, packedLight, packedOverlay, 0.25F, 0.80F, 1.0F, 0.40F);
        this.core.visible = true;
        this.tail.visible = true;
        this.orbitRing.visible = true;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

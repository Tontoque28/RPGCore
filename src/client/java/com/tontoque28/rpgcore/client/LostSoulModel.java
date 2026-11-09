package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tontoque28.rpgcore.RpgCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class LostSoulModel extends EntityModel<LostSoulRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(RpgCore.MOD_ID, "lost_soul"), "main"
    );

    private final ModelPart rootPart;
    private final ModelPart core;
    private final ModelPart outerShell;
    private final ModelPart tail;
    private final ModelPart orbitRing;

    public LostSoulModel(ModelPart root) {
        super(root);
        this.rootPart = root.getChild("root");
        this.core = this.rootPart.getChild("core");
        this.outerShell = this.rootPart.getChild("outer_shell");
        this.tail = this.rootPart.getChild("tail");
        this.orbitRing = this.rootPart.getChild("orbit_ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("outer_shell", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, 4.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.ZERO);

        PartDefinition orbit = root.addOrReplaceChild("orbit_ring", CubeListBuilder.create(), PartPose.ZERO);
        orbit.addOrReplaceChild("wisp_1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(11.0F, -1.5F, -1.0F, 2.5F, 2.5F, 2.5F), PartPose.ZERO);
        orbit.addOrReplaceChild("wisp_2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.5F, -2.5F, 9.5F, 2.5F, 2.5F, 2.5F), PartPose.ZERO);
        orbit.addOrReplaceChild("wisp_3", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.5F, 1.5F, -11.5F, 2.5F, 2.5F, 2.5F), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LostSoulRenderState state) {
        float ageInTicks = state.ageInTicks;

        this.rootPart.y = 16.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;

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
        this.rootPart.render(poseStack, buffer, packedLight, packedOverlay, argb(0.95F, 0.85F, 0.98F, 1.0F));
        this.outerShell.visible = true;
    }

    public void renderAura(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.core.visible = false;
        this.tail.visible = false;
        this.orbitRing.visible = false;
        this.rootPart.render(poseStack, buffer, packedLight, packedOverlay, argb(0.40F, 0.25F, 0.80F, 1.0F));
        this.core.visible = true;
        this.tail.visible = true;
        this.orbitRing.visible = true;
    }

    private static int argb(float alpha, float red, float green, float blue) {
        return ARGB.color((int) (alpha * 255.0F), (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
    }
}

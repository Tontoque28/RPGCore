package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.RpgCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class LostSoulModel extends EntityModel<LostSoulRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(RpgCore.MOD_ID, "lost_soul"), "main"
    );

    private final ModelPart soulRoot;
    private final ModelPart core;
    private final ModelPart tail;
    private final ModelPart orbitRing;

    public LostSoulModel(ModelPart root) {
        super(root);
        this.soulRoot = root.getChild("root");
        this.core = this.soulRoot.getChild("core");
        this.tail = this.soulRoot.getChild("tail");
        this.orbitRing = this.soulRoot.getChild("orbit_ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
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
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        this.soulRoot.y = 16.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;

        this.core.yRot = ageInTicks * 0.06F;
        this.core.xRot = ageInTicks * 0.04F;

        this.tail.xRot = Mth.cos(ageInTicks * 0.1F) * 0.25F;
        this.tail.zRot = Mth.sin(ageInTicks * 0.1F) * 0.25F;

        this.orbitRing.yRot = ageInTicks * 0.12F;
        this.orbitRing.zRot = Mth.sin(ageInTicks * 0.06F) * 0.3F;
    }
}

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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class LostSoulAuraModel extends EntityModel<LostSoulRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(RpgCore.MOD_ID, "lost_soul"), "aura"
    );

    private final ModelPart rootPart;
    private final ModelPart outerShell;

    public LostSoulAuraModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucentEmissive);
        this.rootPart = root.getChild("root");
        this.outerShell = this.rootPart.getChild("outer_shell");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("outer_shell", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LostSoulRenderState state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;

        this.rootPart.y = 16.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;

        this.outerShell.yRot = -ageInTicks * 0.035F;
        this.outerShell.zRot = Mth.sin(ageInTicks * 0.05F) * 0.15F;
    }
}

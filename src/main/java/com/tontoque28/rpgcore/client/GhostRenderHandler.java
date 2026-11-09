package com.tontoque28.rpgcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderAvatarEvent;
import net.minecraftforge.client.event.RenderHandEvent;

public class GhostRenderHandler {

    private static Model.Simple headModel;

    public static void register() {
        RenderAvatarEvent.Pre.BUS.addListener(GhostRenderHandler::onRenderAvatarPre);
        RenderHandEvent.BUS.addListener(GhostRenderHandler::onRenderHand);
    }

    private static Model.Simple headModel() {
        if (headModel == null) {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
            mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
            ModelPart root = LayerDefinition.create(mesh, 64, 64).bakeRoot();
            headModel = new Model.Simple(root, RenderTypes::entityTranslucent);
        }
        return headModel;
    }

    public static boolean onRenderAvatarPre(RenderAvatarEvent.Pre event) {
        AvatarRenderState state = event.getState();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(minecraft.level.getEntity(state.id) instanceof Player player)) {
            return false;
        }

        if (!GhostRestrictionsHandler.isGhost(player)) {
            return false;
        }

        state.shadowPieces.clear();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        float bobbing = Mth.sin(state.ageInTicks * 0.12F) * 0.10F;

        poseStack.translate(0.0D, 1.25D + bobbing, 0.0D);

        float headYaw = state.bodyRot + state.yRot;
        float headPitch = state.xRot;

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.rotateDegrees(Axis.YP, headYaw - 180.0F);
        poseStack.rotateDegrees(Axis.XP, headPitch);

        Identifier skinTexture = state.skin.body().texturePath();

        int color = ARGB.color((int) (0.55F * 255.0F), (int) (0.80F * 255.0F), (int) (0.95F * 255.0F), (int) (1.0F * 255.0F));

        Model.Simple model = headModel();
        event.getNodeCollector().submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entityTranslucent(skinTexture),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color, null, 0);

        poseStack.popPose();
        return true;
    }

    public static boolean onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        return player != null && GhostRestrictionsHandler.isGhost(player);
    }
}

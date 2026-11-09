package com.tontoque28.rpgcore.client;

import com.tontoque28.rpgcore.events.GhostRestrictionsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "rpgcore", value = Side.CLIENT)
public class GhostRenderHandler {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();

        if (!GhostRestrictionsHandler.isGhost(player)) {
            return;
        }

        event.setCanceled(true);

        RenderPlayer renderer = event.getRenderer();
        ModelPlayer model = renderer.getMainModel();

        ResourceLocation skinTexture = player instanceof AbstractClientPlayer
                ? ((AbstractClientPlayer) player).getLocationSkin()
                : DefaultPlayerSkin.getDefaultSkinLegacy();

        float partialTick = event.getPartialRenderTick();
        float ageInTicks = player.ticksExisted + partialTick;
        float bobbing = MathHelper.sin(ageInTicks * 0.12F) * 0.10F;

        float headYaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTick;
        float headPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTick;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) event.getX(), (float) event.getY() + 1.25F + bobbing, (float) event.getZ());
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.rotate(headYaw - 180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(headPitch, 1.0F, 0.0F, 0.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(skinTexture);

        model.bipedHead.rotateAngleX = 0.0F;
        model.bipedHead.rotateAngleY = 0.0F;
        model.bipedHead.rotateAngleZ = 0.0F;
        model.bipedHead.rotationPointX = 0.0F;
        model.bipedHead.rotationPointY = 0.0F;
        model.bipedHead.rotationPointZ = 0.0F;
        ModelBase.copyModelAngles(model.bipedHead, model.bipedHeadwear);
        model.bipedHeadwear.rotationPointX = 0.0F;
        model.bipedHeadwear.rotationPointY = 0.0F;
        model.bipedHeadwear.rotationPointZ = 0.0F;

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(0.80F, 0.95F, 1.0F, 0.55F);

        model.bipedHead.render(0.0625F);
        model.bipedHeadwear.render(0.0625F);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && GhostRestrictionsHandler.isGhost(player)) {
            event.setCanceled(true);
        }
    }
}

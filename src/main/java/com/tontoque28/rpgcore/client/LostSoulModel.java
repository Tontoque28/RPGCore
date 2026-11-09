package com.tontoque28.rpgcore.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class LostSoulModel extends ModelBase {

    private final ModelRenderer root;
    private final ModelRenderer core;
    private final ModelRenderer outerShell;
    private final ModelRenderer tail;
    private final ModelRenderer orbitRing;

    public LostSoulModel() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.root = new ModelRenderer(this);
        this.root.setRotationPoint(0.0F, 16.0F, 0.0F);

        this.core = new ModelRenderer(this, 0, 0);
        this.core.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
        this.root.addChild(this.core);

        this.outerShell = new ModelRenderer(this, 0, 0);
        this.outerShell.addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10);
        this.root.addChild(this.outerShell);

        this.tail = new ModelRenderer(this, 0, 0);
        this.tail.addBox(-2.0F, 4.0F, -2.0F, 4, 5, 4);
        this.root.addChild(this.tail);

        this.orbitRing = new ModelRenderer(this);
        this.orbitRing.setRotationPoint(0.0F, 0.0F, 0.0F);
        ModelRenderer wisp1 = new ModelRenderer(this, 0, 0);
        wisp1.addBox(11.0F, -1.5F, -1.0F, 2, 2, 2, 0.25F);
        ModelRenderer wisp2 = new ModelRenderer(this, 0, 0);
        wisp2.addBox(-6.5F, -2.5F, 9.5F, 2, 2, 2, 0.25F);
        ModelRenderer wisp3 = new ModelRenderer(this, 0, 0);
        wisp3.addBox(-6.5F, 1.5F, -11.5F, 2, 2, 2, 0.25F);
        this.orbitRing.addChild(wisp1);
        this.orbitRing.addChild(wisp2);
        this.orbitRing.addChild(wisp3);
        this.root.addChild(this.orbitRing);
    }

    public void setupAnim(float ageInTicks) {
        this.root.rotationPointY = 16.0F + MathHelper.sin(ageInTicks * 0.1F) * 2.0F;

        this.core.rotateAngleY = ageInTicks * 0.06F;
        this.core.rotateAngleX = ageInTicks * 0.04F;

        this.outerShell.rotateAngleY = -ageInTicks * 0.035F;
        this.outerShell.rotateAngleZ = MathHelper.sin(ageInTicks * 0.05F) * 0.15F;

        this.tail.rotateAngleX = MathHelper.cos(ageInTicks * 0.1F) * 0.25F;
        this.tail.rotateAngleZ = MathHelper.sin(ageInTicks * 0.1F) * 0.25F;

        this.orbitRing.rotateAngleY = ageInTicks * 0.12F;
        this.orbitRing.rotateAngleZ = MathHelper.sin(ageInTicks * 0.06F) * 0.3F;
    }

    public void renderCore(float scale) {
        this.outerShell.showModel = false;
        GlStateManager.color(0.85F, 0.98F, 1.0F, 0.95F);
        this.root.render(scale);
        this.outerShell.showModel = true;
    }

    public void renderAura(float scale) {
        this.core.showModel = false;
        this.tail.showModel = false;
        this.orbitRing.showModel = false;
        GlStateManager.color(0.25F, 0.80F, 1.0F, 0.40F);
        this.root.render(scale);
        this.core.showModel = true;
        this.tail.showModel = true;
        this.orbitRing.showModel = true;
    }
}

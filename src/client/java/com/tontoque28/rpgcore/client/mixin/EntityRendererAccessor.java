package com.tontoque28.rpgcore.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

    @Accessor("shadowRadius")
    void rpgcore$setShadowRadius(float value);

    @Accessor("shadowStrength")
    void rpgcore$setShadowStrength(float value);
}

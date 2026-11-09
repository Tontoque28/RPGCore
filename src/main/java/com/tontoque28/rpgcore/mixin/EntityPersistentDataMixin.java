package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.capability.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPersistentDataMixin implements PersistentDataHolder {

    @Unique
    private CompoundTag rpgcore$persistentData;

    @Override
    public CompoundTag rpgcore$getPersistentData() {
        if (this.rpgcore$persistentData == null) {
            this.rpgcore$persistentData = new CompoundTag();
        }
        return this.rpgcore$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void rpgcore$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.rpgcore$persistentData != null && !this.rpgcore$persistentData.isEmpty()) {
            tag.put("rpgcore_data", this.rpgcore$persistentData.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void rpgcore$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("rpgcore_data", 10)) {
            this.rpgcore$persistentData = tag.getCompound("rpgcore_data");
        }
    }
}

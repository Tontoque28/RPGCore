package com.tontoque28.rpgcore.mixin;

import com.tontoque28.rpgcore.capability.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void rpgcore$save(ValueOutput output, CallbackInfo ci) {
        if (this.rpgcore$persistentData != null && !this.rpgcore$persistentData.isEmpty()) {
            output.store("rpgcore_data", CompoundTag.CODEC, this.rpgcore$persistentData.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void rpgcore$load(ValueInput input, CallbackInfo ci) {
        input.read("rpgcore_data", CompoundTag.CODEC).ifPresent(data -> this.rpgcore$persistentData = data);
    }
}

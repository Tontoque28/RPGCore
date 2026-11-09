package com.tontoque28.rpgcore.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface PersistentDataHolder {

    CompoundTag rpgcore$getPersistentData();

    static CompoundTag of(Entity entity) {
        return ((PersistentDataHolder) entity).rpgcore$getPersistentData();
    }
}

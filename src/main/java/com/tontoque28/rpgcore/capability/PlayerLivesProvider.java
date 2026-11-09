package com.tontoque28.rpgcore.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerLivesProvider implements ICapabilitySerializable<CompoundNBT> {

    @CapabilityInject(PlayerLives.class)
    public static Capability<PlayerLives> PLAYER_LIVES = null;

    private PlayerLives lives = null;
    private final LazyOptional<PlayerLives> optional = LazyOptional.of(this::createPlayerLives);

    private PlayerLives createPlayerLives() {
        if (this.lives == null) {
            this.lives = new PlayerLives();
        }
        return this.lives;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_LIVES) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        createPlayerLives().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        createPlayerLives().loadNBTData(nbt);
    }
}

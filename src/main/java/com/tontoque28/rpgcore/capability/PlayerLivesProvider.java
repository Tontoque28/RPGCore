package com.tontoque28.rpgcore.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class PlayerLivesProvider implements ICapabilitySerializable<NBTTagCompound> {

    @CapabilityInject(PlayerLives.class)
    public static Capability<PlayerLives> PLAYER_LIVES = null;

    private final PlayerLives lives = new PlayerLives();

    public static PlayerLives of(EntityPlayer player) {
        if (player == null || PLAYER_LIVES == null) {
            return null;
        }
        return player.getCapability(PLAYER_LIVES, null);
    }

    public static int livesOf(EntityPlayer player, int fallback) {
        PlayerLives value = of(player);
        return value == null ? fallback : value.getLives();
    }

    public static final class Storage implements Capability.IStorage<PlayerLives> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<PlayerLives> capability, PlayerLives instance, EnumFacing side) {
            NBTTagCompound nbt = new NBTTagCompound();
            instance.saveNBTData(nbt);
            return nbt;
        }

        @Override
        public void readNBT(Capability<PlayerLives> capability, PlayerLives instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagCompound) {
                instance.loadNBTData((NBTTagCompound) nbt);
            }
        }
    }

    public static final class Factory implements Callable<PlayerLives> {
        @Override
        public PlayerLives call() {
            return new PlayerLives();
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_LIVES;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == PLAYER_LIVES) {
            return PLAYER_LIVES.cast(lives);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        lives.saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        lives.loadNBTData(nbt);
    }
}

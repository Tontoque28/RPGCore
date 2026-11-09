package com.tontoque28.rpgcore.capability;

import net.minecraft.nbt.CompoundNBT;

public class PlayerLives {
    private int lives;

    public PlayerLives() {
        this.lives = 10;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    public void addLives(int amount) {
        this.lives = Math.max(0, this.lives + amount);
    }

    public void removeLives(int amount) {
        this.lives = Math.max(0, this.lives - amount);
    }

    public void copyFrom(PlayerLives source) {
        this.lives = source.lives;
    }

    public void saveNBTData(CompoundNBT nbt) {
        nbt.putInt("rpgcore_lives", this.lives);
    }

    public void loadNBTData(CompoundNBT nbt) {
        if (nbt.contains("rpgcore_lives")) {
            this.lives = Math.max(0, nbt.getInt("rpgcore_lives"));
        } else {
            this.lives = 10;
        }
    }
}

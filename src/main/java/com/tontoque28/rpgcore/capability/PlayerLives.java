package com.tontoque28.rpgcore.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerLives {

    public static final String KEY = "rpgcore_lives";
    public static final int DEFAULT_LIVES = 10;

    private PlayerLives() {
    }

    public static int getLives(Player player) {
        CompoundTag data = PersistentDataHolder.of(player);
        if (data.contains(KEY)) {
            return Math.max(0, data.getInt(KEY));
        }
        return DEFAULT_LIVES;
    }

    public static void setLives(Player player, int lives) {
        PersistentDataHolder.of(player).putInt(KEY, Math.max(0, lives));
    }

    public static void addLives(Player player, int amount) {
        setLives(player, getLives(player) + amount);
    }

    public static void removeLives(Player player, int amount) {
        setLives(player, getLives(player) - amount);
    }

    public static void copyFrom(Player source, Player target) {
        setLives(target, getLives(source));
    }
}

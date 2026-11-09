package com.tontoque28.rpgcore.events;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

public class WorldDebuffState extends WorldSavedData {

    static final String DATA_NAME = "rpgcore_world_debuff";
    private static final String WORLD_DEBUFF_INDEX = "debuff_index";
    private static final String WORLD_DEBUFF_INDICES = "debuff_indices";
    private static final String WORLD_ALL_DEAD_HANDLED = "all_dead_handled";
    private static final String WORLD_LOST = "world_lost";

    final List<Integer> debuffIndices = new ArrayList<Integer>();
    boolean allDeadHandled;
    boolean worldLost;

    public WorldDebuffState(String name) {
        super(name);
    }

    static WorldDebuffState get(MinecraftServer server) {
        MapStorage storage = server.getWorld(0).getMapStorage();
        WorldDebuffState state = (WorldDebuffState) storage.getOrLoadData(WorldDebuffState.class, DATA_NAME);
        if (state == null) {
            state = new WorldDebuffState(DATA_NAME);
            storage.setData(DATA_NAME, state);
        }
        return state;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.debuffIndices.clear();
        if (tag.hasKey(WORLD_DEBUFF_INDICES)) {
            for (int index : tag.getIntArray(WORLD_DEBUFF_INDICES)) {
                if (index >= 0 && index < WorldDebuffs.ALL.length && !debuffIndices.contains(index)) {
                    debuffIndices.add(index);
                }
            }
        } else if (tag.hasKey(WORLD_DEBUFF_INDEX)) {
            int legacyIndex = tag.getInteger(WORLD_DEBUFF_INDEX);
            if (legacyIndex >= 0 && legacyIndex < WorldDebuffs.ALL.length) {
                debuffIndices.add(legacyIndex);
            }
        }
        this.allDeadHandled = tag.getBoolean(WORLD_ALL_DEAD_HANDLED);
        this.worldLost = tag.getBoolean(WORLD_LOST);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        int[] indices = new int[debuffIndices.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = debuffIndices.get(i);
        }
        tag.setIntArray(WORLD_DEBUFF_INDICES, indices);
        tag.setBoolean(WORLD_ALL_DEAD_HANDLED, allDeadHandled);
        tag.setBoolean(WORLD_LOST, worldLost);
        return tag;
    }

    boolean hasDebuff(int index) {
        return debuffIndices.contains(index);
    }
}

package com.tontoque28.rpgcore.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RpgCoreConfig {

    private static final String[] DEFAULT_REWARDS = {
            "minecraft:wither|D|1",
            "minecraft:ender_dragon|S|3",
            "minecraft:warden|D|1",
            "mowziesmobs:frostmaw|E|1",
            "goety:vizier|B|2",
            "mowziesmobs:ferrous_wroughtnaut|A|2",
            "bosses_of_mass_destruction:lich|B|2",
            "cataclysm:ancient_remnant|A|2",
            "cataclysm:netherite_monstrosity|S|3",
            "bosses_of_mass_destruction:void_blossom|S|3",
            "cataclysm:the_harbinger|SS|4",
            "cataclysm:ignis|SS+|5"
    };

    private static volatile List<String> bossRewards = Collections.unmodifiableList(Arrays.asList(DEFAULT_REWARDS));

    private RpgCoreConfig() {
    }

    public static List<String> getBossRewards() {
        return bossRewards;
    }

    public static void load(File file) {
        Configuration configuration = new Configuration(file);
        Property property = configuration.get(
                "general",
                "boss_rewards",
                DEFAULT_REWARDS,
                "Boss rewards use the format: entity_id|tier_label|heart_count\n"
                        + "The tier label is informational and is configured by the modpack.\n"
                        + "RPGCore only validates the entity id and drops Life Heart items."
        );
        List<String> values = new ArrayList<>(Arrays.asList(property.getStringList()));
        boolean valid = true;
        for (String entry : values) {
            if (!isValid(entry)) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            property.set(DEFAULT_REWARDS);
            values = new ArrayList<>(Arrays.asList(DEFAULT_REWARDS));
        }
        bossRewards = Collections.unmodifiableList(values);
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static boolean isValid(String entry) {
        if (entry == null) {
            return false;
        }
        String[] parts = entry.split("\\|", -1);
        if (parts.length != 3 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(parts[2]) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}

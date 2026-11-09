package com.tontoque28.rpgcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class RpgCoreConfig {

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_REWARDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        BOSS_REWARDS = builder
                .comment(
                        "Boss rewards use the format: entity_id|tier_label|heart_count",
                        "The tier label is informational and is configured by the modpack.",
                        "RPGCore only validates the entity id and drops Life Heart items."
                )
                .defineListAllowEmpty(
                        "boss_rewards",
                        List.of(
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
                        ),
                        value -> {
                            if (!(value instanceof String entry)) {
                                return false;
                            }
                            String[] parts = entry.split("\\|", -1);
                            if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank()) {
                                return false;
                            }
                            try {
                                return Integer.parseInt(parts[2]) > 0;
                            } catch (NumberFormatException exception) {
                                return false;
                            }
                        }
                );
        SERVER_SPEC = builder.build();
    }

    private RpgCoreConfig() {
    }
}

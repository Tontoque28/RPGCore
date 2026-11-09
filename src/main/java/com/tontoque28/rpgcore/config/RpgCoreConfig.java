package com.tontoque28.rpgcore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RpgCoreConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String KEY = "boss_rewards";

    public static final List<String> DEFAULT_BOSS_REWARDS = List.of(
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
    );

    private static volatile List<String> bossRewards = new ArrayList<>(DEFAULT_BOSS_REWARDS);

    private RpgCoreConfig() {
    }

    public static List<String> getBossRewards() {
        return bossRewards;
    }

    public static boolean isValidEntry(Object value) {
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

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("rpgcore-server.json");
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            bossRewards = new ArrayList<>(DEFAULT_BOSS_REWARDS);
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject() || !root.getAsJsonObject().has(KEY) || !root.getAsJsonObject().get(KEY).isJsonArray()) {
                LOGGER.warn("RPGCore: configuracion invalida, se restauran los valores por defecto.");
                bossRewards = new ArrayList<>(DEFAULT_BOSS_REWARDS);
                save();
                return;
            }
            List<String> loaded = new ArrayList<>();
            boolean changed = false;
            for (JsonElement element : root.getAsJsonObject().getAsJsonArray(KEY)) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && isValidEntry(element.getAsString())) {
                    loaded.add(element.getAsString());
                } else {
                    changed = true;
                    LOGGER.warn("RPGCore: entrada boss_rewards invalida ignorada: {}", element);
                }
            }
            bossRewards = loaded;
            if (changed) {
                save();
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("RPGCore: no se pudo leer la configuracion, se usan los valores por defecto.", exception);
            bossRewards = new ArrayList<>(DEFAULT_BOSS_REWARDS);
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();
        for (String entry : bossRewards) {
            array.add(entry);
        }
        root.add(KEY, array);
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("RPGCore: no se pudo guardar la configuracion.", exception);
        }
    }
}

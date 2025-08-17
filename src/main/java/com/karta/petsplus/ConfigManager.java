package com.karta.petsplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final PetsPlus plugin;
    private FileConfiguration config;
    private File configFile;

    private File petsFile;
    private FileConfiguration petsConfig;
    private final Map<String, PetType> petTypes = new HashMap<>();

    public ConfigManager(PetsPlus plugin) {
        this.plugin = plugin;
        // The plugin's onEnable calls saveDefaultConfig, so we can just get the config.
        this.config = plugin.getConfig();
        loadPets();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getLogger().info("config.yml has been reloaded.");
        reloadPets(); // Also reload pets when the main config is reloaded
    }

    public void loadPets() {
        if (petsFile == null) {
            petsFile = new File(plugin.getDataFolder(), "pets.yml");
        }
        if (!petsFile.exists()) {
            plugin.saveResource("pets.yml", false);
        }

        petsConfig = YamlConfiguration.loadConfiguration(petsFile);
        petTypes.clear();

        ConfigurationSection petsSection = petsConfig.getConfigurationSection("");
        if (petsSection == null) {
            plugin.getLogger().warning("pets.yml is empty or invalid.");
            return;
        }

        for (String key : petsSection.getKeys(false)) {
            // This is a check to avoid trying to load the comment header as a pet
            if (!petsConfig.isConfigurationSection(key)) {
                continue;
            }

            try {
                String displayName = ChatColor.translateAlternateColorCodes('&', petsConfig.getString(key + ".display-name", key));
                String entityTypeName = petsConfig.getString(key + ".entity", "").toUpperCase();
                EntityType entityType = EntityType.valueOf(entityTypeName);
                double price = petsConfig.getDouble(key + ".price", 0.0);
                boolean isBaby = petsConfig.getBoolean(key + ".baby", false);

                PetType petType = new PetType(key, displayName, entityType, price, isBaby);
                petTypes.put(key.toLowerCase(), petType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type '" + petsConfig.getString(key + ".entity") + "' for pet '" + key + "' in pets.yml. Skipping.");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load pet '" + key + "' from pets.yml.", e);
            }
        }
        plugin.getLogger().info("Loaded " + petTypes.size() + " pet types from pets.yml.");
    }

    public void reloadPets() {
        loadPets();
    }

    public PetType getPetType(String internalName) {
        if (internalName == null) return null;
        return petTypes.get(internalName.toLowerCase());
    }

    public Map<String, PetType> getPetTypes() {
        return Collections.unmodifiableMap(petTypes);
    }

    public int getFollowTaskTicks() {
        return config.getInt("follow-task-ticks", 20);
    }

    public int getMaxPetsPerPlayer() {
        return config.getInt("max-pets-per-player", 1);
    }
}

package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Manages the plugin's configuration files.
 * This class handles loading, creating, and reloading config.yml, messages.yml, and pets.yml.
 */
public class ConfigManager {

    private final KartaPetsPlus plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messages;
    private File messagesFile;
    private FileConfiguration pets;
    private File petsFile;

    public ConfigManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        saveDefaultConfigs();
    }

    /**
     * Saves the default configuration files from the JAR to the plugin's data folder if they do not already exist.
     */
    public void saveDefaultConfigs() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        if (petsFile == null) {
            petsFile = new File(plugin.getDataFolder(), "pets.yml");
        }
        if (!petsFile.exists()) {
            plugin.saveResource("pets.yml", false);
        }
    }

    /**
     * Loads all configuration files into memory.
     */
    public void loadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        pets = YamlConfiguration.loadConfiguration(petsFile);
        plugin.getLogger().info("Configuration files have been loaded.");
    }

    /**
     * Reloads all configuration files from disk.
     */
    public void reloadConfigs() {
        try {
            config.load(configFile);
            messages.load(messagesFile);
            pets.load(petsFile);
            plugin.getLogger().info("Configuration files have been reloaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not reload configuration files: " + e.getMessage());
        }
    }

    /**
     * Gets the main plugin configuration (config.yml).
     * @return The FileConfiguration for config.yml.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfigs();
        }
        return config;
    }

    /**
     * Gets the messages configuration (messages.yml).
     * @return The FileConfiguration for messages.yml.
     */
    public FileConfiguration getMessages() {
        if (messages == null) {
            loadConfigs();
        }
        return messages;
    }

    /**
     * Gets the pets configuration (pets.yml).
     * @return The FileConfiguration for pets.yml.
     */
    public FileConfiguration getPets() {
        if (pets == null) {
            loadConfigs();
        }
        return pets;
    }

    /**
     * Saves the pets configuration to pets.yml.
     */
    public void savePets() {
        if (pets == null || petsFile == null) {
            return;
        }
        try {
            pets.save(petsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pets.yml: " + e.getMessage());
        }
    }
}

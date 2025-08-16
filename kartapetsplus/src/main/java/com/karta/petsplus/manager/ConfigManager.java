package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Manages the plugin's configuration files.
 * This class handles loading, creating, and accessing config.yml, messages.yml, and pets.yml.
 */
public class ConfigManager {

    private final KartaPetsPlus plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration pets;

    private File configFile;
    private File messagesFile;
    private File petsFile;

    /**
     * Constructs a new ConfigManager.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        setup();
    }

    /**
     * Sets up and loads the configuration files.
     * Creates default files if they do not exist.
     */
    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        petsFile = new File(plugin.getDataFolder(), "pets.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        if (!petsFile.exists()) {
            plugin.saveResource("pets.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        pets = YamlConfiguration.loadConfiguration(petsFile);
    }

    /**
     * Gets the main configuration (config.yml).
     *
     * @return The FileConfiguration for config.yml.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the messages configuration (messages.yml).
     *
     * @return The FileConfiguration for messages.yml.
     */
    public FileConfiguration getMessages() {
        return messages;
    }

    /**
     * Gets the pets configuration (pets.yml).
     *
     * @return The FileConfiguration for pets.yml.
     */
    public FileConfiguration getPets() {
        return pets;
    }

    /**
     * Reloads all configuration files from disk.
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        pets = YamlConfiguration.loadConfiguration(petsFile);
    }
}

package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private final KartaPetsPlus plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messages;
    private File messagesFile;
    private FileConfiguration pets;
    private File petsFile;

    // Behavior settings
    private int behaviorTicks;
    private int maxActivePetsPerPlayer;
    private double teleportDistance;
    private boolean debugLogging;
    private boolean autoRespawnOnJoin;
    private boolean autoRespawnOnDeath;
    private List<String> blacklistedWorlds;
    private DamagePolicy damagePolicy;
    private boolean cancelFallDamageWhenRidingPet;
    private boolean removePetOnWorldChange;
    private boolean petInteractionGuiEnabled;

    // Rename settings
    private boolean renameEnabled;
    private List<String> renameBlockedWords;
    private String renameBlockedPattern;
    private boolean renameColorEnabled;
    private boolean renameColorHex;
    private boolean renameLimitCharsEnabled;
    private int renameLimitCharsNumber;
    private boolean renameTrim;

    public enum DamagePolicy {
        INVULNERABLE,
        OWNER_ONLY,
        ALL
    }

    public ConfigManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        saveDefaultConfigs();
    }

    public void saveDefaultConfigs() {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);

        if (messagesFile == null) messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);

        if (petsFile == null) petsFile = new File(plugin.getDataFolder(), "pets.yml");
        if (!petsFile.exists()) plugin.saveResource("pets.yml", false);
    }

    public void loadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        pets = YamlConfiguration.loadConfiguration(petsFile);
        loadBehaviorSettings();
        plugin.getLogger().info("Configuration files have been loaded.");
    }

    private void loadBehaviorSettings() {
        behaviorTicks = getConfig().getInt("pet-behavior.ticks-per-update", 10);
        maxActivePetsPerPlayer = getConfig().getInt("pet-behavior.max-active-pets-per-player", 1);
        teleportDistance = getConfig().getDouble("pet-behavior.teleport-distance", 15.0);
        debugLogging = getConfig().getBoolean("pet-behavior.debug-logging", false);
        autoRespawnOnJoin = getConfig().getBoolean("pet-behavior.auto-respawn-on-join", true);
        autoRespawnOnDeath = getConfig().getBoolean("pet-behavior.auto-respawn-on-death", true);
        blacklistedWorlds = getConfig().getStringList("pet-behavior.world-blacklist");

        try {
            damagePolicy = DamagePolicy.valueOf(getConfig().getString("pet-behavior.damage-policy", "INVULNERABLE").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid damage policy found in config.yml. Defaulting to INVULNERABLE.");
            damagePolicy = DamagePolicy.INVULNERABLE;
        }
        cancelFallDamageWhenRidingPet = getConfig().getBoolean("pet-behavior.cancel-fall-damage-when-riding-pet", true);
        removePetOnWorldChange = getConfig().getBoolean("pet-behavior.remove-pet-on-world-change", true);
        petInteractionGuiEnabled = getConfig().getBoolean("pet-behavior.pet-interaction-gui-enabled", true);

        // Load rename settings
        renameEnabled = getConfig().getBoolean("pet-rename.enabled", true);
        renameBlockedWords = getConfig().getStringList("pet-rename.blocked-words");
        renameBlockedPattern = getConfig().getString("pet-rename.blocked-pattern", "");
        renameColorEnabled = getConfig().getBoolean("pet-rename.color-enabled", true);
        renameColorHex = getConfig().getBoolean("pet-rename.color-hex", true);
        renameLimitCharsEnabled = getConfig().getBoolean("pet-rename.limit-chars-enabled", true);
        renameLimitCharsNumber = getConfig().getInt("pet-rename.limit-chars-number", 16);
        renameTrim = getConfig().getBoolean("pet-rename.trim", true);
    }

    public void reloadConfigs() {
        try {
            config.load(configFile);
            messages.load(messagesFile);
            pets.load(petsFile);
            loadBehaviorSettings();
            plugin.getLogger().info("Configuration files have been reloaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not reload configuration files: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) loadConfigs();
        return config;
    }

    public FileConfiguration getMessages() {
        if (messages == null) loadConfigs();
        return messages;
    }

    public FileConfiguration getPets() {
        if (pets == null) loadConfigs();
        return pets;
    }

    public void savePets() {
        if (pets == null || petsFile == null) return;
        try {
            pets.save(petsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pets.yml: " + e.getMessage());
        }
    }

    // --- Behavior Settings Getters ---

    public int getBehaviorTicks() { return behaviorTicks; }
    public int getMaxActivePetsPerPlayer() { return maxActivePetsPerPlayer; }
    public double getTeleportDistance() { return teleportDistance; }
    public boolean isDebugLogging() { return debugLogging; }
    public boolean isAutoRespawnOnJoin() { return autoRespawnOnJoin; }
    public boolean isAutoRespawnOnDeath() { return autoRespawnOnDeath; }
    public List<String> getBlacklistedWorlds() { return Collections.unmodifiableList(blacklistedWorlds); }
    public DamagePolicy getDamagePolicy() { return damagePolicy; }
    public boolean isCancelFallDamageWhenRidingPet() { return cancelFallDamageWhenRidingPet; }
    public boolean isRemovePetOnWorldChange() { return removePetOnWorldChange; }
    public boolean isPetInteractionGuiEnabled() { return petInteractionGuiEnabled; }

    // --- Rename Settings Getters ---
    public boolean isRenameEnabled() { return renameEnabled; }
    public List<String> getRenameBlockedWords() { return renameBlockedWords; }
    public String getRenameBlockedPattern() { return renameBlockedPattern; }
    public boolean isRenameColorEnabled() { return renameColorEnabled; }
    public boolean isRenameColorHex() { return renameColorHex; }
    public boolean isRenameLimitCharsEnabled() { return renameLimitCharsEnabled; }
    public int getRenameLimitCharsNumber() { return renameLimitCharsNumber; }
    public boolean isRenameTrim() { return renameTrim; }
}

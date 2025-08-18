package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ShopConfig {

    private final PetsPlus plugin;
    private FileConfiguration config;

    // Cached values
    private String menuTitle;
    private int menuSize;
    private List<Integer> contentSlots;
    private long clickCooldown;
    private boolean asyncEconomy;
    private boolean formatPrice;
    private Sound insufficientFundsSound;
    private Sound successSound;

    public ShopConfig(PetsPlus plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!configFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        parseConfig();
    }

    private void parseConfig() {
        menuTitle = config.getString("shop-menu.title", "&e&lPet Shop");
        menuSize = config.getInt("shop-menu.size", 54);
        if (menuSize % 9 != 0 || menuSize < 9 || menuSize > 54) {
            plugin.getLogger().warning("[Shop] Invalid shop menu size in shop.yml. Must be a multiple of 9 between 9 and 54. Defaulting to 54.");
            menuSize = 54;
        }

        parseContentSlots();

        clickCooldown = config.getLong("behavior.click-cooldown-ms", 500);
        asyncEconomy = config.getBoolean("behavior.async-economy", true);
        formatPrice = config.getBoolean("behavior.format-price", true);

        try {
            insufficientFundsSound = Sound.valueOf(config.getString("behavior.insufficient-funds-sound", "ENTITY_VILLAGER_NO").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid insufficient-funds-sound in shop.yml. Defaulting to ENTITY_VILLAGER_NO.");
            insufficientFundsSound = Sound.ENTITY_VILLAGER_NO;
        }

        try {
            successSound = Sound.valueOf(config.getString("behavior.success-sound", "ENTITY_PLAYER_LEVELUP").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid success-sound in shop.yml. Defaulting to ENTITY_PLAYER_LEVELUP.");
            successSound = Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    private void parseContentSlots() {
        contentSlots = new ArrayList<>();
        List<String> rawSlots = config.getStringList("shop-menu.content-slots");
        if (rawSlots.isEmpty()) {
            plugin.getLogger().warning("[Shop] 'shop-menu.content-slots' is empty in shop.yml. The shop will not display any items.");
            return;
        }

        for (String slotEntry : rawSlots) {
            try {
                if (slotEntry.contains("-")) {
                    String[] parts = slotEntry.split("-");
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    for (int i = start; i <= end; i++) {
                        contentSlots.add(i);
                    }
                } else {
                    contentSlots.add(Integer.parseInt(slotEntry.trim()));
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("[Shop] Invalid slot entry '" + slotEntry + "' in shop.yml. Skipping.");
            }
        }
        Collections.sort(contentSlots);
    }

    // --- Getters for cached values ---

    public String getMenuTitle() { return menuTitle; }
    public int getMenuSize() { return menuSize; }
    public List<Integer> getContentSlots() { return Collections.unmodifiableList(contentSlots); }
    public long getClickCooldown() { return clickCooldown; }
    public boolean isAsyncEconomy() { return asyncEconomy; }
    public boolean shouldFormatPrice() { return formatPrice; }
    public Sound getInsufficientFundsSound() { return insufficientFundsSound; }
    public Sound getSuccessSound() { return successSound; }

    // --- Direct access to ConfigurationSections for complex, non-cached data ---

    public ConfigurationSection getButton(String key) {
        return config.getConfigurationSection("shop-menu.buttons." + key);
    }

    public ConfigurationSection getDefaultPetSection() {
        return config.getConfigurationSection("defaults");
    }

    public ConfigurationSection getOverride(String petType) {
        return config.getConfigurationSection("overrides." + petType.toLowerCase());
    }

    public ConfigurationSection getConfirmSection() {
        return config.getConfigurationSection("confirm");
    }
}

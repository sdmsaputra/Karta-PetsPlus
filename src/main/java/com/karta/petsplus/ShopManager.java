package com.karta.petsplus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ShopManager {

    private final PetsPlus plugin;
    private FileConfiguration shopConfig;
    private File shopConfigFile;

    public ShopManager(PetsPlus plugin) {
        this.plugin = plugin;
        loadShopConfig();
    }

    public void loadShopConfig() {
        shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopConfigFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    public void reloadShopConfig() {
        if (shopConfigFile == null) {
            shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }
}

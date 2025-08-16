package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GuiManager {

    private final KartaPetsPlus plugin;
    private FileConfiguration guiConfig;

    public GuiManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        loadGuiConfig();
    }

    public void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
}

package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.PetType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class PetConfigManager {

    private final KartaPetsPlus plugin;
    private final Map<PetType, FileConfiguration> petConfigs = new EnumMap<>(PetType.class);
    private final File petsFolder;

    public PetConfigManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.petsFolder = new File(plugin.getDataFolder(), "pets");
        if (!petsFolder.exists()) {
            petsFolder.mkdirs();
        }
        loadPetConfigs();
    }

    public void loadPetConfigs() {
        for (PetType type : PetType.values()) {
            File petFile = new File(petsFolder, type.name().toLowerCase() + ".yml");
            if (!petFile.exists()) {
                plugin.saveResource("pets/" + type.name().toLowerCase() + ".yml", false);
            }
            petConfigs.put(type, YamlConfiguration.loadConfiguration(petFile));
        }
    }

    public FileConfiguration getPetConfig(PetType type) {
        return petConfigs.get(type);
    }

    public void savePetConfig(PetType type) {
        try {
            petConfigs.get(type).save(new File(petsFolder, type.name().toLowerCase() + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pet config for " + type.name() + ": " + e.getMessage());
        }
    }

    public void reloadPetConfigs() {
        petConfigs.clear();
        loadPetConfigs();
    }
}

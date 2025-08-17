package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlStorage implements Storage {

    private final PetsPlus plugin;
    private final File dataFolder;

    public YamlStorage(PetsPlus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }

    @Override
    public void init() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public void shutdown() {
        // Not needed for YAML
    }

    @Override
    public CompletableFuture<PetData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            if (!playerFile.exists()) {
                return null; // Or a default PetData object
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            String petType = config.getString("pet.type");
            String petName = config.getString("pet.name");
            int level = config.getInt("pet.level");
            double xp = config.getDouble("pet.xp");
            return new PetData(petType, petName, level, xp);
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            config.set("pet.type", data.getActivePetType());
            config.set("pet.name", data.getPetName());
            config.set("pet.level", data.getLevel());
            config.set("pet.xp", data.getXp());
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save pet data for " + uuid);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void createTables() {
        // Not needed for YAML
    }
}

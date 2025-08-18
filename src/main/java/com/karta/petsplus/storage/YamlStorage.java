package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlStorage implements Storage {

    private final PetsPlus plugin;
    private final File dataFolder;

    public YamlStorage(PetsPlus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
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
                // Return a new PetData object for new players
                return new PetData(null, null, 1, 0, new ArrayList<>());
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            String activePetType = config.getString("active-pet-type");
            String petName = config.getString("pet-name");
            int level = config.getInt("level", 1);
            double xp = config.getDouble("xp", 0);
            List<String> ownedPets = config.getStringList("owned-pets");

            return new PetData(activePetType, petName, level, xp, ownedPets);
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            FileConfiguration config = new YamlConfiguration(); // Use new config to avoid keeping old data

            config.set("active-pet-type", data.getActivePetType());
            config.set("pet-name", data.getPetName());
            config.set("level", data.getLevel());
            config.set("xp", data.getXp());
            config.set("owned-pets", data.getOwnedPets());

            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save pet data for " + uuid);
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Set<String>> getUnlockedPets(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            if (!playerFile.exists()) {
                return new HashSet<>();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            return new HashSet<>(config.getStringList("unlocked-pets"));
        });
    }

    @Override
    public CompletableFuture<Boolean> isPetUnlocked(UUID uuid, String petType) {
        return getUnlockedPets(uuid).thenApply(unlockedPets -> unlockedPets.contains(petType.toLowerCase()));
    }

    @Override
    public CompletableFuture<Void> unlockPet(UUID uuid, String petType) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            List<String> unlockedPets = config.getStringList("unlocked-pets");
            if (!unlockedPets.contains(petType.toLowerCase())) {
                unlockedPets.add(petType.toLowerCase());
                config.set("unlocked-pets", unlockedPets);
                try {
                    config.save(playerFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save unlocked pet data for " + uuid);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void createTables() {
        // Not needed for YAML
    }
}

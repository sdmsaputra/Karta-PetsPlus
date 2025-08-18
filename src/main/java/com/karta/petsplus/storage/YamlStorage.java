package com.karta.petsplus.storage;

import com.karta.petsplus.OwnedPet;
import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
                return new PetData(); // New player
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            List<OwnedPet> ownedPets = new ArrayList<>();
            UUID activePetId = null;

            if (config.isList("owned-pets")) {
                // Check if it's the old format (List<String>) or new format (List<Map>)
                Object firstElement = config.getList("owned-pets").get(0);
                if (firstElement instanceof String) {
                    // --- Data Migration from String List to OwnedPet List ---
                    List<String> oldOwnedPets = config.getStringList("owned-pets");
                    String oldActivePetType = config.getString("active-pet-type");
                    String oldPetName = config.getString("pet-name");

                    for (String petType : oldOwnedPets) {
                        OwnedPet newPet = new OwnedPet(petType);
                        if (petType.equalsIgnoreCase(oldActivePetType)) {
                             if (oldPetName != null && !oldPetName.isEmpty()) {
                                newPet.setCustomName(oldPetName);
                            }
                            activePetId = newPet.getPetId(); // Set this as the active pet
                            oldActivePetType = null; // Ensure we only do this once
                        }
                        ownedPets.add(newPet);
                    }
                } else {
                     // --- New Format (List<Map<String, Object>>) ---
                    List<Map<?, ?>> petMaps = config.getMapList("owned-pets");
                    for (Map<?, ?> petMap : petMaps) {
                        String type = (String) petMap.get("type");
                        UUID id = UUID.fromString((String) petMap.get("id"));
                        String name = (String) petMap.get("name");
                        ownedPets.add(new OwnedPet(type, id, name));
                    }
                     if (config.isString("active-pet-id")) {
                        activePetId = UUID.fromString(config.getString("active-pet-id"));
                    }
                }
            }


            int level = config.getInt("level", 1);
            double xp = config.getDouble("xp", 0);

            return new PetData(activePetId, level, xp, ownedPets);
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            FileConfiguration config = new YamlConfiguration();

            if (data.getActivePetId() != null) {
                config.set("active-pet-id", data.getActivePetId().toString());
            } else {
                config.set("active-pet-id", null);
            }

            config.set("level", data.getLevel());
            config.set("xp", data.getXp());

            List<Map<String, Object>> petList = new ArrayList<>();
            for (OwnedPet pet : data.getOwnedPets()) {
                Map<String, Object> petMap = new HashMap<>();
                petMap.put("type", pet.getPetType());
                petMap.put("id", pet.getPetId().toString());
                petMap.put("name", pet.getCustomName());
                petList.add(petMap);
            }
            config.set("owned-pets", petList);

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
         return loadPlayerData(uuid).thenApply(petData ->
            petData.getOwnedPets().stream()
                    .map(OwnedPet::getPetType)
                    .collect(Collectors.toSet())
        );
    }

    @Override
    public CompletableFuture<Boolean> isPetUnlocked(UUID uuid, String petType) {
        return getUnlockedPets(uuid).thenApply(unlockedPets -> unlockedPets.contains(petType.toLowerCase()));
    }

    @Override
    public CompletableFuture<Void> unlockPet(UUID uuid, String petType) {
        return loadPlayerData(uuid).thenCompose(petData -> {
            boolean alreadyOwned = petData.getOwnedPets().stream()
                    .anyMatch(p -> p.getPetType().equalsIgnoreCase(petType));
            if (!alreadyOwned) {
                petData.addOwnedPet(new OwnedPet(petType));
            }
            return savePlayerData(uuid, petData);
        });
    }

    @Override
    public void createTables() {
        // Not needed for YAML
    }
}

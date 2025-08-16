package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the storage and retrieval of player-specific pet data.
 * Data is stored in individual YAML files in the /data/ subfolder.
 */
public class PlayerDataManager {

    private final KartaPetsPlus plugin;
    private final File dataFolder;
    private final Map<UUID, List<Pet>> playerPetCache = new HashMap<>();

    public PlayerDataManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Loads a player's pet data from their YAML file into the cache.
     *
     * @param player The player whose data is to be loaded.
     */
    public void loadPlayerPets(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            playerPetCache.put(player.getUniqueId(), new ArrayList<>());
            return;
        }

        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);
        List<Pet> pets = new ArrayList<>();
        if (playerData.isConfigurationSection("pets")) {
            for (String petId : playerData.getConfigurationSection("pets").getKeys(false)) {
                String petType = playerData.getString("pets." + petId + ".type");
                String petName = playerData.getString("pets." + petId + ".name");
                Pet pet = new Pet(player.getUniqueId(), petType, petName);
                // Optionally load status, etc. here
                pets.add(pet);
            }
        }
        playerPetCache.put(player.getUniqueId(), pets);
    }

    /**
     * Saves a player's pet data from the cache into their YAML file.
     *
     * @param player The player whose data is to be saved.
     */
    public void savePlayerPets(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!playerPetCache.containsKey(playerUUID)) {
            return; // Nothing to save
        }

        File playerFile = new File(dataFolder, playerUUID + ".yml");
        FileConfiguration playerData = new YamlConfiguration();
        List<Pet> pets = playerPetCache.get(playerUUID);

        if (pets.isEmpty()) {
            if (playerFile.exists()) {
                playerFile.delete(); // Clean up empty files
            }
            return;
        }

        for (int i = 0; i < pets.size(); i++) {
            Pet pet = pets.get(i);
            String path = "pets.pet" + i;
            playerData.set(path + ".type", pet.getPetType());
            playerData.set(path + ".name", pet.getPetName());
            // Optionally save status, etc. here
        }

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pet data for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the list of pets for a given player from the cache.
     *
     * @param player The player.
     * @return A list of the player's pets.
     */
    public List<Pet> getPets(Player player) {
        return playerPetCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * Adds a new pet to a player's collection in the cache.
     *
     * @param player The player who will own the pet.
     * @param pet    The pet to add.
     */
    public void addPet(Player player, Pet pet) {
        playerPetCache.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(pet);
    }
}

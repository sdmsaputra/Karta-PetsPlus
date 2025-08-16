package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Manages the storage and retrieval of player-specific pet data.
 * Data is stored in individual YAML files in the /data/ subfolder.
 */
public class PlayerDataManager {

    private final KartaPetsPlus plugin;
    private final StorageManager storageManager;

    public PlayerDataManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.storageManager = new StorageManager(plugin);
    }

    /**
     * Loads a player's pet data from their YAML file into the cache.
     *
     * @param player The player whose data is to be loaded.
     */
    public void loadPlayerPets(Player player) {
        storageManager.loadPlayerPets(player);
    }

    /**
     * Saves a player's pet data from the cache into their YAML file.
     *
     * @param player The player whose data is to be saved.
     */
    public void savePlayerPets(Player player) {
        storageManager.savePlayerPets(player);
    }

    /**
     * Retrieves the list of pets for a given player from the cache.
     *
     * @param player The player.
     * @return A list of the player's pets.
     */
    public List<Pet> getPets(Player player) {
        return storageManager.getPets(player);
    }

    /**
     * Adds a new pet to a player's collection in the cache.
     *
     * @param player The player who will own the pet.
     * @param pet    The pet to add.
     */
    public void addPet(Player player, Pet pet) {
        storageManager.addPet(player, pet);
    }
}

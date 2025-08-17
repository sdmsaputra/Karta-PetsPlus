package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * @param player  The player who will own the pet.
     * @param petType The type of pet to add.
     */
    public void addPet(Player player, String petType) {
        storageManager.addPet(player, petType);
    }

    /**
     * Gets a specific pet for a player by its UUID.
     *
     * @param player The player.
     * @param petId  The UUID of the pet.
     * @return An Optional containing the pet if found.
     */
    public Optional<Pet> getPet(Player player, UUID petId) {
        return storageManager.getPet(player, petId);
    }

    /**
     * Checks if a player owns a pet of a specific type.
     *
     * @param player  The player.
     * @param petType The type of pet to check for.
     * @return True if the player owns a pet of this type, false otherwise.
     */
    public boolean hasPet(Player player, String petType) {
        return storageManager.hasPet(player, petType);
    }

    public void savePlayerPet(Player player, Pet pet) {
        storageManager.savePlayerPet(player, pet);
    }

    public void removePet(Player player, Pet pet) {
        storageManager.removePet(player, pet);
    }
}

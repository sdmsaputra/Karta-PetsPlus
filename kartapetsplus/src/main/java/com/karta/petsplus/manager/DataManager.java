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
 * Manages player pet data storage.
 * Handles saving and loading pet data from YAML files in the /data/ folder.
 */
public class DataManager {

    private final KartaPetsPlus plugin;
    private final File dataFolder;
    private final Map<UUID, List<Pet>> petCache = new HashMap<>();

    /**
     * Constructs a new DataManager.
     *
     * @param plugin The main plugin instance.
     */
    public DataManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Loads a player's pet data into the cache.
     *
     * @param player The player whose data to load.
     */
    public void loadPlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            petCache.put(player.getUniqueId(), new ArrayList<>());
            return;
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        // The list is deserialized automatically by Bukkit's system thanks to ConfigurationSerializable
        List<Pet> pets = (List<Pet>) data.getList("pets");
        petCache.put(player.getUniqueId(), pets != null ? pets : new ArrayList<>());
    }

    /**
     * Saves a player's pet data from the cache to their file.
     *
     * @param player The player whose data to save.
     */
    public void savePlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!petCache.containsKey(playerUUID)) {
            return;
        }
        File playerFile = new File(dataFolder, playerUUID + ".yml");
        FileConfiguration data = new YamlConfiguration();
        data.set("pets", petCache.get(playerUUID));
        try {
            data.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pet data for " + player.getName());
            e.printStackTrace();
        }
    }

    /**
     * Gets the list of pets for a player from the cache.
     *
     * @param player The player.
     * @return A list of the player's pets.
     */
    public List<Pet> getPets(Player player) {
        return petCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * Adds a new pet to a player.
     *
     * @param player The player to give the pet to.
     * @param pet    The pet to add.
     */
    public void addPet(Player player, Pet pet) {
        List<Pet> pets = petCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
        pets.add(pet);
        petCache.put(player.getUniqueId(), pets);
    }

    /**
     * Removes a player's data from the cache.
     * Typically called when the player logs out.
     *
     * @param player The player to remove from the cache.
     */
    public void uncachePlayerData(Player player) {
        petCache.remove(player.getUniqueId());
    }
}

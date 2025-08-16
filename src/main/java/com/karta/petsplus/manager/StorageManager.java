package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.storage.MySqlStorage;
import com.karta.petsplus.storage.Storage;
import com.karta.petsplus.storage.YamlStorage;
import org.bukkit.entity.Player;

import java.util.List;

public class StorageManager implements Storage {

    private final Storage storage;

    public StorageManager(KartaPetsPlus plugin) {
        String storageType = plugin.getConfigManager().getConfig().getString("storage-type", "yaml");
        if (storageType.equalsIgnoreCase("mysql")) {
            storage = new MySqlStorage(plugin);
        } else {
            storage = new YamlStorage(plugin);
        }
    }

    @Override
    public void loadPlayerPets(Player player) {
        storage.loadPlayerPets(player);
    }

    @Override
    public void savePlayerPets(Player player) {
        storage.savePlayerPets(player);
    }

    @Override
    public List<Pet> getPets(Player player) {
        return storage.getPets(player);
    }

    @Override
    public void addPet(Player player, Pet pet) {
        storage.addPet(player, pet);
    }
}

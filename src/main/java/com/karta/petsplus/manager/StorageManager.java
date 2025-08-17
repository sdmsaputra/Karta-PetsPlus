package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.storage.MySqlStorage;
import com.karta.petsplus.storage.Storage;
import com.karta.petsplus.storage.YamlStorage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public void addPet(Player player, String petType) {
        storage.addPet(player, petType);
    }

    @Override
    public Optional<Pet> getPet(Player player, UUID petId) {
        return storage.getPet(player, petId);
    }

    @Override
    public boolean hasPet(Player player, String petType) {
        return storage.hasPet(player, petType);
    }

    @Override
    public void savePlayerPet(Player player, Pet pet) {
        storage.savePlayerPet(player, pet);
    }

    @Override
    public void removePet(Player player, Pet pet) {
        storage.removePet(player, pet);
    }
}

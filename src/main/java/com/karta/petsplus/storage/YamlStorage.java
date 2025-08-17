package com.karta.petsplus.storage;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.data.PetType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YamlStorage implements Storage {

    private final KartaPetsPlus plugin;
    private final File dataFolder;
    private final Map<UUID, List<Pet>> playerPetCache = new HashMap<>();

    public YamlStorage(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public void loadPlayerPets(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            playerPetCache.put(player.getUniqueId(), new ArrayList<>());
            return;
        }

        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);
        List<Pet> pets = new ArrayList<>();
        if (playerData.isConfigurationSection("pets")) {
            for (String petIdStr : playerData.getConfigurationSection("pets").getKeys(false)) {
                UUID petId = UUID.fromString(petIdStr);
                String path = "pets." + petIdStr;
                PetType petType = PetType.valueOf(playerData.getString(path + ".type"));
                String petName = playerData.getString(path + ".name");
                String petStatus = playerData.getString(path + ".status", "STOWED");

                Pet pet = new Pet(player.getUniqueId(), petType, petName, petId);
                pet.setStatus(Pet.PetStatus.valueOf(petStatus));
                pets.add(pet);
            }
        }
        playerPetCache.put(player.getUniqueId(), pets);
    }

    @Override
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

        // Set pets to null to clear old data before saving
        playerData.set("pets", null);

        for (Pet pet : pets) {
            String path = "pets." + pet.getPetId().toString();
            playerData.set(path + ".type", pet.getPetType().name());
            playerData.set(path + ".name", pet.getPetName());
            playerData.set(path + ".status", pet.getStatus().name());
        }

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pet data for " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public List<Pet> getPets(Player player) {
        return playerPetCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    @Override
    public void addPet(Player player, String petType) {
        String displayName = plugin.getConfigManager().getPets().getString("pets." + petType + ".display-name", "My Pet");
        Pet newPet = new Pet(player.getUniqueId(), PetType.valueOf(petType), displayName);
        playerPetCache.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(newPet);
    }

    @Override
    public Optional<Pet> getPet(Player player, UUID petId) {
        return getPets(player).stream().filter(p -> p.getPetId().equals(petId)).findFirst();
    }

    @Override
    public boolean hasPet(Player player, String petType) {
        return getPets(player).stream().anyMatch(p -> p.getPetType().name().equalsIgnoreCase(petType));
    }

    @Override
    public void savePlayerPet(Player player, Pet pet) {
        List<Pet> pets = getPets(player);
        for (int i = 0; i < pets.size(); i++) {
            if (pets.get(i).getPetId().equals(pet.getPetId())) {
                pets.set(i, pet);
                break;
            }
        }
        savePlayerPets(player);
    }

    @Override
    public void removePet(Player player, Pet pet) {
        List<Pet> pets = getPets(player);
        pets.removeIf(p -> p.getPetId().equals(pet.getPetId()));
        savePlayerPets(player);
    }
}

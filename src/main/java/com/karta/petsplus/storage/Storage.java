package com.karta.petsplus.storage;

import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Storage {

    void loadPlayerPets(Player player);

    void savePlayerPets(Player player);

    List<Pet> getPets(Player player);

    Optional<Pet> getPet(Player player, UUID petId);

    void addPet(Player player, String petType);

    boolean hasPet(Player player, String petType);

    void savePlayerPet(Player player, Pet pet);
}

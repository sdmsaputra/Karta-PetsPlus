package com.karta.petsplus.storage;

import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Player;

import java.util.List;

public interface Storage {

    void loadPlayerPets(Player player);

    void savePlayerPets(Player player);

    List<Pet> getPets(Player player);

    void addPet(Player player, Pet pet);
}

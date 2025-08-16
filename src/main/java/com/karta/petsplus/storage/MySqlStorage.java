package com.karta.petsplus.storage;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySqlStorage implements Storage {

    private final KartaPetsPlus plugin;

    public MySqlStorage(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void loadPlayerPets(Player player) {
        // Data is loaded on demand from the database, so this can be a no-op
    }

    @Override
    public void savePlayerPets(Player player) {
        // Data is saved on demand to the database, so this can be a no-op
    }

    @Override
    public List<Pet> getPets(Player player) {
        List<Pet> pets = new ArrayList<>();
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM pets WHERE owner_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String petType = resultSet.getString("pet_type");
                String petName = resultSet.getString("pet_name");
                String petStatus = resultSet.getString("pet_status");
                Pet pet = new Pet(player.getUniqueId(), petType, petName);
                pet.setStatus(Pet.PetStatus.valueOf(petStatus));
                pets.add(pet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load pets from database for " + player.getName() + ": " + e.getMessage());
        }
        return pets;
    }

    @Override
    public void addPet(Player player, Pet pet) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO pets (owner_uuid, pet_type, pet_name, pet_status) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, pet.getPetType());
            statement.setString(3, pet.getPetName());
            statement.setString(4, pet.getStatus().name());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add pet to database for " + player.getName() + ": " + e.getMessage());
        }
    }
}

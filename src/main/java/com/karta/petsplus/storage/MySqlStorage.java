package com.karta.petsplus.storage;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.data.PetType;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
             PreparedStatement statement = connection.prepareStatement("SELECT pet_uuid, pet_type, pet_name, pet_status FROM pets WHERE owner_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID petId = UUID.fromString(resultSet.getString("pet_uuid"));
                PetType petType = PetType.valueOf(resultSet.getString("pet_type"));
                String petName = resultSet.getString("pet_name");
                String petStatus = resultSet.getString("pet_status");
                Pet pet = new Pet(player.getUniqueId(), petType, petName, petId);
                pet.setStatus(Pet.PetStatus.valueOf(petStatus));
                pets.add(pet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load pets from database for " + player.getName() + ": " + e.getMessage());
        }
        return pets;
    }

    @Override
    public Optional<Pet> getPet(Player player, UUID petId) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT pet_type, pet_name, pet_status FROM pets WHERE owner_uuid = ? AND pet_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, petId.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                PetType petType = PetType.valueOf(resultSet.getString("pet_type"));
                String petName = resultSet.getString("pet_name");
                String petStatus = resultSet.getString("pet_status");
                Pet pet = new Pet(player.getUniqueId(), petType, petName, petId);
                pet.setStatus(Pet.PetStatus.valueOf(petStatus));
                return Optional.of(pet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load pet from database for " + player.getName() + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void addPet(Player player, String petType) {
        String defaultName = plugin.getConfigManager().getPets().getString("pets." + petType + ".display-name", "My Pet");
        Pet newPet = new Pet(player.getUniqueId(), PetType.valueOf(petType), defaultName, UUID.randomUUID());

        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO pets (owner_uuid, pet_uuid, pet_type, pet_name, pet_status) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, newPet.getPetId().toString());
            statement.setString(3, newPet.getPetType().name());
            statement.setString(4, newPet.getPetName());
            statement.setString(5, newPet.getStatus().name());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add pet to database for " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean hasPet(Player player, String petType) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM pets WHERE owner_uuid = ? AND pet_type = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, petType);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check pet ownership in database for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public void savePlayerPet(Player player, Pet pet) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE pets SET pet_name = ?, pet_status = ? WHERE pet_uuid = ?")) {
            statement.setString(1, pet.getPetName());
            statement.setString(2, pet.getStatus().name());
            statement.setString(3, pet.getPetId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save pet data to database for " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void removePet(Player player, Pet pet) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM pets WHERE pet_uuid = ?")) {
            statement.setString(1, pet.getPetId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove pet from database for " + player.getName() + ": " + e.getMessage());
        }
    }
}

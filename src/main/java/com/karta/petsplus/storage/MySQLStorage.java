package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLStorage implements Storage {

    private final PetsPlus plugin;
    private Connection connection;

    public MySQLStorage(PetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("storage.mysql");
            String host = mysqlConfig.getString("host");
            int port = mysqlConfig.getInt("port");
            String database = mysqlConfig.getString("database");
            String username = mysqlConfig.getString("username");
            String password = mysqlConfig.getString("password");

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to MySQL database!");
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTables() {
        String tableQuery = "CREATE TABLE IF NOT EXISTS `pet_data` (" +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`active_pet_type` VARCHAR(255) NULL," +
                "`pet_name` VARCHAR(255) NULL," +
                "`level` INT NOT NULL DEFAULT 1," +
                "`xp` DOUBLE NOT NULL DEFAULT 0," +
                "`owned_pets` TEXT NOT NULL," +
                "PRIMARY KEY (`uuid`)" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(tableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<PetData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM pet_data WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String activePetType = rs.getString("active_pet_type");
                    String petName = rs.getString("pet_name");
                    int level = rs.getInt("level");
                    double xp = rs.getDouble("xp");
                    String ownedPetsRaw = rs.getString("owned_pets");
                    List<String> ownedPets = new ArrayList<>();
                    if (ownedPetsRaw != null && !ownedPetsRaw.isEmpty()) {
                        ownedPets.addAll(Arrays.asList(ownedPetsRaw.split(",")));
                    }
                    return new PetData(activePetType, petName, level, xp, ownedPets);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new PetData(null, null, 1, 0, new ArrayList<>());
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO pet_data (uuid, active_pet_type, pet_name, level, xp, owned_pets) VALUES (?, ?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE active_pet_type = VALUES(active_pet_type), pet_name = VALUES(pet_name), " +
                           "level = VALUES(level), xp = VALUES(xp), owned_pets = VALUES(owned_pets)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, data.getActivePetType());
                stmt.setString(3, data.getPetName());
                stmt.setInt(4, data.getLevel());
                stmt.setDouble(5, data.getXp());
                String ownedPetsStr = String.join(",", data.getOwnedPets());
                stmt.setString(6, ownedPetsStr);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}

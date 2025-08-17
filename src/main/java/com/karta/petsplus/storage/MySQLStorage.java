package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
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
        String query = "CREATE TABLE IF NOT EXISTS `pet_data` (" +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`pet_type` VARCHAR(255) NULL," +
                "`pet_name` VARCHAR(255) NULL," +
                "`level` INT NOT NULL DEFAULT 1," +
                "`xp` DOUBLE NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)" +
                ");";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.execute();
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
                    String petType = rs.getString("pet_type");
                    String petName = rs.getString("pet_name");
                    int level = rs.getInt("level");
                    double xp = rs.getDouble("xp");
                    return new PetData(petType, petName, level, xp);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO pet_data (uuid, pet_type, pet_name, level, xp) VALUES (?, ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE pet_type = VALUES(pet_type), pet_name = VALUES(pet_name), " +
                           "level = VALUES(level), xp = VALUES(xp)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, data.getActivePetType());
                stmt.setString(3, data.getPetName());
                stmt.setInt(4, data.getLevel());
                stmt.setDouble(5, data.getXp());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}

package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteStorage implements Storage {

    private final PetsPlus plugin;
    private Connection connection;

    public SQLiteStorage(PetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            File dbFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.sqlite.file", "petsplus.db"));
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to SQLite database!");
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
        String query = "CREATE TABLE IF NOT EXISTS pet_data (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "pet_type VARCHAR(255)," +
                "pet_name VARCHAR(255)," +
                "level INT," +
                "xp DOUBLE" +
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
            String query = "INSERT OR REPLACE INTO pet_data (uuid, pet_type, pet_name, level, xp) VALUES (?, ?, ?, ?, ?)";
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

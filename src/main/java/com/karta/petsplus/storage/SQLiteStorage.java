package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;

import java.io.File;
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
import java.util.stream.Collectors;

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
        String tableQuery = "CREATE TABLE IF NOT EXISTS pet_data (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "active_pet_type VARCHAR(255)," +
                "pet_name VARCHAR(255)," +
                "level INT NOT NULL DEFAULT 1," +
                "xp DOUBLE NOT NULL DEFAULT 0," +
                "owned_pets TEXT NOT NULL DEFAULT ''" +
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
            // Return a new PetData object for new players
            return new PetData(null, null, 1, 0, new ArrayList<>());
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO pet_data (uuid, active_pet_type, pet_name, level, xp, owned_pets) VALUES (?, ?, ?, ?, ?, ?) " +
                           "ON CONFLICT(uuid) DO UPDATE SET active_pet_type = excluded.active_pet_type, pet_name = excluded.pet_name, " +
                           "level = excluded.level, xp = excluded.xp, owned_pets = excluded.owned_pets;";
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

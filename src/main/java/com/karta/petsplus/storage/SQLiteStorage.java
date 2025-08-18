package com.karta.petsplus.storage;

import com.karta.petsplus.OwnedPet;
import com.karta.petsplus.PetData;
import com.karta.petsplus.PetsPlus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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
        String petDataTableQuery = "CREATE TABLE IF NOT EXISTS pet_data (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "active_pet_id VARCHAR(36)," +
                "level INT NOT NULL DEFAULT 1," +
                "xp DOUBLE NOT NULL DEFAULT 0," +
                "owned_pets TEXT NOT NULL DEFAULT ''" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(petDataTableQuery);
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
                     UUID activePetId = null;
                    if (rs.getString("active_pet_id") != null) {
                        activePetId = UUID.fromString(rs.getString("active_pet_id"));
                    }
                    int level = rs.getInt("level");
                    double xp = rs.getDouble("xp");
                    String ownedPetsRaw = rs.getString("owned_pets");
                    List<OwnedPet> ownedPets = deserializePets(ownedPetsRaw);

                    return new PetData(activePetId, level, xp, ownedPets);
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: active_pet_id")) {
                    return loadOldPlayerData(uuid).join();
                }
                e.printStackTrace();
            }
            return new PetData();
        });
    }

    private CompletableFuture<PetData> loadOldPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM pet_data WHERE uuid = ?";
             try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    List<OwnedPet> ownedPets = new ArrayList<>();
                     String ownedPetsRaw = rs.getString("owned_pets");
                    List<String> oldOwnedPets = new ArrayList<>();
                    if (ownedPetsRaw != null && !ownedPetsRaw.isEmpty()) {
                        oldOwnedPets.addAll(Arrays.asList(ownedPetsRaw.split(",")));
                    }

                    String oldActivePetType = rs.getString("active_pet_type");
                    String oldPetName = rs.getString("pet_name");
                    UUID activePetId = null;

                    for (String petType : oldOwnedPets) {
                        OwnedPet newPet = new OwnedPet(petType);
                        if (petType.equalsIgnoreCase(oldActivePetType)) {
                            if (oldPetName != null && !oldPetName.isEmpty()) {
                                newPet.setCustomName(oldPetName);
                            }
                            activePetId = newPet.getPetId();
                            oldActivePetType = null;
                        }
                        ownedPets.add(newPet);
                    }

                    int level = rs.getInt("level");
                    double xp = rs.getDouble("xp");
                    PetData migratedData = new PetData(activePetId, level, xp, ownedPets);

                    // Manually alter table for SQLite
                    try (Statement alterStmt = connection.createStatement()){
                        alterStmt.execute("ALTER TABLE pet_data RENAME TO pet_data_old;");
                        createTables(); // Create new table
                        savePlayerData(uuid, migratedData); // Save to new table
                        // Optionally copy other players over and then drop old table
                    }

                    return migratedData;
                }
             } catch (SQLException e) {
                 e.printStackTrace();
             }
             return new PetData();
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PetData data) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO pet_data (uuid, active_pet_id, level, xp, owned_pets) VALUES (?, ?, ?, ?, ?) " +
                           "ON CONFLICT(uuid) DO UPDATE SET active_pet_id = excluded.active_pet_id, " +
                           "level = excluded.level, xp = excluded.xp, owned_pets = excluded.owned_pets;";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, data.getActivePetId() != null ? data.getActivePetId().toString() : null);
                stmt.setInt(3, data.getLevel());
                stmt.setDouble(4, data.getXp());
                stmt.setString(5, serializePets(data.getOwnedPets()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private String serializePets(List<OwnedPet> pets) {
        return pets.stream()
                .map(p -> p.getPetId().toString() + ";" + p.getPetType() + ";" +
                        (p.getCustomName() != null ? Base64.getEncoder().encodeToString(p.getCustomName().getBytes()) : ""))
                .collect(Collectors.joining(","));
    }

    private List<OwnedPet> deserializePets(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(s -> {
                    String[] parts = s.split(";", 3);
                    UUID id = UUID.fromString(parts[0]);
                    String type = parts[1];
                    String name = (parts.length > 2 && !parts[2].isEmpty()) ? new String(Base64.getDecoder().decode(parts[2])) : null;
                    return new OwnedPet(type, id, name);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Set<String>> getUnlockedPets(UUID uuid) {
        return loadPlayerData(uuid).thenApply(petData ->
                petData.getOwnedPets().stream()
                        .map(OwnedPet::getPetType)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public CompletableFuture<Boolean> isPetUnlocked(UUID uuid, String petType) {
        return loadPlayerData(uuid).thenApply(petData ->
                petData.getOwnedPets().stream()
                        .anyMatch(p -> p.getPetType().equalsIgnoreCase(petType))
        );
    }

    @Override
    public CompletableFuture<Void> unlockPet(UUID uuid, String petType) {
         return loadPlayerData(uuid).thenCompose(petData -> {
            boolean alreadyOwned = petData.getOwnedPets().stream()
                    .anyMatch(p -> p.getPetType().equalsIgnoreCase(petType));
            if (!alreadyOwned) {
                petData.addOwnedPet(new OwnedPet(petType));
            }
            return savePlayerData(uuid, petData);
        });
    }
}

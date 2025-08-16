package com.karta.petsplus.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's pet.
 * This class stores all data related to a single pet instance and is serializable.
 */
public class Pet implements ConfigurationSerializable {

    private final UUID ownerUUID;
    private final String petType;
    private String petName;
    private PetStatus status;

    public enum PetStatus {
        ACTIVE,
        STORED
    }

    /**
     * Constructs a new Pet for creation purposes.
     * Status defaults to STORED.
     */
    public Pet(UUID ownerUUID, String petType, String petName) {
        this(ownerUUID, petType, petName, PetStatus.STORED);
    }

    /**
     * Constructs a Pet from deserialized data.
     */
    public Pet(UUID ownerUUID, String petType, String petName, PetStatus status) {
        this.ownerUUID = ownerUUID;
        this.petType = petType;
        this.petName = petName;
        this.status = status;
    }

    // Getters and Setters
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getPetType() { return petType; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public PetStatus getStatus() { return status; }
    public void setStatus(PetStatus status) { this.status = status; }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerUUID", ownerUUID.toString());
        map.put("petType", petType);
        map.put("petName", petName);
        map.put("status", status.name());
        return map;
    }

    @NotNull
    public static Pet deserialize(@NotNull Map<String, Object> map) {
        return new Pet(
                UUID.fromString((String) map.get("ownerUUID")),
                (String) map.get("petType"),
                (String) map.get("petName"),
                PetStatus.valueOf((String) map.get("status"))
        );
    }
}

package com.karta.petsplus.data;

import java.util.UUID;

/**
 * Represents a single pet owned by a player.
 * This class is a data model for storing pet-specific information.
 */
public class Pet {

    private final UUID owner;
    private final String petType;
    private String petName;
    private PetStatus status;

    /**
     * Constructs a new Pet instance.
     *
     * @param owner   The UUID of the player who owns this pet.
     * @param petType The type of the pet, corresponding to an entry in pets.yml.
     * @param petName The custom name of the pet.
     */
    public Pet(UUID owner, String petType, String petName) {
        this.owner = owner;
        this.petType = petType;
        this.petName = petName;
        this.status = PetStatus.STOWED; // Pets are stowed by default
    }

    public UUID getOwner() {
        return owner;
    }

    public String getPetType() {
        return petType;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public PetStatus getStatus() {
        return status;
    }

    public void setStatus(PetStatus status) {
        this.status = status;
    }

    /**
     * Enum representing the current status of a pet.
     */
    public enum PetStatus {
        SUMMONED,
        STOWED
    }
}

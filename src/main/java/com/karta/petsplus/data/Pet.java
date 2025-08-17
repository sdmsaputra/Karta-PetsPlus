package com.karta.petsplus.data;

import java.util.UUID;

public class Pet {

    private final UUID petId;
    private final UUID owner;
    private final PetType petType;
    private String petName;
    private PetStatus status;

    public Pet(UUID owner, PetType petType, String petName) {
        this.petId = UUID.randomUUID();
        this.owner = owner;
        this.petType = petType;
        this.petName = petName;
        this.status = PetStatus.STOWED;
    }

    public Pet(UUID owner, PetType petType, String petName, UUID petId) {
        this.petId = petId;
        this.owner = owner;
        this.petType = petType;
        this.petName = petName;
        this.status = PetStatus.STOWED;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getOwner() {
        return owner;
    }

    public PetType getPetType() {
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

    public enum PetStatus {
        SUMMONED,
        STOWED,
        STAY
    }
}

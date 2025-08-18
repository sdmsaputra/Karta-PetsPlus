package com.karta.petsplus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class PetData {

    private UUID activePetId;
    private int level;
    private double xp;
    private final List<OwnedPet> ownedPets;

    public PetData(UUID activePetId, int level, double xp, List<OwnedPet> ownedPets) {
        this.activePetId = activePetId;
        this.level = level;
        this.xp = xp;
        this.ownedPets = ownedPets != null ? new ArrayList<>(ownedPets) : new ArrayList<>();
    }

    // Constructor for new players
    public PetData() {
        this(null, 1, 0, new ArrayList<>());
    }

    public UUID getActivePetId() {
        return activePetId;
    }

    public void setActivePetId(UUID activePetId) {
        this.activePetId = activePetId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public List<OwnedPet> getOwnedPets() {
        return ownedPets;
    }

    public Optional<OwnedPet> getOwnedPet(UUID petId) {
        return ownedPets.stream().filter(p -> p.getPetId().equals(petId)).findFirst();
    }

    public Optional<OwnedPet> getOwnedPet(String petType) {
        return ownedPets.stream().filter(p -> p.getPetType().equals(petType)).findFirst();
    }

    public void addOwnedPet(OwnedPet pet) {
        ownedPets.add(pet);
    }
}

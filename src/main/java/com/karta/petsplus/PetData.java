package com.karta.petsplus;

import java.util.ArrayList;
import java.util.List;

public class PetData {

    private String activePetType;
    private String petName;
    private int level;
    private double xp;
    private final List<String> ownedPets;

    public PetData(String activePetType, String petName, int level, double xp, List<String> ownedPets) {
        this.activePetType = activePetType;
        this.petName = petName;
        this.level = level;
        this.xp = xp;
        this.ownedPets = ownedPets != null ? new ArrayList<>(ownedPets) : new ArrayList<>();
    }

    public String getActivePetType() {
        return activePetType;
    }

    public void setActivePetType(String activePetType) {
        this.activePetType = activePetType;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
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

    public List<String> getOwnedPets() {
        return ownedPets;
    }
}

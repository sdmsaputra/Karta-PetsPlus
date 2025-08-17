package com.karta.petsplus;

public class PetData {

    private String activePetType;
    private String petName;
    private int level;
    private double xp;

    public PetData(String activePetType, String petName, int level, double xp) {
        this.activePetType = activePetType;
        this.petName = petName;
        this.level = level;
        this.xp = xp;
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
}

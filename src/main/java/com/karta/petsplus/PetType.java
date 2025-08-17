package com.karta.petsplus;

import org.bukkit.entity.EntityType;

public class PetType {

    private final String internalName;
    private final String displayName;
    private final EntityType entityType;
    private final double price;
    private final boolean isBaby;

    public PetType(String internalName, String displayName, EntityType entityType, double price, boolean isBaby) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.entityType = entityType;
        this.price = price;
        this.isBaby = isBaby;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBaby() {
        return isBaby;
    }
}

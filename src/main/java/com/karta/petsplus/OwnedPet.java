package com.karta.petsplus;

import java.util.UUID;

public class OwnedPet {

    private final String petType;
    private final UUID petId;
    private String customName;

    public OwnedPet(String petType, UUID petId, String customName) {
        this.petType = petType;
        this.petId = petId;
        this.customName = customName;
    }

    public OwnedPet(String petType) {
        this(petType, UUID.randomUUID(), null);
    }

    public String getPetType() {
        return petType;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getDisplayName(PetsPlus plugin) {
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        PetType type = plugin.getConfigManager().getPetType(petType);
        return type != null ? type.getDisplayName() : "Unknown Pet";
    }
}

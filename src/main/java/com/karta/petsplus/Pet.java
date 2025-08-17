package com.karta.petsplus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents an active, spawned pet in the world.
 */
public class Pet {

    private final UUID owner;
    private final PetType petType;
    private Entity entity;
    private String petName;

    public Pet(UUID owner, PetType petType, String petName, Entity entity) {
        this.owner = owner;
        this.petType = petType;
        this.petName = petName;
        this.entity = entity;
    }

    public UUID getOwner() {
        return owner;
    }

    public Player getOwnerPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public PetType getPetType() {
        return petType;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
        if (this.entity != null && !this.entity.isDead()) {
            this.entity.setCustomName(petName);
            this.entity.setCustomNameVisible(true);
        }
    }
}

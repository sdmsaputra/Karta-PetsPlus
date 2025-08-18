package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    void init();

    void shutdown();

    CompletableFuture<PetData> loadPlayerData(UUID uuid);

    CompletableFuture<Void> savePlayerData(UUID uuid, PetData data);

    CompletableFuture<Set<String>> getUnlockedPets(UUID uuid);

    CompletableFuture<Boolean> isPetUnlocked(UUID uuid, String petType);

    CompletableFuture<Void> unlockPet(UUID uuid, String petType);

    void createTables();
}

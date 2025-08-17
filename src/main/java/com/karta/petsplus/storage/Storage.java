package com.karta.petsplus.storage;

import com.karta.petsplus.PetData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    void init();

    void shutdown();

    CompletableFuture<PetData> loadPlayerData(UUID uuid);

    CompletableFuture<Void> savePlayerData(UUID uuid, PetData data);

    void createTables();
}

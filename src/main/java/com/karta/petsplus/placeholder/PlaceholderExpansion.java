package com.karta.petsplus.placeholder;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    private final KartaPetsPlus plugin;

    public PlaceholderExpansion(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kartapetsplus";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jules";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1-SNAPSHOT";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "";
        }

        Optional<Pet> activePetOpt = plugin.getPlayerDataManager().getPets(player.getPlayer()).stream()
                .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED)
                .findFirst();

        if (params.equalsIgnoreCase("has_active_pet")) {
            return activePetOpt.isPresent() ? "yes" : "no";
        }

        if (activePetOpt.isPresent()) {
            Pet activePet = activePetOpt.get();
            switch (params.toLowerCase()) {
                case "pet_name":
                    return activePet.getPetName();
                case "pet_type":
                    return activePet.getPetType();
                case "pet_status":
                    return activePet.getStatus().name();
            }
        }

        return "No active pet";
    }
}

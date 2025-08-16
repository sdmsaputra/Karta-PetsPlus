package com.karta.petsplus.placeholder;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.ConfigManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    private final KartaPetsPlus plugin;
    private final ConfigManager configManager;

    public PlaceholderExpansion(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
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
        if (player == null) {
            return "";
        }

        // Handle total pet count (works for online players)
        if (params.equalsIgnoreCase("pet_count")) {
            if (player.isOnline()) {
                return String.valueOf(plugin.getPlayerDataManager().getPets(player.getPlayer()).size());
            }
            return "0"; // Cannot get pet count for offline players with current setup
        }

        // The rest of the placeholders require an online player with an active pet
        if (!player.isOnline()) {
            return "";
        }

        Optional<Pet> activePetOpt = plugin.getPlayerDataManager().getPets(player.getPlayer()).stream()
                .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED)
                .findFirst();

        if (params.equalsIgnoreCase("has_active_pet")) {
            return activePetOpt.isPresent() ? "yes" : "no";
        }

        // Placeholders that depend on an active pet
        if (activePetOpt.isPresent()) {
            Pet activePet = activePetOpt.get();
            switch (params.toLowerCase()) {
                // New, more descriptive placeholders
                case "active_pet_name":
                case "pet_name": // Keep old for backwards compatibility
                    return activePet.getPetName();
                case "active_pet_type":
                case "pet_type": // Keep old for backwards compatibility
                    // Get the display name from pets.yml for consistency
                    return configManager.getPets().getString("pets." + activePet.getPetType() + ".display-name", activePet.getPetType());
                case "active_pet_status":
                case "pet_status": // Keep old for backwards compatibility
                    return activePet.getStatus().name();
            }
        }

        // If an active-pet-related placeholder was requested, but there's no active pet, return a default value.
        String defaultValue = "No active pet"; // TODO: Make this configurable
        if (params.startsWith("active_pet_") || params.startsWith("pet_")) {
            return defaultValue;
        }

        // Return null for any other unrecognised placeholder, as per PAPI guidelines
        return null;
    }
}

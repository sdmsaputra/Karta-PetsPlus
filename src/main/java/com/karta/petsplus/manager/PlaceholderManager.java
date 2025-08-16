package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the integration with PlaceholderAPI by providing custom placeholders.
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private final KartaPetsPlus plugin;

    public PlaceholderManager(KartaPetsPlus plugin) {
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
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // We want to update placeholders on-the-fly
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %kartapetsplus_pet_count%
        if (params.equalsIgnoreCase("pet_count")) {
            return String.valueOf(plugin.getPlayerDataManager().getPets(player).size());
        }

        return null; // Placeholder not found
    }
}

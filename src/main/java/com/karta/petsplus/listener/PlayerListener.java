package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player events to manage their pet data.
 * This class handles loading data on join and saving on quit.
 */
public class PlayerListener implements Listener {

    private final KartaPetsPlus plugin;

    public PlayerListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Asynchronously load player data to avoid blocking the main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPlayerDataManager().loadPlayerPets(event.getPlayer());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Despawn any active pets
        plugin.getPetManager().handlePlayerQuit(event.getPlayer());

        // Save player data and clear from cache
        plugin.getPlayerDataManager().savePlayerPets(event.getPlayer());
    }
}

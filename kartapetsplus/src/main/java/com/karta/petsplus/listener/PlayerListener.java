package com.karta.petsplus.listener;

import com.karta.petsplus.manager.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player events to manage their data.
 */
public class PlayerListener implements Listener {

    private final DataManager dataManager;

    /**
     * Constructs a new PlayerListener.
     *
     * @param dataManager The DataManager instance.
     */
    public PlayerListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.loadPlayerData(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dataManager.savePlayerData(player);
        dataManager.uncachePlayerData(player);
    }
}

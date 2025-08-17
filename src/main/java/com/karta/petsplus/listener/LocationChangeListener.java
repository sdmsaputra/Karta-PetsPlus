package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LocationChangeListener implements Listener {

    private final KartaPetsPlus plugin;

    public LocationChangeListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!plugin.getConfigManager().isRemovePetOnWorldChange()) {
            return;
        }
        plugin.getPetManager().getActivePet(event.getPlayer()).ifPresent(pet -> {
            plugin.getPetManager().despawnPet(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getPetManager().summonPet(event.getPlayer(), pet);
            }, 40);
        });
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            return;
        }
        if (!plugin.getConfigManager().isRemovePetOnWorldChange()) {
            return;
        }
        plugin.getPetManager().getActivePet(event.getPlayer()).ifPresent(pet -> {
            plugin.getPetManager().despawnPet(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getPetManager().summonPet(event.getPlayer(), pet);
            }, 40);
        });
    }
}

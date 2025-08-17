package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class PetSpawnListener implements Listener {

    private final KartaPetsPlus plugin;

    public PetSpawnListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity e = event.getEntity();
        if (!plugin.getPetManager().isPet(e)) {
            return;
        }
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(EntitySpawnEvent event) {
        Entity e = event.getEntity();
        if (!plugin.getPetManager().isPet(e)) {
            return;
        }
        event.setCancelled(false);
    }
}

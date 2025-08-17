package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class EntityListener implements Listener {

    private final KartaPetsPlus plugin;

    public EntityListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        // Check if the damager or victim is a pet
        boolean isDamagerPet = plugin.getPetManager().isPet(damager);
        boolean isVictimPet = plugin.getPetManager().isPet(victim);

        // Prevent pet vs pet combat
        if (isDamagerPet && isVictimPet) {
            event.setCancelled(true);
            return;
        }

        // Prevent pets from attacking their owners
        if (isDamagerPet && victim instanceof Player) {
            Player owner = plugin.getPetManager().getPetOwner(damager);
            if (owner != null && owner.getUniqueId().equals(victim.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}

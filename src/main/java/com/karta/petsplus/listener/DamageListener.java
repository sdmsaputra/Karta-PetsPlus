package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    private final KartaPetsPlus plugin;

    public DamageListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (!plugin.getPetManager().isPet(vehicle)) {
            return;
        }

        if (plugin.getConfigManager().isCancelFallDamageWhenRidingPet()) {
            event.setCancelled(true);
        }
    }
}

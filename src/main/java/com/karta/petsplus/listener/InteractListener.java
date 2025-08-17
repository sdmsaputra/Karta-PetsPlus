package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.ui.PetInteractionGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InteractListener implements Listener {

    private final KartaPetsPlus plugin;

    public InteractListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isPetInteractionGuiEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        plugin.getPetManager().getActivePet(player).ifPresent(pet -> {
            if (event.getRightClicked().getUniqueId().equals(plugin.getPetManager().getActivePetEntity(player).getUniqueId())) {
                event.setCancelled(true);
                PetInteractionGUI.open(plugin, player, pet);
            }
        });
    }
}

package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.PetManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
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
        Player player = event.getPlayer();
        PetManager petManager = plugin.getPetManager();
        Entity clickedEntity = event.getRightClicked();

        // Check if the clicked entity is a pet managed by the plugin
        if (!petManager.isPet(clickedEntity)) {
            return;
        }

        // Check if the pet belongs to the player who interacted
        Entity activePetEntity = petManager.getActivePetEntity(player);
        if (activePetEntity == null || !activePetEntity.getUniqueId().equals(clickedEntity.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        petManager.getActivePet(player).ifPresent(pet -> {
            if (pet.getStatus() == Pet.PetStatus.SUMMONED) {
                // Command the pet to sit
                pet.setStatus(Pet.PetStatus.STAY);
                if (activePetEntity instanceof Mob) {
                    ((Mob) activePetEntity).setAI(false); // Immediately stop AI to make it sit
                }
                plugin.getMessageManager().sendMessage(player, "pet-now-sitting", "<green>Your pet is now sitting.</green>");
            } else if (pet.getStatus() == Pet.PetStatus.STAY) {
                // Command the pet to follow
                pet.setStatus(Pet.PetStatus.SUMMONED);
                if (activePetEntity instanceof Mob) {
                    ((Mob) activePetEntity).setAI(true); // Re-enable AI
                }
                plugin.getMessageManager().sendMessage(player, "pet-now-following", "<green>Your pet is now following you.</green>");
            }
            plugin.getPlayerDataManager().savePlayerPet(player, pet);
        });
    }
}

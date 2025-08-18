package com.karta.petsplus.listeners;

import com.karta.petsplus.Pet;
import com.karta.petsplus.PetManager;
import com.karta.petsplus.PetsPlus;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class PetListener implements Listener {

    private final PetManager petManager;

    public PetListener(PetsPlus plugin) {
        this.petManager = plugin.getPetManager();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;

        // Check if the targeting entity is an active pet
        for (Pet pet : petManager.getActivePets().values()) {
            if (pet.getEntity().equals(event.getEntity())) {
                // It's a pet. Now check if it's targeting its owner.
                if (pet.getOwnerPlayer() != null && pet.getOwnerPlayer().equals(event.getTarget())) {
                    event.setCancelled(true);
                }
                return; // We found the pet, no need to check further.
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        // Owner attacks something, pet should attack it too
        if (event.getDamager() instanceof Player) {
            Player owner = (Player) event.getDamager();
            Pet pet = petManager.getActivePet(owner);
            if (pet != null && pet.getEntity() instanceof Mob && event.getEntity() instanceof LivingEntity) {
                Mob petMob = (Mob) pet.getEntity();
                LivingEntity target = (LivingEntity) event.getEntity();
                if (target != petMob && target != owner) { // Don't make pet attack itself or its owner
                    petMob.setTarget(target);
                }
            }
        }

        // Something attacks owner, pet should retaliate
        if (event.getEntity() instanceof Player) {
            Player owner = (Player) event.getEntity();
            Pet pet = petManager.getActivePet(owner);
            if (pet != null && pet.getEntity() instanceof Mob && event.getDamager() instanceof LivingEntity) {
                Mob petMob = (Mob) pet.getEntity();
                LivingEntity attacker = (LivingEntity) event.getDamager();
                 if (attacker != petMob) { // Don't make pet attack itself
                    petMob.setTarget(attacker);
                }
            }
        }
    }
}

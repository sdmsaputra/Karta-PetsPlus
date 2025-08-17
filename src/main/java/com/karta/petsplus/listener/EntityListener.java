package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListener implements Listener {

    private final KartaPetsPlus plugin;

    public EntityListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPetDamage(EntityDamageEvent event) {
        if (!plugin.getPetManager().isPet(event.getEntity())) {
            return; // Not a pet, we don't care
        }

        ConfigManager.DamagePolicy policy = plugin.getConfigManager().getDamagePolicy();

        switch (policy) {
            case INVULNERABLE:
                event.setCancelled(true);
                break;
            case OWNER_ONLY:
                handleOwnerOnlyDamage(event);
                break;
            case ALL:
                // Allow all damage
                break;
        }
    }

    private void handleOwnerOnlyDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            // Allow environmental damage (e.g., fall, fire) but cancel if it's not from an entity
            // To prevent things like suffocation damage being cancellable by non-owners
            // We let it pass, a better implementation might block specific causes.
            // For now, only entity-caused damage is owner-restricted.
            return;
        }

        EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
        Entity damager = entityEvent.getDamager();
        Player petOwner = plugin.getPetManager().getPetOwner(event.getEntity());

        if (petOwner == null) {
            event.setCancelled(true); // Should not happen, but safety first
            return;
        }

        // Check for projectile source
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();
                if (!shooter.getUniqueId().equals(petOwner.getUniqueId())) {
                    event.setCancelled(true); // Not the owner
                }
            } else {
                event.setCancelled(true); // Damaged by non-player projectile (e.g., Skeleton)
            }
        } else if (damager instanceof Player) {
            // Direct damage from a player
            if (!damager.getUniqueId().equals(petOwner.getUniqueId())) {
                event.setCancelled(true); // Not the owner
            }
        } else {
            // Damaged by another mob (e.g., Zombie)
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPetAttack(EntityDamageByEntityEvent event) {
        // Prevent pets from attacking their owners
        if (plugin.getPetManager().isPet(event.getDamager())) {
            Entity victim = event.getEntity();
            if (victim instanceof Player) {
                Player owner = plugin.getPetManager().getPetOwner(event.getDamager());
                if (owner != null && owner.getUniqueId().equals(victim.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

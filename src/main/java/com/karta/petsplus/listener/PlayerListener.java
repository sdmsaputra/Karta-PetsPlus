package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final KartaPetsPlus plugin;

    public PlayerListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getStorageManager().loadPlayerPets(player);

            if (plugin.getConfigManager().isAutoRespawnOnJoin()) {
                Optional<Pet> petToSummon = plugin.getStorageManager().getPets(player).stream()
                    .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED || p.getStatus() == Pet.PetStatus.STAY)
                    .findFirst();

                petToSummon.ifPresent(pet -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getPetManager().summonPet(player, pet);
                }));
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPetManager().handlePlayerQuit(event.getPlayer());
        plugin.getStorageManager().savePlayerPets(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.getPetManager().despawnPet(event.getEntity());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (plugin.getConfigManager().isAutoRespawnOnDeath()) {
            Player player = event.getPlayer();
            Optional<Pet> petToSummon = plugin.getStorageManager().getPets(player).stream()
                .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED || p.getStatus() == Pet.PetStatus.STAY)
                .findFirst();

            petToSummon.ifPresent(pet -> plugin.getPetManager().summonPet(player, pet));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Entity petEntity = plugin.getPetManager().getActivePetEntity(player);

        if (petEntity != null) {
            // Check world blacklist
            if (plugin.getConfigManager().getBlacklistedWorlds().contains(player.getWorld().getName())) {
                plugin.getPetManager().stowPet(player);
                plugin.getMessageManager().sendMessage(player, "pet-despawned-world");
                return;
            }

            // Teleport pet to the new world
            plugin.getPetManager().teleportPetToPlayer(petEntity, player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // We only care about large teleports, as small ones are handled by the regular tick
        if (event.getFrom().distanceSquared(event.getTo()) > 4) { // 2 blocks
            Entity petEntity = plugin.getPetManager().getActivePetEntity(event.getPlayer());
            if (petEntity != null) {
                plugin.getPetManager().teleportPetToPlayer(petEntity, event.getPlayer());
            }
        }
    }
}

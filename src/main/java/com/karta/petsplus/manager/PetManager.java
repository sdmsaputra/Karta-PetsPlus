package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PetManager {

    private final KartaPetsPlus plugin;
    private final Map<UUID, UUID> activePets = new ConcurrentHashMap<>(); // Player UUID -> Pet Entity UUID
    private BukkitTask behaviorTask;

    public PetManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        startBehaviorScheduler();
    }

    public void summonPet(Player player, Pet pet) {
        if (plugin.getConfigManager().getBlacklistedWorlds().contains(player.getWorld().getName())) {
            plugin.getMessageManager().sendMessage(player, "cannot-summon-in-world");
            return;
        }
        if (activePets.size() >= plugin.getConfigManager().getMaxActivePetsPerPlayer() && !player.hasPermission("kartapetsplus.bypass.limit")) {
            plugin.getMessageManager().sendMessage(player, "pet-limit-reached");
            return;
        }
        if (isPetActive(player)) {
            despawnPet(player);
        }

        String petTypeStr = plugin.getConfigManager().getPets().getString("pets." + pet.getPetType() + ".entity-type");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(petTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logDebug("Invalid entity type for pet " + pet.getPetType() + ": " + petTypeStr);
            return;
        }

        findSafeLocation(player.getLocation()).thenAccept(spawnLoc -> {
            Entity spawnedPet = player.getWorld().spawnEntity(spawnLoc, entityType);
            spawnedPet.customName(MiniMessage.miniMessage().deserialize(pet.getPetName()));
            spawnedPet.setCustomNameVisible(true);

            if (spawnedPet instanceof Tameable) {
                ((Tameable) spawnedPet).setOwner(player);
                ((Tameable) spawnedPet).setTamed(true);
            }

            if (spawnedPet instanceof Mob) {
                ((Mob) spawnedPet).setPersistent(false);
            }

            activePets.put(player.getUniqueId(), spawnedPet.getUniqueId());
            pet.setStatus(Pet.PetStatus.SUMMONED);
            plugin.getStorageManager().savePlayerPet(player, pet);
            plugin.getMessageManager().sendMessage(player, "pet-summoned", Map.of("pet_name", pet.getPetName()));
            logDebug("Summoned pet " + pet.getPetType() + " for player " + player.getName());
        });
    }

    public void despawnPet(Player player) {
        UUID petEntityId = activePets.remove(player.getUniqueId());
        if (petEntityId != null) {
            Entity petEntity = Bukkit.getEntity(petEntityId);
            if (petEntity != null) {
                petEntity.remove();
                logDebug("Despawned pet for player " + player.getName());
            }
        }
    }

    public void stowPet(Player player) {
        despawnPet(player);
        getActivePet(player).ifPresent(pet -> {
            pet.setStatus(Pet.PetStatus.STOWED);
            plugin.getStorageManager().savePlayerPet(player, pet);
            plugin.getMessageManager().sendMessage(player, "pet-stowed", Map.of("pet_name", pet.getPetName()));
        });
    }

    public void handlePlayerQuit(Player player) {
        if (isPetActive(player)) {
            despawnPet(player);
        }
    }

    public void shutdown() {
        stopBehaviorScheduler();
        Bukkit.getOnlinePlayers().forEach(this::despawnPet);
        activePets.clear();
        logDebug("PetManager shutdown complete. All pets despawned.");
    }

    public boolean isPetActive(Player player) {
        return activePets.containsKey(player.getUniqueId());
    }

    public Entity getActivePetEntity(Player player) {
        UUID petEntityId = activePets.get(player.getUniqueId());
        return petEntityId != null ? Bukkit.getEntity(petEntityId) : null;
    }

    public boolean isPet(Entity entity) {
        return activePets.containsValue(entity.getUniqueId());
    }

    public Optional<Pet> getActivePet(Player player) {
        return plugin.getStorageManager().getPets(player).stream()
            .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED || p.getStatus() == Pet.PetStatus.STAY)
            .findFirst();
    }

    public Player getPetOwner(Entity entity) {
        UUID petId = entity.getUniqueId();
        for (Map.Entry<UUID, UUID> entry : activePets.entrySet()) {
            if (entry.getValue().equals(petId)) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null;
    }

    private void startBehaviorScheduler() {
        if (behaviorTask != null) behaviorTask.cancel();
        long ticks = plugin.getConfigManager().getBehaviorTicks();
        behaviorTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, ticks);
        logDebug("Pet behavior scheduler started with a period of " + ticks + " ticks.");
    }

    private void stopBehaviorScheduler() {
        if (behaviorTask != null) {
            behaviorTask.cancel();
            behaviorTask = null;
            logDebug("Pet behavior scheduler stopped.");
        }
    }

    private void tick() {
        double teleportDistSq = Math.pow(plugin.getConfigManager().getTeleportDistance(), 2);
        for (UUID ownerId : activePets.keySet()) {
            Player owner = Bukkit.getPlayer(ownerId);
            Entity petEntity = getActivePetEntity(owner);

            if (owner == null || !owner.isOnline() || petEntity == null || !petEntity.isValid()) {
                cleanupPet(ownerId);
                continue;
            }

            getActivePet(owner).ifPresentOrElse(petData -> {
                if (petData.getStatus() == Pet.PetStatus.SUMMONED) {
                    handleFollowing(owner, petEntity, teleportDistSq);
                } else if (petData.getStatus() == Pet.PetStatus.STAY) {
                     if (petEntity instanceof Mob) ((Mob) petEntity).getPathfinder().stop();
                }
            }, () -> cleanupPet(ownerId));
        }
    }

    private void handleFollowing(Player owner, Entity petEntity, double teleportDistSq) {
        if (!petEntity.getWorld().equals(owner.getWorld())) {
            logDebug("Pet for " + owner.getName() + " is in a different world. Teleporting.");
            teleportPetToPlayer(petEntity, owner);
            return;
        }
        double distanceSq = owner.getLocation().distanceSquared(petEntity.getLocation());
        if (distanceSq > teleportDistSq) {
            logDebug("Pet for " + owner.getName() + " is too far (" + Math.sqrt(distanceSq) + " blocks). Teleporting.");
            teleportPetToPlayer(petEntity, owner);
        } else if (petEntity instanceof Mob mob) {
            mob.getPathfinder().moveTo(owner, 1.2);
        }
    }

    private void cleanupPet(UUID ownerId) {
        activePets.remove(ownerId);
        Player player = Bukkit.getPlayer(ownerId);
        if (player != null) {
            despawnPet(player);
        }
        logDebug("Cleaned up orphaned pet data for player UUID: " + ownerId);
    }

    public void teleportPetToPlayer(Entity pet, Player owner) {
        findSafeLocation(owner.getLocation()).thenAccept(pet::teleportAsync);
        logDebug("Teleported pet for " + owner.getName());
    }

    private CompletableFuture<Location> findSafeLocation(Location center) {
        return CompletableFuture.supplyAsync(() -> {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Location checkLoc = center.clone().add(x, 0, z);
                    Block ground = checkLoc.getBlock();
                    Block body = checkLoc.clone().add(0, 1, 0).getBlock();
                    Block head = checkLoc.clone().add(0, 2, 0).getBlock();
                    if (!ground.getType().isSolid() && body.getType() == Material.AIR && head.getType() == Material.AIR) {
                        return body.getLocation();
                    }
                }
            }
            return center; // Fallback to original location
        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    private void logDebug(String message) {
        if (plugin.getConfigManager().isDebugLogging()) {
            plugin.getLogger().log(Level.INFO, "[Debug] " + message);
        }
    }
}

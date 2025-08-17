package com.karta.petsplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PetManager implements Listener {

    private final PetsPlus plugin;
    private final Map<UUID, Pet> activePets = new ConcurrentHashMap<>();
    private final Map<UUID, PetData> playerDataCache = new ConcurrentHashMap<>();
    private BukkitTask followTask;

    public PetManager(PetsPlus plugin) {
        this.plugin = plugin;
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startFollowTask();
    }

    public void shutdown() {
        if (followTask != null) {
            followTask.cancel();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            dismissPet(player, true); // silent dismiss
            savePlayerData(player.getUniqueId());
        }
        activePets.clear();
        playerDataCache.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        dismissPet(player, true);
        savePlayerData(playerUuid);
        playerDataCache.remove(playerUuid);
    }

    private void loadPlayerData(UUID uuid) {
        plugin.getStorageManager().getStorage().loadPlayerData(uuid).thenAccept(petData -> {
            playerDataCache.put(uuid, petData);
            if (petData.getActivePetType() != null && !petData.getActivePetType().equalsIgnoreCase("none")) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> summonPet(player, petData.getActivePetType()));
                }
            }
        });
    }

    private void savePlayerData(UUID uuid) {
        PetData data = playerDataCache.get(uuid);
        if (data != null) {
            if (!activePets.containsKey(uuid)) {
                data.setActivePetType(null);
            }
            plugin.getStorageManager().getStorage().savePlayerData(uuid, data);
        }
    }

    public void summonPet(Player player, String type) {
        if (getActivePet(player) != null) {
            dismissPet(player, true); // Be silent, a new pet is coming
        }

        PetType petType = plugin.getConfigManager().getPetType(type);
        if (petType == null) {
            player.sendMessage(ChatColor.RED + "Pet type '" + type + "' not found.");
            return;
        }

        if (!player.hasPermission("petsplus.summon." + petType.getInternalName()) && !player.hasPermission("petsplus.summon.*")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to summon this pet.");
            return;
        }

        Location spawnLocation = player.getLocation();
        Entity entity = player.getWorld().spawnEntity(spawnLocation, petType.getEntityType());

        if (entity instanceof Ageable && petType.isBaby()) {
            ((Ageable) entity).setBaby();
        }

        if (entity instanceof Tameable) {
            ((Tameable) entity).setOwner(player);
        }

        PetData data = getPlayerData(player);
        String petName = data.getPetName();
        if (petName == null || petName.isEmpty()) {
            petName = petType.getDisplayName();
        }

        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', petName));
        entity.setCustomNameVisible(true);
        entity.setPersistent(false);

        Pet pet = new Pet(player.getUniqueId(), petType, petName, entity);
        activePets.put(player.getUniqueId(), pet);
        data.setActivePetType(petType.getInternalName());

        player.sendMessage(ChatColor.GREEN + "You have summoned your pet: " + pet.getPetName());
    }

    public void dismissPet(Player player, boolean silent) {
        Pet pet = getActivePet(player);
        if (pet != null) {
            if (pet.getEntity() != null && !pet.getEntity().isDead()) {
                pet.getEntity().remove();
            }
            activePets.remove(player.getUniqueId());
            getPlayerData(player).setActivePetType(null);
            if (!silent) {
                player.sendMessage(ChatColor.YELLOW + "Your pet has been dismissed.");
            }
        } else {
            if (!silent) {
                player.sendMessage(ChatColor.RED + "You don't have an active pet.");
            }
        }
    }

    public void renamePet(Player player, String name) {
        Pet pet = getActivePet(player);
        if (pet == null) {
            player.sendMessage(ChatColor.RED + "You don't have an active pet to rename.");
            return;
        }
        String coloredName = ChatColor.translateAlternateColorCodes('&', name);
        pet.setPetName(coloredName);
        getPlayerData(player).setPetName(coloredName);
        player.sendMessage(ChatColor.GREEN + "Your pet has been renamed to " + coloredName);
    }

    public Pet getActivePet(Player player) {
        return activePets.get(player.getUniqueId());
    }

    public PetData getPlayerData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }

    private void startFollowTask() {
        int ticks = plugin.getConfigManager().getFollowTaskTicks();
        followTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Pet pet : activePets.values()) {
                Player owner = pet.getOwnerPlayer();
                Entity petEntity = pet.getEntity();

                if (owner == null || !owner.isOnline() || petEntity == null || petEntity.isDead()) {
                    continue;
                }

                if (!owner.getWorld().equals(petEntity.getWorld())) {
                    petEntity.teleport(owner.getLocation());
                    continue;
                }

                double distanceSq = owner.getLocation().distanceSquared(petEntity.getLocation());

                if (distanceSq > 20 * 20) {
                    petEntity.teleport(owner.getLocation());
                } else if (distanceSq > 5 * 5) {
                    if (petEntity instanceof Mob) {
                        ((Mob) petEntity).getPathfinder().moveTo(owner.getLocation(), 1.2);
                    }
                } else {
                     if (petEntity instanceof Mob) {
                        if (((Mob) petEntity).getPathfinder().hasPath()) {
                             ((Mob) petEntity).getPathfinder().stopPathfinding();
                        }
                    }
                }
            }
        }, 0L, ticks);
    }
}

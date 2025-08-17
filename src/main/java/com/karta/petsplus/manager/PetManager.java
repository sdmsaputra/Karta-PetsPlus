package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetManager {

    private final KartaPetsPlus plugin;
    private final Map<UUID, UUID> activePets = new HashMap<>(); // Player UUID -> Pet Entity UUID

    public PetManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    public void summonPet(Player player, Pet pet) {
        if (activePets.containsKey(player.getUniqueId())) {
            despawnPet(player);
        }

        String petTypeStr = plugin.getConfigManager().getPets().getString("pets." + pet.getPetType() + ".entity-type");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(petTypeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid entity type for pet " + pet.getPetType() + ": " + petTypeStr);
            return;
        }

        Entity spawnedPet = player.getWorld().spawnEntity(player.getLocation(), entityType);
        spawnedPet.setCustomName(MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(pet.getPetName())));
        spawnedPet.setCustomNameVisible(true);

        if (spawnedPet instanceof Tameable) {
            Tameable tameable = (Tameable) spawnedPet;
            tameable.setOwner(player);
            tameable.setTamed(true);
        }

        activePets.put(player.getUniqueId(), spawnedPet.getUniqueId());
        pet.setStatus(Pet.PetStatus.SUMMONED);
        plugin.getMessageManager().sendMessage(player, "pet-summoned", "<green>Your pet <pet_name> has been summoned!</green>", Placeholder.parsed("pet_name", pet.getPetName()));
    }

    public void despawnPet(Player player) {
        UUID petEntityId = activePets.remove(player.getUniqueId());
        if (petEntityId != null) {
            Entity petEntity = Bukkit.getEntity(petEntityId);
            if (petEntity != null) {
                petEntity.remove();
            }
        }

        plugin.getPlayerDataManager().getPets(player).stream()
            .filter(p -> p.getStatus() == Pet.PetStatus.SUMMONED)
            .findFirst()
            .ifPresent(pet -> {
                pet.setStatus(Pet.PetStatus.STOWED);
                plugin.getMessageManager().sendMessage(player, "pet-stowed", "<gray>Your pet <pet_name> has been put away.</gray>", Placeholder.parsed("pet_name", pet.getPetName()));
            });
    }

    public boolean isPetActive(Player player) {
        return activePets.containsKey(player.getUniqueId());
    }

    public void handlePlayerQuit(Player player) {
        if (isPetActive(player)) {
            despawnPet(player);
        }
    }

    public boolean isPet(Entity entity) {
        return activePets.containsValue(entity.getUniqueId());
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
}

package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the creation and management of the Pet Management GUI.
 * This menu allows players to view, summon, and manage their owned pets.
 */
public class PetMenuGui {

    private final KartaPetsPlus plugin;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final Player player;

    public PetMenuGui(KartaPetsPlus plugin, Player player) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.configManager = plugin.getConfigManager();
        this.player = player;
    }

    /**
     * Creates and opens the pet management inventory for the player.
     */
    public void open() {
        List<Pet> playerPets = playerDataManager.getPets(player);

        if (playerPets.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "no-pets-owned", "<gray>You do not own any pets yet. Visit the <aqua>/petshop</aqua> to buy one!</gray>");
            return;
        }

        // Get GUI configuration from gui.yml
        String title = plugin.getGuiManager().getGuiConfig().getString("pet-menu.title", "My Pets");
        int size = plugin.getGuiManager().getGuiConfig().getInt("pet-menu.size", 54);

        Inventory gui = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(title));

        // Fill empty slots if enabled
        if (plugin.getGuiManager().getGuiConfig().getBoolean("pet-menu.fill-item.enabled", false)) {
            String materialName = plugin.getGuiManager().getGuiConfig().getString("pet-menu.fill-item.material", "GRAY_STAINED_GLASS_PANE");
            Material fillMaterial = Material.getMaterial(materialName);
            if (fillMaterial != null) {
                ItemStack fillItem = new ItemStack(fillMaterial);
                ItemMeta meta = fillItem.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(" "));
                    fillItem.setItemMeta(meta);
                }
                for (int i = 0; i < size; i++) {
                    gui.setItem(i, fillItem);
                }
            }
        }

        List<Integer> petSlots = plugin.getGuiManager().getGuiConfig().getIntegerList("pet-menu.slots");

        // Iterate through player's pets and place them in the specified slots
        for (int i = 0; i < playerPets.size(); i++) {
            if (i >= petSlots.size()) {
                plugin.getLogger().warning("Not enough slots defined in gui.yml for player " + player.getName() + "'s pets. Some pets will not be displayed.");
                break;
            }

            Pet pet = playerPets.get(i);
            int slot = petSlots.get(i);

            if (slot >= 0 && slot < size) {
                gui.setItem(slot, createPetItem(pet));
            } else {
                plugin.getLogger().warning("Invalid slot " + slot + " defined in gui.yml for PetMenuGui.");
            }
        }

        player.openInventory(gui);
    }

    /**
     * Creates an ItemStack representing a pet for the GUI.
     * @param pet The pet to create an item for.
     * @return The created ItemStack.
     */
    private ItemStack createPetItem(Pet pet) {
        String petType = pet.getPetType();
        // Get icon from pets.yml, default to a barrier if not found
        String iconMaterialName = configManager.getPets().getString("pets." + petType + ".icon", "BARRIER").toUpperCase();
        Material iconMaterial = Material.getMaterial(iconMaterialName);
        if (iconMaterial == null) {
            iconMaterial = Material.BARRIER;
        }

        ItemStack petItem = new ItemStack(iconMaterial);
        ItemMeta meta = petItem.getItemMeta();

        if (meta != null) {
            // Set the display name to the pet's custom name
            meta.displayName(MiniMessage.miniMessage().deserialize(pet.getPetName()));

            // Create the lore
            List<Component> lore = new ArrayList<>();
            // TODO: Make lore format configurable
            String petTypeName = configManager.getPets().getString("pets." + petType + ".display-name", petType);
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Type: " + petTypeName));
            lore.add(Component.text("")); // Spacer
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Status: <yellow>" + pet.getStatus().name() + "</yellow>"));
            lore.add(Component.text("")); // Spacer
            lore.add(MiniMessage.miniMessage().deserialize("<aqua>Left-click to summon/stow.</aqua>"));
            lore.add(MiniMessage.miniMessage().deserialize("<aqua>Right-click to rename.</aqua>"));

            meta.lore(lore);
            petItem.setItemMeta(meta);
        }

        return petItem;
    }
}

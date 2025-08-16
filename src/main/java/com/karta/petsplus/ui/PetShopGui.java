package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import com.karta.petsplus.manager.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the creation and management of the Pet Shop GUI.
 * This menu allows players to browse and purchase new pets.
 */
public class PetShopGui {

    private final KartaPetsPlus plugin;
    private final ConfigManager configManager;
    private final EconomyManager economyManager;
    private final Player player;

    public PetShopGui(KartaPetsPlus plugin, Player player) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.economyManager = plugin.getEconomyManager();
        this.player = player;
    }

    /**
     * Creates and opens the pet shop inventory for the player.
     */
    public void open() {
        // Get GUI configuration from gui.yml
        String title = plugin.getGuiManager().getGuiConfig().getString("pet-shop.title", "Pet Shop");
        int size = plugin.getGuiManager().getGuiConfig().getInt("pet-shop.size", 54);

        Inventory gui = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(title));

        // Fill empty slots if enabled
        if (plugin.getGuiManager().getGuiConfig().getBoolean("pet-shop.fill-item.enabled", false)) {
            String materialName = plugin.getGuiManager().getGuiConfig().getString("pet-shop.fill-item.material", "BLACK_STAINED_GLASS_PANE");
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

        ConfigurationSection itemsSection = plugin.getGuiManager().getGuiConfig().getConfigurationSection("pet-shop.items");
        if (itemsSection == null) {
            plugin.getLogger().warning("The 'pet-shop.items' section is missing from gui.yml. The pet shop will be empty.");
            player.openInventory(gui);
            return;
        }

        for (String petId : itemsSection.getKeys(false)) {
            String petPath = "pets." + petId;
            if (!configManager.getPets().contains(petPath)) {
                plugin.getLogger().warning("Pet '" + petId + "' defined in gui.yml does not exist in pets.yml.");
                continue;
            }

            int slot = itemsSection.getInt(petId + ".slot", -1);
            if (slot < 0 || slot >= size) {
                plugin.getLogger().warning("Invalid slot for pet '" + petId + "' in gui.yml. Slot: " + slot);
                continue;
            }

            String iconName = configManager.getPets().getString(petPath + ".icon", "BARRIER").toUpperCase();
            Material iconMaterial = Material.getMaterial(iconName);
            if (iconMaterial == null) {
                plugin.getLogger().warning("Invalid icon material '" + iconName + "' for pet '" + petId + "'. Using BARRIER.");
                iconMaterial = Material.BARRIER;
            }

            ItemStack item = new ItemStack(iconMaterial);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String displayName = configManager.getPets().getString(petPath + ".display-name", "<red>Unknown Pet</red>");
                meta.displayName(MiniMessage.miniMessage().deserialize(displayName));

                double price = configManager.getPets().getDouble(petPath + ".price", 0.0);
                String formattedPrice = economyManager.format(price);

                List<String> loreStrings = configManager.getPets().getStringList(petPath + ".lore");
                List<Component> lore = loreStrings.stream()
                        .map(line -> MiniMessage.miniMessage().deserialize(line.replace("{price}", formattedPrice)))
                        .collect(Collectors.toList());

                lore.add(Component.text("")); // Spacer
                lore.add(MiniMessage.miniMessage().deserialize("<green>Click to purchase!</green>"));

                meta.lore(lore);

                // Add pet ID to the item's persistent data container
                NamespacedKey key = new NamespacedKey(plugin, "pet_id");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, petId);

                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
        }
        player.openInventory(gui);
    }
}

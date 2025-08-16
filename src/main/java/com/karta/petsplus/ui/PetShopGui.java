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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        String titleString = configManager.getConfig().getString("pet-shop.menu-title", "Pet Shop");
        Component title = MiniMessage.miniMessage().deserialize(titleString);

        int rows = configManager.getConfig().getInt("pet-shop.menu-rows", 4);
        int size = Math.max(9, Math.min(54, rows * 9)); // Ensure size is between 9 and 54

        Inventory gui = Bukkit.createInventory(null, size, title);

        ConfigurationSection petsSection = configManager.getPets().getConfigurationSection("pets");
        if (petsSection == null) {
            plugin.getLogger().warning("The 'pets' section is missing from pets.yml. The pet shop will be empty.");
            player.openInventory(gui);
            return;
        }

        for (String petId : petsSection.getKeys(false)) {
            String path = "pets." + petId + ".";
            String iconName = configManager.getPets().getString(path + "icon", "BARRIER").toUpperCase();
            Material iconMaterial = Material.getMaterial(iconName);
            if (iconMaterial == null) {
                plugin.getLogger().warning("Invalid icon material '" + iconName + "' for pet '" + petId + "'. Using BARRIER.");
                iconMaterial = Material.BARRIER;
            }

            ItemStack item = new ItemStack(iconMaterial);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String displayName = configManager.getPets().getString(path + "display-name", "<red>Unknown Pet</red>");
                meta.displayName(MiniMessage.miniMessage().deserialize(displayName));

                double price = configManager.getPets().getDouble(path + "price", 0.0);
                String formattedPrice = economyManager.format(price);

                List<String> loreStrings = configManager.getPets().getStringList(path + "lore");
                List<Component> lore = loreStrings.stream()
                        .map(line -> MiniMessage.miniMessage().deserialize(line.replace("{price}", formattedPrice)))
                        .collect(Collectors.toList());

                lore.add(Component.text("")); // Spacer
                lore.add(MiniMessage.miniMessage().deserialize("<green>Click to purchase!</green>"));

                meta.lore(lore);
                item.setItemMeta(meta);
            }
            gui.addItem(item);
        }
        player.openInventory(gui);
    }
}

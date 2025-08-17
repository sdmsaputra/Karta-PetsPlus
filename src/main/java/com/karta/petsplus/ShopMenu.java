package com.karta.petsplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopMenu implements InventoryHolder {

    private final PetsPlus plugin;
    private final Player player;
    private int page = 0;
    private Inventory inventory;

    public ShopMenu(PetsPlus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        String title = plugin.getShopManager().getShopConfig().getString("shop-menu.title", "&e&lPet Shop");
        int size = plugin.getShopManager().getShopConfig().getInt("shop-menu.size", 54);

        inventory = Bukkit.createInventory(this, size, ChatColor.translateAlternateColorCodes('&', title));

        loadPage(page);

        player.openInventory(inventory);
    }

    private void loadPage(int page) {
        inventory.clear();
        ConfigurationSection petsSection = plugin.getShopManager().getShopConfig().getConfigurationSection("shop-menu.pets");
        if (petsSection == null) {
            return;
        }

        List<String> petKeys = new ArrayList<>(petsSection.getKeys(false));
        int pageSize = 45; // 54 - 9 for navigation
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, petKeys.size());

        for (int i = startIndex; i < endIndex; i++) {
            String petKey = petKeys.get(i);
            ConfigurationSection petSection = petsSection.getConfigurationSection(petKey);
            if (petSection != null) {
                int slot = petSection.getInt("slot", -1);
                if (slot != -1) {
                    inventory.setItem(slot, createPetItem(petSection));
                }
            }
        }

        addNavigationButtons(page, petKeys.size() > endIndex);
    }

    private ItemStack createPetItem(ConfigurationSection petSection) {
        Material material = Material.getMaterial(petSection.getString("material", "STONE"));
        String name = petSection.getString("name", "Unnamed Pet");
        List<String> lore = petSection.getStringList("lore");
        double price = petSection.getDouble("price", 0.0);
        String currency = petSection.getString("currency", "VAULT");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            PetData petData = plugin.getPetManager().getPlayerData(player);
            boolean unlocked = petData != null && petData.getOwnedPets().contains(petSection.getName());
            String status = unlocked ? "&aUnlocked" : "&cLocked";

            List<String> processedLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.replace("%pet_status%", ChatColor.translateAlternateColorCodes('&', status)))
                    .map(line -> line.replace("%pet_price%", String.valueOf(price)))
                    .map(line -> line.replace("%currency_type%", currency))
                    .collect(Collectors.toList());
            meta.setLore(processedLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void addNavigationButtons(int currentPage, boolean hasNextPage) {
        int size = inventory.getSize();

        if (currentPage > 0) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta meta = previous.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Previous Page");
            previous.setItemMeta(meta);
            inventory.setItem(size - 9, previous);
        }

        if (hasNextPage) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Next Page");
            next.setItemMeta(meta);
            inventory.setItem(size - 1, next);
        }
    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleMenuClick(int slot) {
        int size = inventory.getSize();
        if (slot == size - 9 && page > 0) {
            page--;
            loadPage(page);
        } else if (slot == size - 1) {
            ConfigurationSection petsSection = plugin.getShopManager().getShopConfig().getConfigurationSection("shop-menu.pets");
            if (petsSection != null) {
                int pageSize = 45;
                if ((page + 1) * pageSize < petsSection.getKeys(false).size()) {
                    page++;
                    loadPage(page);
                }
            }
        } else {
            // Handle pet purchase
            ConfigurationSection petsSection = plugin.getShopManager().getShopConfig().getConfigurationSection("shop-menu.pets");
            if (petsSection != null) {
                for (String petKey : petsSection.getKeys(false)) {
                    ConfigurationSection petSection = petsSection.getConfigurationSection(petKey);
                    if (petSection != null && petSection.getInt("slot") == slot) {
                        // Defer purchase handling to a dedicated handler
                        plugin.getPurchaseHandler().attemptPurchase(player, petKey);
                        player.closeInventory();
                        break;
                    }
                }
            }
        }
    }
}

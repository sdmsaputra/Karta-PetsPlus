package com.karta.petsplus.shop;

import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
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

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PurchaseConfirmMenu implements InventoryHolder {

    private final PetsPlus plugin;
    private final ShopManager shopManager;
    private final Player player;
    private final PetType petToBuy;
    private final double price;
    private final CurrencyProvider currency;
    private final Inventory inventory;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###.##");

    public PurchaseConfirmMenu(PetsPlus plugin, ShopManager shopManager, Player player, PetType petToBuy, double price, CurrencyProvider currency) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.player = player;
        this.petToBuy = petToBuy;
        this.price = price;
        this.currency = currency;

        ConfigurationSection confirmSection = shopManager.getShopConfig().getConfirmSection();
        String title = ChatColor.translateAlternateColorCodes('&', confirmSection.getString("title", "&6Confirm Purchase"));
        this.inventory = Bukkit.createInventory(this, 27, title); // 3x9 inventory
    }

    public void open() {
        ConfigurationSection confirmSection = shopManager.getShopConfig().getConfirmSection();

        // Accept button
        ConfigurationSection acceptSection = confirmSection.getConfigurationSection("accept");
        if (acceptSection != null) {
            inventory.setItem(acceptSection.getInt("slot", 11), createButton(acceptSection));
        }

        // Cancel button
        ConfigurationSection cancelSection = confirmSection.getConfigurationSection("cancel");
        if (cancelSection != null) {
            inventory.setItem(cancelSection.getInt("slot", 15), createButton(cancelSection));
        }

        player.openInventory(inventory);
    }

    private ItemStack createButton(ConfigurationSection section) {
        try {
            Material material = Material.valueOf(section.getString("material", "STONE").toUpperCase());
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String formattedPrice = shopManager.getShopConfig().shouldFormatPrice() ? priceFormatter.format(price) : String.valueOf(price);
                meta.setDisplayName(replacePlaceholders(section.getString("name", " ")));
                List<String> lore = section.getStringList("lore").stream()
                        .map(this::replacePlaceholders)
                        .collect(Collectors.toList());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create button from config section " + section.getName());
            return new ItemStack(Material.BARRIER);
        }
    }

    private String replacePlaceholders(String text) {
        if (text == null) return "";
        String formattedPrice = shopManager.getShopConfig().shouldFormatPrice() ? priceFormatter.format(price) : String.valueOf(price);
        return ChatColor.translateAlternateColorCodes('&', text
                .replace("%pet_display_name%", petToBuy.getDisplayName())
                .replace("%pet_price%", formattedPrice)
                .replace("%currency_symbol%", currency.getCurrencySymbol())
        );
    }

    public void handleClick(int slot) {
        ConfigurationSection confirmSection = shopManager.getShopConfig().getConfirmSection();
        ConfigurationSection acceptSection = confirmSection.getConfigurationSection("accept");
        ConfigurationSection cancelSection = confirmSection.getConfigurationSection("cancel");

        ReentrantLock lock = shopManager.getPurchaseHandler().purchaseLocks.get(player.getUniqueId());

        try {
            if (acceptSection != null && slot == acceptSection.getInt("slot", 11)) {
                // Player confirmed, execute the purchase
                shopManager.getPurchaseHandler().executePurchase(player, petToBuy, price, currency);
                player.closeInventory();
            } else if (cancelSection != null && slot == cancelSection.getInt("slot", 15)) {
                // Player cancelled, reopen the main shop menu
                player.closeInventory(); // Close confirm menu first
                new ShopMenu(plugin, shopManager, player).open();
            }
        } finally {
            // The lock was acquired in PurchaseHandler#attemptPurchase.
            // It is our responsibility to release it here, as this menu concluding the flow.
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

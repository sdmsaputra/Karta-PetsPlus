package com.karta.petsplus.shop;

import com.karta.petsplus.MessageManager;
import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ShopMenu implements InventoryHolder {

    private final PetsPlus plugin;
    private final ShopManager shopManager;
    private final Player player;
    private final ShopConfig shopConfig;
    private final IconResolver iconResolver;
    private final MessageManager messageManager;

    private int currentPage = 0;
    private final List<ShopPet> displayablePets;
    private Inventory inventory;

    public ShopMenu(PetsPlus plugin, ShopManager shopManager, Player player) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.player = player;
        this.shopConfig = shopManager.getShopConfig();
        this.iconResolver = shopManager.getIconResolver();
        this.messageManager = plugin.getMessageManager();
        this.displayablePets = new ArrayList<>();
    }

    public void open() {
        prepareDisplayablePets();
        String title = ChatColor.translateAlternateColorCodes('&', shopConfig.getMenuTitle());
        inventory = Bukkit.createInventory(this, shopConfig.getMenuSize(), title);
        renderPage();
        player.openInventory(inventory);
        player.sendMessage(messageManager.getMessage("shop_opened"));
    }

    private void prepareDisplayablePets() {
        displayablePets.clear();
        Set<String> unlockedPets = shopManager.getUnlockedPets(player);

        List<PetType> allPetTypes = new ArrayList<>(plugin.getConfigManager().getPetTypes().values());
        allPetTypes.sort(Comparator.comparing(PetType::getInternalName));

        for (PetType petType : allPetTypes) {
            ConfigurationSection override = shopConfig.getOverride(petType.getInternalName());
            ConfigurationSection defaults = shopConfig.getDefaultPetSection();

            if (resolveBoolean(override, defaults, "hidden")) {
                continue;
            }

            double price = resolveDouble(override, defaults, "price");
            boolean isPurchasable = resolveBoolean(override, defaults, "purchasable");
            String currencyName = resolveString(override, defaults, "currency").toUpperCase();
            CurrencyProvider currency = shopManager.getCurrencyProvider(currencyName)
                    .orElse(shopManager.getDefaultCurrencyProvider());

            displayablePets.add(new ShopPet(petType, price, currency, isPurchasable));
        }
    }

    private void renderPage() {
        inventory.clear();
        addNavigationButtons();

        List<Integer> slots = shopConfig.getContentSlots();
        if (slots.isEmpty()) return;

        int startIndex = currentPage * slots.size();
        int endIndex = Math.min(startIndex + slots.size(), displayablePets.size());

        for (int i = 0; i < (endIndex - startIndex); i++) {
            int slotIndex = slots.get(i);
            ShopPet shopPet = displayablePets.get(startIndex + i);
            boolean isUnlocked = shopManager.getUnlockedPets(player).contains(shopPet.petType.getInternalName());

            ItemStack icon = iconResolver.createPetIcon(shopPet.petType, isUnlocked, shopPet.isPurchasable, shopPet.price, shopPet.currency);
            inventory.setItem(slotIndex, icon);
        }
    }

    private void addNavigationButtons() {
        // Previous Page
        ConfigurationSection prevButtonSection = shopConfig.getButton("previous");
        if (currentPage > 0 && prevButtonSection != null) {
            inventory.setItem(prevButtonSection.getInt("slot", 45), createButton(prevButtonSection));
        }

        // Next Page
        List<Integer> slots = shopConfig.getContentSlots();
        int maxPages = (int) Math.ceil((double) displayablePets.size() / (double) (slots.isEmpty() ? 1 : slots.size()));
        ConfigurationSection nextButtonSection = shopConfig.getButton("next");
        if (currentPage < maxPages - 1 && nextButtonSection != null) {
            inventory.setItem(nextButtonSection.getInt("slot", 53), createButton(nextButtonSection));
        }

        // Back button
        ConfigurationSection backButtonSection = shopConfig.getButton("back");
        if (backButtonSection != null && backButtonSection.getBoolean("enabled", false)) {
            inventory.setItem(backButtonSection.getInt("slot", 49), createButton(backButtonSection));
        }
    }

    private ItemStack createButton(ConfigurationSection section) {
        try {
            Material material = Material.valueOf(section.getString("material", "STONE").toUpperCase());
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name", " ")));
                List<String> lore = section.getStringList("lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        } catch (Exception e) {
            return new ItemStack(Material.BARRIER);
        }
    }

    public void handleClick(int slot, ClickType clickType) {
        // Handle navigation clicks
        ConfigurationSection prevButtonSection = shopConfig.getButton("previous");
        if (prevButtonSection != null && slot == prevButtonSection.getInt("slot", 45) && currentPage > 0) {
            currentPage--;
            renderPage();
            return;
        }

        ConfigurationSection nextButtonSection = shopConfig.getButton("next");
        List<Integer> contentSlots = shopConfig.getContentSlots();
        int maxPages = (int) Math.ceil((double) displayablePets.size() / (double) (contentSlots.isEmpty() ? 1 : contentSlots.size()));
        if (nextButtonSection != null && slot == nextButtonSection.getInt("slot", 53) && currentPage < maxPages - 1) {
            currentPage++;
            renderPage();
            return;
        }

        // Handle back button click (logic to be implemented, maybe a callback)
        ConfigurationSection backButtonSection = shopConfig.getButton("back");
        if (backButtonSection != null && backButtonSection.getBoolean("enabled") && slot == backButtonSection.getInt("slot", 49)) {
            player.closeInventory();
            // Potentially open another menu, e.g., Bukkit.dispatchCommand(player, "pets");
            return;
        }

        // Handle pet item clicks
        if (contentSlots.contains(slot)) {
            int index = contentSlots.indexOf(slot) + (currentPage * contentSlots.size());
            if (index < displayablePets.size()) {
                ShopPet clickedPet = displayablePets.get(index);
                handlePetClick(clickedPet, clickType);
            }
        }
    }

    private void handlePetClick(ShopPet shopPet, ClickType clickType) {
        boolean isUnlocked = shopManager.getUnlockedPets(player).contains(shopPet.petType.getInternalName());

        if (clickType.isLeftClick()) {
            if (isUnlocked) {
                player.sendMessage(messageManager.getMessage("shop_already_owned", "%pet_display_name%", shopPet.petType.getDisplayName()));
            } else if (!shopPet.isPurchasable) {
                player.sendMessage(messageManager.getMessage("shop_unavailable", "%pet_display_name%", shopPet.petType.getDisplayName()));
            } else {
                shopManager.getPurchaseHandler().attemptPurchase(player, shopPet.petType, shopPet.price, shopPet.currency);
            }
        } else if (clickType.isRightClick()) {
            // Handle preview logic
            shopManager.startPetPreview(player, shopPet.petType);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    // Helper methods to resolve config values
    private String resolveString(ConfigurationSection override, ConfigurationSection defaults, String path) {
        return override != null ? override.getString(path, defaults.getString(path)) : defaults.getString(path);
    }

    private double resolveDouble(ConfigurationSection override, ConfigurationSection defaults, String path) {
        return override != null ? override.getDouble(path, defaults.getDouble(path)) : defaults.getDouble(path);
    }

    private boolean resolveBoolean(ConfigurationSection override, ConfigurationSection defaults, String path) {
        return override != null ? override.getBoolean(path, defaults.getBoolean(path)) : defaults.getBoolean(path);
    }

    // Private record to hold resolved data for a pet in the shop
    private record ShopPet(PetType petType, double price, CurrencyProvider currency, boolean isPurchasable) {}
}

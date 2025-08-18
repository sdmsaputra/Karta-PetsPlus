package com.karta.petsplus.gui;

import com.karta.petsplus.PetManager;
import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.shop.IconResolver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.stream.Collectors;

public class PetListMenu implements InventoryHolder {

    private final PetsPlus plugin;
    private final Player player;
    private final PetManager petManager;
    private final IconResolver iconResolver;
    private final List<PetType> ownedPets;

    private Inventory inventory;
    private int currentPage = 0;
    private final int contentSlotsSize = 45; // 5 rows of 9 slots for pets

    public PetListMenu(PetsPlus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.petManager = plugin.getPetManager();
        this.iconResolver = plugin.getShopManager().getIconResolver(); // Reuse the shop's icon resolver
        this.ownedPets = new ArrayList<>();
    }

    public void open() {
        prepareOwnedPets();
        if (ownedPets.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-pets-owned"));
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', "&1Your Pets"); // Can be made configurable later
        inventory = Bukkit.createInventory(this, 54, title);
        renderPage();
        player.openInventory(inventory);
    }

    private void prepareOwnedPets() {
        ownedPets.clear();
        List<String> ownedPetNames = petManager.getPlayerData(player).getOwnedPets();
        for (String petName : ownedPetNames) {
            PetType petType = plugin.getConfigManager().getPetType(petName);
            if (petType != null) {
                ownedPets.add(petType);
            }
        }
        ownedPets.sort(Comparator.comparing(PetType::getDisplayName));
    }

    private void renderPage() {
        inventory.clear();
        addNavigationButtons();

        int startIndex = currentPage * contentSlotsSize;
        int endIndex = Math.min(startIndex + contentSlotsSize, ownedPets.size());

        for (int i = 0; i < (endIndex - startIndex); i++) {
            int slotIndex = i;
            PetType petType = ownedPets.get(startIndex + i);
            ItemStack icon = createPetIcon(petType);
            inventory.setItem(slotIndex, icon);
        }
    }

    private ItemStack createPetIcon(PetType petType) {
        // We can reuse the IconResolver, but we need to create a simpler lore for the list
        ItemStack icon = iconResolver.createPetIcon(petType, true, false, 0, null);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + petType.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to summon this pet.");
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private void addNavigationButtons() {
        if (currentPage > 0) {
            inventory.setItem(45, createButton(Material.ARROW, "&7Previous Page"));
        }

        int maxPages = (int) Math.ceil((double) ownedPets.size() / (double) contentSlotsSize);
        if (currentPage < maxPages - 1) {
            inventory.setItem(53, createButton(Material.ARROW, "&aNext Page"));
        }
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(int slot, ClickType clickType) {
        if (slot == 45 && currentPage > 0) {
            currentPage--;
            renderPage();
            return;
        }

        int maxPages = (int) Math.ceil((double) ownedPets.size() / (double) contentSlotsSize);
        if (slot == 53 && currentPage < maxPages - 1) {
            currentPage++;
            renderPage();
            return;
        }

        if (slot >= 0 && slot < contentSlotsSize) {
            int index = slot + (currentPage * contentSlotsSize);
            if (index < ownedPets.size()) {
                PetType clickedPet = ownedPets.get(index);
                player.closeInventory();
                petManager.summonPet(player, clickedPet.getInternalName());
            }
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

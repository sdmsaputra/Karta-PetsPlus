package com.karta.petsplus.ui.lib;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public abstract class PagedGUI {

    protected final KartaPetsPlus plugin;
    protected final Inventory inventory;
    protected final int page;
    protected final int totalPages;

    public PagedGUI(KartaPetsPlus plugin, String title, int rows, int page, int totalPages) {
        this.plugin = plugin;
        this.page = page;
        this.totalPages = totalPages;
        this.inventory = Bukkit.createInventory(null, rows * 9, MiniMessage.miniMessage().deserialize(title));
    }

    protected void fillBackground(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
    }

    protected void addNavigationButtons() {
        int size = inventory.getSize();

        // Previous Page Button
        if (page > 0) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previous.getItemMeta();
            previousMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN));
            previousMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "previous_page");
            previous.setItemMeta(previousMeta);
            inventory.setItem(size - 9, previous);
        }

        // Next Page Button
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN));
            nextMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "next_page");
            next.setItemMeta(nextMeta);
            inventory.setItem(size - 1, next);
        }

        // Page Info
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageInfoMeta = pageInfo.getItemMeta();
        pageInfoMeta.displayName(Component.text("Page " + (page + 1) + "/" + totalPages, NamedTextColor.YELLOW));
        pageInfo.setItemMeta(pageInfoMeta);
        inventory.setItem(size - 5, pageInfo);
    }

    protected void addBackButton(String action) {
        int size = inventory.getSize();
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back", NamedTextColor.RED));
        backMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, action);
        back.setItemMeta(backMeta);
        inventory.setItem(size - 8, back);
    }

    public Inventory getInventory() {
        return inventory;
    }
}

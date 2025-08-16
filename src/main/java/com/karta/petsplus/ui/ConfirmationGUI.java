package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ConfirmationGUI {

    public static void open(KartaPetsPlus plugin, Player player, String petId) {
        Inventory gui = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<dark_aqua>Confirm Purchase</dark_aqua>"));

        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.displayName(Component.text(" "));
        background.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        // Confirm Button
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN));
        confirmMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "confirm_purchase");
        confirmMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pet_id"), PersistentDataType.STRING, petId);
        confirm.setItemMeta(confirmMeta);
        gui.setItem(11, confirm);

        // Cancel Button
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
        cancelMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_action"), PersistentDataType.STRING, "open_pet_shop");
        cancel.setItemMeta(cancelMeta);
        gui.setItem(15, cancel);

        player.openInventory(gui);
    }
}

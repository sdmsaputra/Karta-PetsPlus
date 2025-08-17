package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PetInteractionGUI {

    public static void open(KartaPetsPlus plugin, Player player, Pet pet) {
        Inventory gui = Bukkit.createInventory(null, 9, "Pet Menu");

        ItemStack renameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameItem.getItemMeta();
        renameMeta.setDisplayName("Rename Pet");
        renameItem.setItemMeta(renameMeta);

        gui.setItem(4, renameItem);

        player.openInventory(gui);
    }
}

package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PetMenuGuiListener implements Listener {

    private final KartaPetsPlus plugin;

    public PetMenuGuiListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("My Pets")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            if (event.getClick().isLeftClick()) {
                // Summon/stow pet
                player.sendMessage("You left-clicked a pet!");
            } else if (event.getClick().isRightClick()) {
                // Rename pet
                player.sendMessage("You right-clicked a pet!");
            }
        }
    }
}

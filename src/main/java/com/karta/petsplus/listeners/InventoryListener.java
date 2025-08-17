package com.karta.petsplus.listeners;

import com.karta.petsplus.ShopMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ShopMenu) {
            event.setCancelled(true);
            ShopMenu shopMenu = (ShopMenu) holder;
            shopMenu.handleMenuClick(event.getRawSlot());
        }
    }
}

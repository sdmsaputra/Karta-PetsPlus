package com.karta.petsplus.listeners;

import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.gui.PetListMenu;
import com.karta.petsplus.shop.PurchaseConfirmMenu;
import com.karta.petsplus.shop.ShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    private final PetsPlus plugin;

    public InventoryListener(PetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ShopMenu) {
            event.setCancelled(true);
            ShopMenu shopMenu = (ShopMenu) holder;
            shopMenu.handleClick(event.getRawSlot(), event.getClick());
        } else if (holder instanceof PurchaseConfirmMenu) {
            event.setCancelled(true);
            PurchaseConfirmMenu confirmMenu = (PurchaseConfirmMenu) holder;
            confirmMenu.handleClick(event.getRawSlot());
        } else if (holder instanceof PetListMenu) {
            event.setCancelled(true);
            PetListMenu listMenu = (PetListMenu) holder;
            listMenu.handleClick(event.getRawSlot(), event.getClick());
        }
    }
}

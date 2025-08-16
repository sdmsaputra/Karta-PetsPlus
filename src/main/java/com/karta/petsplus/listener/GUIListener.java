package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.EconomyManager;
import com.karta.petsplus.manager.PlayerDataManager;
import com.karta.petsplus.ui.ConfirmationGUI;
import com.karta.petsplus.ui.PetManagementGUI;
import com.karta.petsplus.ui.PetShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GUIListener implements Listener {

    private final KartaPetsPlus plugin;

    public GUIListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();
        String shopTitle = MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getGui().getString("pet-shop.title", "Pet Shop")));
        String menuTitle = MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getGui().getString("pet-menu.title", "My Pets")));
        String confirmTitle = MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize("<dark_aqua>Confirm Purchase</dark_aqua>"));

        boolean isCustomGui = inventoryTitle.equals(shopTitle) || inventoryTitle.equals(menuTitle) || inventoryTitle.equals(confirmTitle);

        if (!isCustomGui) {
            return;
        }

        event.setCancelled(true);

        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();
        NamespacedKey actionKey = new NamespacedKey(plugin, "gui_action");

        if (data.has(actionKey, PersistentDataType.STRING)) {
            handleActionClick(player, inventory, data.get(actionKey, PersistentDataType.STRING), data);
        } else {
            handleItemClick(player, inventoryTitle, shopTitle, clickedItem);
        }
    }

    private void handleActionClick(Player player, Inventory inventory, String action, PersistentDataContainer data) {
        int currentPage = getCurrentPage(inventory);

        switch (action) {
            case "next_page":
                if (inventory.getViewers().get(0).getOpenInventory().getTitle().contains("Shop")) {
                    PetShopGUI.openShop(plugin, player, currentPage + 1);
                } else {
                    PetManagementGUI.open(plugin, player, currentPage + 1);
                }
                break;
            case "previous_page":
                 if (inventory.getViewers().get(0).getOpenInventory().getTitle().contains("Shop")) {
                    PetShopGUI.openShop(plugin, player, currentPage - 1);
                } else {
                    PetManagementGUI.open(plugin, player, currentPage - 1);
                }
                break;
            case "open_pet_shop":
                PetShopGUI.openShop(plugin, player, 0);
                break;
            case "confirm_purchase":
                String petIdToBuy = data.get(new NamespacedKey(plugin, "pet_id"), PersistentDataType.STRING);
                purchasePet(player, petIdToBuy);
                break;
        }
    }

    private void handleItemClick(Player player, String inventoryTitle, String shopTitle, ItemStack clickedItem) {
        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();

        if (inventoryTitle.equals(shopTitle)) {
            // Click in Pet Shop
            String petId = data.get(new NamespacedKey(plugin, "pet_id"), PersistentDataType.STRING);
            if (petId != null) {
                ConfirmationGUI.open(plugin, player, petId);
            }
        } else {
            // Click in Pet Menu
            String petUuidStr = data.get(new NamespacedKey(plugin, "pet_uuid"), PersistentDataType.STRING);
            if (petUuidStr != null) {
                UUID petUuid = UUID.fromString(petUuidStr);
                plugin.getPlayerDataManager().getPet(player, petUuid).ifPresent(pet -> {
                    if (pet.getStatus() == Pet.PetStatus.STOWED) {
                        plugin.getPetManager().summonPet(player, pet);
                    } else {
                        plugin.getPetManager().despawnPet(player);
                    }
                    // Refresh the GUI
                    int currentPage = getCurrentPage(player.getOpenInventory().getTopInventory());
                    PetManagementGUI.open(plugin, player, currentPage);
                });
            }
        }
    }

    private void purchasePet(Player player, String petId) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (playerDataManager.hasPet(player, petId)) {
            plugin.getMessageManager().sendMessage(player, "pet-already-owned");
            return;
        }

        double price = plugin.getConfigManager().getPets().getDouble("pets." + petId + ".price");
        EconomyManager economyManager = plugin.getEconomyManager();

        if (economyManager.has(player, price)) {
            economyManager.withdraw(player, price);
            playerDataManager.addPet(player, petId);
            plugin.getMessageManager().sendMessage(player, "pet-purchase-success", "{pet_name}", plugin.getConfigManager().getPets().getString("pets." + petId + ".display-name"));
            player.closeInventory();
        } else {
            plugin.getMessageManager().sendMessage(player, "not-enough-money");
        }
    }

    private int getCurrentPage(Inventory inventory) {
        ItemStack pageInfoItem = inventory.getItem(inventory.getSize() - 5);
        if (pageInfoItem == null || pageInfoItem.getType() != org.bukkit.Material.PAPER) {
            return 0;
        }
        String pageText = MiniMessage.miniMessage().serialize(pageInfoItem.displayName());
        try {
            String pageNumStr = pageText.split(" ")[1].split("/")[0];
            return Integer.parseInt(pageNumStr) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}

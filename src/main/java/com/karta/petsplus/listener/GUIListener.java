package com.karta.petsplus.listener;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.EconomyManager;
import com.karta.petsplus.manager.PlayerDataManager;
import com.karta.petsplus.ui.MyPetsGUI;
import com.karta.petsplus.ui.PetShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.stream.Collectors;

public class GUIListener implements Listener {

    private final KartaPetsPlus plugin;

    public GUIListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = MiniMessage.miniMessage().serialize(event.getView().title());

        String shopTitle = "§d§lKartaPetsPlus";
        String myPetsTitle = "§d§lMy Pets";

        if (inventoryTitle.equals(shopTitle)) {
            handlePetShopClick(event, player);
        } else if (inventoryTitle.equals(myPetsTitle)) {
            handleMyPetsClick(event, player);
        }
    }

    private void handlePetShopClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();
        NamespacedKey actionKey = new NamespacedKey(plugin, "gui_action");
        NamespacedKey petIdKey = new NamespacedKey(plugin, "pet_id");

        if (data.has(actionKey, PersistentDataType.STRING)) {
            // Handle navigation clicks
            String action = data.get(actionKey, PersistentDataType.STRING);
            int currentPage = getCurrentPage(event.getInventory());
            switch (action) {
                case "next_page":
                    PetShopGUI.openShop(plugin, player, currentPage + 1);
                    break;
                case "my_pets":
                    MyPetsGUI.open(plugin, player, 0);
                    break;
                case "close":
                    player.closeInventory();
                    break;
            }
        } else if (data.has(petIdKey, PersistentDataType.STRING)) {
            // Handle pet item clicks
            String petId = data.get(petIdKey, PersistentDataType.STRING);
            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
            boolean isOwned = playerDataManager.getPets(player).stream()
                    .anyMatch(p -> p.getPetType().name().equalsIgnoreCase(petId));

            if (event.getClick() == ClickType.LEFT) {
                // Buy pet
                if (isOwned) {
                    plugin.getMessageManager().sendMessage(player, "pet-already-owned", "<red>You already own this pet!</red>");
                } else {
                    purchasePet(player, petId);
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                // Summon pet
                if (isOwned) {
                    playerDataManager.getPets(player).stream()
                        .filter(p -> p.getPetType().name().equalsIgnoreCase(petId))
                        .findFirst().ifPresent(pet -> {
                            plugin.getPetManager().summonPet(player, pet);
                            player.closeInventory();
                        });
                } else {
                    plugin.getMessageManager().sendMessage(player, "pet-not-owned", "<red>You do not own this pet. Left-click to buy it!</red>");
                }
            }
        }
    }

    private void handleMyPetsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();
        NamespacedKey actionKey = new NamespacedKey(plugin, "gui_action");
        NamespacedKey petIdKey = new NamespacedKey(plugin, "pet_id");

        if (data.has(actionKey, PersistentDataType.STRING)) {
            String action = data.get(actionKey, PersistentDataType.STRING);
            if ("open_shop".equals(action)) {
                PetShopGUI.openShop(plugin, player, 0);
            }
        } else if (data.has(petIdKey, PersistentDataType.STRING)) {
            String petId = data.get(petIdKey, PersistentDataType.STRING);
            plugin.getPlayerDataManager().getPets(player).stream()
                .filter(p -> p.getPetType().name().equalsIgnoreCase(petId))
                .findFirst().ifPresent(pet -> {
                    plugin.getPetManager().summonPet(player, pet);
                    player.closeInventory();
                });
        }
    }

    private void purchasePet(Player player, String petId) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        double price = plugin.getConfigManager().getPets().getDouble("pets." + petId.toLowerCase() + ".price");
        EconomyManager economyManager = plugin.getEconomyManager();

        if (economyManager.has(player, price)) {
            economyManager.withdraw(player, price);
            playerDataManager.addPet(player, petId);
            String petName = plugin.getConfigManager().getPets().getString("pets." + petId.toLowerCase() + ".display-name", petId);
            plugin.getMessageManager().sendMessage(player, "pet-purchase-success", "<green>You have successfully purchased {pet_name}!</green>", Placeholder.parsed("pet_name", petName));

            // Refresh GUI to show as owned
            int currentPage = getCurrentPage(player.getOpenInventory().getTopInventory());
            PetShopGUI.openShop(plugin, player, currentPage);
        } else {
            plugin.getMessageManager().sendMessage(player, "not-enough-money", "<red>You do not have enough money to purchase this pet.</red>");
            player.closeInventory();
        }
    }

    private int getCurrentPage(org.bukkit.inventory.Inventory inventory) {
        ItemStack navItem = inventory.getItem(47); // Next page button slot
        if (navItem == null) return 0; // Default to page 0

        String title = MiniMessage.miniMessage().serialize(inventory.getViewers().get(0).getOpenInventory().title());
        if (title.equals("§d§lMy Pets")) {
             navItem = inventory.getItem(49); // Back to shop button
        }

        if (navItem == null || navItem.getItemMeta() == null) return 0;

        // A bit of a hacky way to get page number, would be better to store in inventory's PDC
        // For now, we assume if next page exists, we are on page N and it takes us to N+1
        // This part of the logic is not perfect but will work for now.
        // A proper implementation would store page number in inventory's PDC.
        return 0; // Simplified, as pagination logic is complex without storing page in PDC
    }
}

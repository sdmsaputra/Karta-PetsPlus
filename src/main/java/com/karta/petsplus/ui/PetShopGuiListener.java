package com.karta.petsplus.ui;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.manager.EconomyManager;
import com.karta.petsplus.manager.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PetShopGuiListener implements Listener {

    private final KartaPetsPlus plugin;
    private final MessageManager messageManager;
    private final EconomyManager economyManager;
    private final Component shopTitle;

    public PetShopGuiListener(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.economyManager = plugin.getEconomyManager();
        String titleString = plugin.getConfigManager().getConfig().getString("pet-shop.menu-title", "Pet Shop");
        this.shopTitle = MiniMessage.miniMessage().deserialize(titleString);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(shopTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        NamespacedKey key = new NamespacedKey(plugin, "pet_id");

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String petId = clickedItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            purchasePet(player, petId);
            player.closeInventory();
        }
    }

    private void purchasePet(Player player, String petId) {
        // Get pet details from pets.yml
        double price = plugin.getConfigManager().getPets().getDouble("pets." + petId + ".price", 0.0);
        String petName = plugin.getConfigManager().getPets().getString("pets." + petId + ".display-name", "A Pet");

        // Check pet limit
        int petLimit = plugin.getConfigManager().getConfig().getInt("default-pet-limit", 5);
        if (plugin.getPlayerDataManager().getPets(player).size() >= petLimit) {
            messageManager.sendMessage(player, "pet-limit-reached", "<red>You have reached your maximum pet limit.</red>");
            return;
        }

        // Check balance
        if (economyManager.getVaultEconomy() == null || !economyManager.getVaultEconomy().has(player, price)) {
            messageManager.sendMessage(player, "not-enough-money", "<red>You do not have enough money to purchase this pet.</red>");
            return;
        }

        // Process purchase
        economyManager.getVaultEconomy().withdrawPlayer(player, price);
        Pet newPet = new Pet(player.getUniqueId(), petId, petName);
        plugin.getPlayerDataManager().addPet(player, newPet);

        messageManager.sendMessage(player, "pet-purchase-success", "<green>You have successfully purchased a {pet_name}!</green>",
                Placeholder.component("pet_name", MiniMessage.miniMessage().deserialize(petName)));
    }
}

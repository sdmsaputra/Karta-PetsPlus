package com.karta.petsplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PurchaseHandler {

    private final PetsPlus plugin;
    private final EconomyManager economyManager;
    private final PetManager petManager;
    private final MessageManager messageManager;

    public PurchaseHandler(PetsPlus plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.petManager = plugin.getPetManager();
        this.messageManager = plugin.getMessageManager();
    }

    public void attemptPurchase(Player player, String petKey) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ConfigurationSection petSection = plugin.getShopManager().getShopConfig().getConfigurationSection("shop-menu.pets." + petKey);
            if (petSection == null) {
                player.sendMessage(messageManager.getMessage("pet-not-found"));
                return;
            }

            double price = petSection.getDouble("price");
            String currency = petSection.getString("currency", "VAULT"); // Assuming VAULT is default

            PetData petData = petManager.getPlayerData(player);
            if (petData.getOwnedPets().contains(petKey)) {
                player.sendMessage(messageManager.getMessage("pet-already-owned"));
                return;
            }

            if (!economyManager.isEconomyEnabled()) {
                player.sendMessage(messageManager.getMessage("economy-disabled"));
                return;
            }

            if (!economyManager.hasEnough(player, price)) {
                player.sendMessage(messageManager.getMessage("not-enough-money"));
                return;
            }

            if (economyManager.withdraw(player, price)) {
                petData.addOwnedPet(petKey);
                petManager.savePlayerData(player.getUniqueId(), petData);
                player.sendMessage(messageManager.getMessage("pet-purchase-success").replace("{pet_name}", petSection.getString("name")));
            } else {
                player.sendMessage(messageManager.getMessage("purchase-failed"));
            }
        });
    }
}

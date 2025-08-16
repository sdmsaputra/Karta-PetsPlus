package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Handles the /petshop command, which opens the pet shop menu
 * and provides administrative subcommands.
 */
public class PetShopCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;
    private final ConfigManager configManager;

    public PetShopCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("add")) {
                handleAddCommand(sender, args);
                return true;
            }
        }

        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be executed by a player.</red>");
            return true;
        }

        Player player = (Player) sender;
        new com.karta.petsplus.ui.PetShopGui(plugin, player).open();
        return true;
    }

    private void handleAddCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kartapetsplus.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission", "<red>You do not have permission to use this command.</red>");
            return;
        }

        if (args.length != 3) {
            plugin.getMessageManager().sendMessage(sender, "petshop-add-usage", "<red>Usage: /petshop add <entityType> <price></red>");
            return;
        }

        String entityTypeName = args[1].toUpperCase(Locale.ROOT);
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeName);
        } catch (IllegalArgumentException e) {
            plugin.getMessageManager().sendMessage(sender, "invalid-entity-type", "<red>Invalid entity type: " + entityTypeName + "</red>");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "invalid-price", "<red>Invalid price: " + args[2] + "</red>");
            return;
        }

        String petId = entityTypeName.toLowerCase(Locale.ROOT);
        String petPath = "pets." + petId;

        if (configManager.getPets().contains(petPath)) {
            plugin.getMessageManager().sendMessage(sender, "pet-already-exists", "<red>A pet with this entity type already exists in the shop.</red>");
            return;
        }

        // Create a new entry in pets.yml
        configManager.getPets().set(petPath + ".display-name", "<white>" + capitalize(entityTypeName) + "</white>");
        configManager.getPets().set(petPath + ".icon", "EGG"); // Default icon
        configManager.getPets().set(petPath + ".price", price);
        configManager.getPets().set(petPath + ".lore", java.util.Arrays.asList("<gray>A new pet available for purchase.", "", "<aqua>Price: <white>{price}</white>"));
        configManager.getPets().set(petPath + ".entity-type", entityTypeName);
        configManager.getPets().set(petPath + ".abilities", java.util.Arrays.asList("NONE"));

        // Save the pets.yml file
        plugin.getConfigManager().savePets();

        plugin.getMessageManager().sendMessage(sender, "pet-added", "<green>Successfully added the " + entityTypeName + " pet to the shop with a price of " + price + ".</green>");
        plugin.getMessageManager().sendMessage(sender, "pet-added-info", "<gray>You may need to add this pet to gui.yml to make it visible in the shop.</gray>");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase(Locale.ROOT);
    }
}

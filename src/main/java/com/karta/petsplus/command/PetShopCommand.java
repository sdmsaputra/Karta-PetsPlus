package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            String subCommand = args[0].toLowerCase(Locale.ROOT);
            if (!player.hasPermission("kartapetsplus.admin")) {
                plugin.getMessageManager().sendMessage(player, "no-permission", "<red>You do not have permission to use this command.</red>");
                return true;
            }
            switch (subCommand) {
                case "add":
                    addPet(player, args);
                    break;
                case "edit":
                    new PetShopEditCommand(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                    break;
                default:
                    plugin.getMessageManager().sendMessage(player, "unknown-subcommand", "<red>Unknown subcommand. Usage: /petshop [add|edit]</red>");
                    break;
            }
        } else {
            com.karta.petsplus.ui.PetShopGUI.openShop(plugin, player, 0);
        }
        return true;
    }

    private void addPet(Player player, String[] args) {
        if (args.length < 3 || args.length > 5) {
            plugin.getMessageManager().sendMessage(player, "petshop-add-usage", "<red>Usage: /petshop add <entityType> <price> [icon|texture:<value>] [description]</red>");
            return;
        }

        String entityTypeStr = args[1].toUpperCase(Locale.ROOT);
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeStr);
            if (!entityType.isAlive()) {
                plugin.getMessageManager().sendMessage(player, "not-a-living-entity", "<red>The entity type must be a living entity.</red>");
                return;
            }
        } catch (IllegalArgumentException e) {
            plugin.getMessageManager().sendMessage(player, "invalid-entity-type", "<red>Invalid entity type. Please provide a valid entity type.</red>");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(player, "invalid-price", "<red>Invalid price. Please provide a valid number.</red>");
            return;
        }

        String petId = entityTypeStr.toLowerCase(Locale.ROOT);
        String petPath = "pets." + petId;

        if (configManager.getPets().contains(petPath)) {
            plugin.getMessageManager().sendMessage(player, "pet-already-exists", "<red>A pet with this entity type already exists in the shop.</red>");
            return;
        }

        String iconArg = args.length > 3 ? args[3] : "PAPER";
        String description = args.length > 4 ? args[4] : "";

        if (iconArg.toLowerCase().startsWith("texture:")) {
            String texture = iconArg.substring(8);
            configManager.getPets().set(petPath + ".icon", "PLAYER_HEAD");
            configManager.getPets().set(petPath + ".head-texture", texture);
        } else {
            configManager.getPets().set(petPath + ".icon", iconArg.toUpperCase(Locale.ROOT));
            configManager.getPets().set(petPath + ".head-texture", null); // Ensure it's not set
        }

        configManager.getPets().set(petPath + ".display-name", "<white>" + entityTypeStr.substring(0, 1).toUpperCase(Locale.ROOT) + entityTypeStr.substring(1).toLowerCase(Locale.ROOT) + "</white>");
        configManager.getPets().set(petPath + ".price", price);
        configManager.getPets().set(petPath + ".lore", java.util.Collections.singletonList(description));
        configManager.getPets().set(petPath + ".entity-type", entityTypeStr);
        configManager.savePets();

        plugin.getMessageManager().sendMessage(player, "pet-added", "<green>Successfully added the " + petId + " pet to the shop.</green>");
    }
}

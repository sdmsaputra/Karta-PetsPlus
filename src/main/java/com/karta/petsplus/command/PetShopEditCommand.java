package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.manager.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class PetShopEditCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;
    private final ConfigManager configManager;

    public PetShopEditCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(sender, "petshop-edit-usage", "<red>Usage: /petshop edit <petId> <name|icon|description|price|delete> [value]</red>");
            return true;
        }

        String petId = args[0].toLowerCase(Locale.ROOT);
        String petPath = "pets." + petId;

        if (!configManager.getPets().contains(petPath)) {
            plugin.getMessageManager().sendMessage(sender, "pet-not-found", "<red>A pet with that ID does not exist in the shop.</red>");
            return true;
        }

        String subCommand = args[1].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "name":
                if (args.length < 3) {
                    plugin.getMessageManager().sendMessage(sender, "petshop-edit-name-usage", "<red>Usage: /petshop edit <petId> name <newName></red>");
                    return true;
                }
                String newName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                configManager.getPets().set(petPath + ".display-name", newName);
                configManager.savePets();
                plugin.getMessageManager().sendMessage(sender, "pet-name-updated", "<green>Pet name updated successfully.</green>");
                break;
            case "icon":
                if (args.length != 3) {
                    plugin.getMessageManager().sendMessage(sender, "petshop-edit-icon-usage", "<red>Usage: /petshop edit <petId> icon <newIcon></red>");
                    return true;
                }
                String newIcon = args[2].toUpperCase(Locale.ROOT);
                configManager.getPets().set(petPath + ".icon", newIcon);
                configManager.savePets();
                plugin.getMessageManager().sendMessage(sender, "pet-icon-updated", "<green>Pet icon updated successfully.</green>");
                break;
            case "description":
                if (args.length < 3) {
                    plugin.getMessageManager().sendMessage(sender, "petshop-edit-description-usage", "<red>Usage: /petshop edit <petId> description <newDescription></red>");
                    return true;
                }
                String newDescription = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                configManager.getPets().set(petPath + ".lore", Arrays.asList(newDescription.split("\\\\n")));
                configManager.savePets();
                plugin.getMessageManager().sendMessage(sender, "pet-description-updated", "<green>Pet description updated successfully.</green>");
                break;
            case "price":
                if (args.length != 3) {
                    plugin.getMessageManager().sendMessage(sender, "petshop-edit-price-usage", "<red>Usage: /petshop edit <petId> price <newPrice></red>");
                    return true;
                }
                try {
                    double newPrice = Double.parseDouble(args[2]);
                    configManager.getPets().set(petPath + ".price", newPrice);
                    configManager.savePets();
                    plugin.getMessageManager().sendMessage(sender, "pet-price-updated", "<green>Pet price updated successfully.</green>");
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(sender, "invalid-price", "<red>Invalid price. Please provide a valid number.</red>");
                }
                break;
            case "delete":
                configManager.getPets().set(petPath, null);
                configManager.savePets();
                plugin.getMessageManager().sendMessage(sender, "pet-deleted", "<green>Pet deleted successfully.</green>");
                break;
            default:
                plugin.getMessageManager().sendMessage(sender, "petshop-edit-usage", "<red>Usage: /petshop edit <petId> <name|icon|description|price|delete> [value]</red>");
                break;
        }

        return true;
    }
}

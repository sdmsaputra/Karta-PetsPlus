package com.karta.petsplus.commands;

import com.karta.petsplus.PetsPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PetsCommand implements CommandExecutor {

    private final PetsPlus plugin;

    public PetsCommand(PetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("petsplus.use")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Show help message
            plugin.getMessageManager().getMessage("help-message").lines().forEach(sender::sendMessage);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("petsplus.admin")) {
                sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                return true;
            }
            plugin.getConfigManager().reloadConfig();
            plugin.getMessageManager().reloadMessages();
            sender.sendMessage(plugin.getMessageManager().getMessage("reload"));
            return true;
        }

        return true;
    }
}

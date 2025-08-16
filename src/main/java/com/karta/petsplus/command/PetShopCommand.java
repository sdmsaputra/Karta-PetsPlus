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
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return true;
        }

        Player player = (Player) sender;
        com.karta.petsplus.ui.PetShopGUI.openShop(plugin, player, 0);
        return true;
    }
}

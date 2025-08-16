package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /petshop command, which opens the pet shop menu.
 */
public class PetShopCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;

    public PetShopCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be executed by a player.</red>");
            return true;
        }

        Player player = (Player) sender;

        // Open the pet shop GUI
        new com.karta.petsplus.ui.PetShopGui(plugin, player).open();

        return true;
    }
}

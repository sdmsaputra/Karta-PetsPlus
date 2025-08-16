package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
            String playerOnlyMessage = plugin.getConfigManager().getMessages().getString("player-only-command", "<red>This command can only be executed by a player.</red>");
            sender.sendMessage(MiniMessage.miniMessage().deserialize(playerOnlyMessage));
            return true;
        }

        Player player = (Player) sender;

        // For now, just sends a placeholder message.
        // In the future, this will open the pet shop GUI.
        String petShopMessage = plugin.getConfigManager().getMessages().getString("pet-shop-opened", "<gray>Opening the Pet Shop...</gray>");
        player.sendMessage(MiniMessage.miniMessage().deserialize(petShopMessage));

        // TODO: Implement Pet Shop GUI

        return true;
    }
}

package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /pets command, which opens the pet management menu.
 */
public class PetsCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;

    public PetsCommand(KartaPetsPlus plugin) {
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
        // In the future, this will open the pet management GUI.
        String petMenuMessage = plugin.getConfigManager().getMessages().getString("pet-menu-opened", "<gray>Opening your pet menu...</gray>");
        player.sendMessage(MiniMessage.miniMessage().deserialize(petMenuMessage));

        // TODO: Implement Pet Management GUI

        return true;
    }
}

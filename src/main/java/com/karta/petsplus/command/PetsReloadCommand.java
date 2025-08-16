package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /petsreload command, which reloads the plugin's configuration files.
 */
public class PetsReloadCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;

    public PetsReloadCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getConfigManager().reloadConfigs();

        String reloadMessage = plugin.getConfigManager().getMessages().getString("reload-success", "<green>KartaPetsPlus configuration has been reloaded successfully!</green>");
        sender.sendMessage(MiniMessage.miniMessage().deserialize(reloadMessage));

        return true;
    }
}

package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PetShopCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;

    public PetShopCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("player-only-command")));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("kartapetsplus.shop")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("no-permission")));
            return true;
        }

        // Placeholder for pet shop menu
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Opening the pet shop...</green>"));
        // TODO: Implement pet shop GUI

        return true;
    }
}

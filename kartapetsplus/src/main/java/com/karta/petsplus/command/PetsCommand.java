package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class PetsCommand implements CommandExecutor, TabCompleter {

    private final KartaPetsPlus plugin;

    public PetsCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                handleReload(sender);
                return true;
            }
        }

        handlePetMenu(sender);
        return true;
    }

    private void handlePetMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("player-only-command")));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("kartapetsplus.use")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("no-permission")));
            return;
        }

        // Placeholder for pet management menu
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Opening your pet menu...</green>"));
        // TODO: Implement pet management GUI
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("kartapetsplus.admin")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("no-permission")));
            return;
        }
        plugin.getConfigManager().reload();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessages().getString("reload-success")));
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("kartapetsplus.admin")) {
                return List.of("reload").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
}

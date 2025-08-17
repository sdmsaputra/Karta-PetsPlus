package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public ReloadCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin's configuration.";
    }

    @Override
    public String getSyntax() {
        return "/pets reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kartapetsplus.command.reload")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission", "<red>You do not have permission to use this command.</red>");
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.getMessageManager().sendMessage(sender, "config-reloaded", "<green>The configuration has been reloaded.</green>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

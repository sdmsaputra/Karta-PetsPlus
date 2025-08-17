package com.karta.petsplus.command.subcommand;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName();
    String getDescription();
    String getSyntax();
    void perform(CommandSender sender, String[] args);
    List<String> onTabComplete(CommandSender sender, String[] args);
}

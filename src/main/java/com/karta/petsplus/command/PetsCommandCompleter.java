package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PetsCommandCompleter implements TabCompleter {

    private final KartaPetsPlus plugin;

    public PetsCommandCompleter(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                if (plugin.getPetManager().isPetActive(player)) {
                    List<String> subcommands = java.util.Arrays.asList("stay", "follow");
                    return subcommands.stream()
                            .filter(s -> s.startsWith(args[0].toLowerCase()))
                            .collect(java.util.stream.Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}

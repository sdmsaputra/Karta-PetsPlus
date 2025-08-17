package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PetsReloadCommandCompleter implements TabCompleter {

    private final KartaPetsPlus plugin;

    public PetsReloadCommandCompleter(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // No sub-commands for /petsreload, so return an empty list
        return new ArrayList<>();
    }
}

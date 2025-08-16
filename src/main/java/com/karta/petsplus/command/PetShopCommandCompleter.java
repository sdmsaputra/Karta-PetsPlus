package com.karta.petsplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PetShopCommandCompleter implements TabCompleter {

    private final List<String> subcommands = Arrays.asList("add");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("kartapetsplus.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subcommands, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            List<String> entityTypes = Arrays.stream(EntityType.values())
                    .filter(EntityType::isAlive)
                    .map(Enum::name)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], entityTypes, new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            return Collections.singletonList("<price>");
        }

        return Collections.emptyList();
    }
}

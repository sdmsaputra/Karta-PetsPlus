package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.command.subcommand.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class ParentCommand implements CommandExecutor, TabCompleter {

    protected final KartaPetsPlus plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public ParentCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    protected void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            Optional<SubCommand> subCommand = getSubCommand(args[0]);
            if (subCommand.isPresent()) {
                subCommand.get().perform(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }
        // Default behavior when no subcommand is found or no args are provided
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                completions.add(subCommand.getName());
            }
            return completions;
        } else if (args.length > 1) {
            Optional<SubCommand> subCommand = getSubCommand(args[0]);
            if (subCommand.isPresent()) {
                return subCommand.get().onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return null;
    }

    private Optional<SubCommand> getSubCommand(String name) {
        return subCommands.stream().filter(sc -> sc.getName().equalsIgnoreCase(name)).findFirst();
    }
}

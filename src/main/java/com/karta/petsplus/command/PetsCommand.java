package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.command.subcommand.FollowCommand;
import com.karta.petsplus.command.subcommand.ListCommand;
import com.karta.petsplus.command.subcommand.RemoveCommand;
import com.karta.petsplus.command.subcommand.RenameCommand;
import com.karta.petsplus.command.subcommand.GenerateMobsCommand;
import com.karta.petsplus.command.subcommand.ReloadCommand;
import com.karta.petsplus.command.subcommand.StayCommand;
import com.karta.petsplus.ui.PetManagementGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PetsCommand extends ParentCommand {

    public PetsCommand(KartaPetsPlus plugin) {
        super(plugin);
        registerSubCommand(new StayCommand(plugin));
        registerSubCommand(new FollowCommand(plugin));
        registerSubCommand(new RenameCommand(plugin));
        registerSubCommand(new ListCommand(plugin));
        registerSubCommand(new RemoveCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
        registerSubCommand(new GenerateMobsCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (super.onCommand(sender, command, label, args)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return true;
        }

        Player player = (Player) sender;
        PetManagementGUI.open(plugin, player, 0);
        return true;
    }
}

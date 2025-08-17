package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ListCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public ListCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all of your pets.";
    }

    @Override
    public String getSyntax() {
        return "/pets list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return;
        }

        Player player = (Player) sender;
        List<Pet> pets = plugin.getPlayerDataManager().getPets(player);

        if (pets.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "no-pets-owned", "<red>You do not own any pets.</red>");
            return;
        }

        plugin.getMessageManager().sendMessage(player, "pet-list-header", "<green>Your Pets:</green>");
        for (Pet pet : pets) {
            plugin.getMessageManager().sendMessage(player, "pet-list-entry", "<gray>- <pet_name> (<pet_type>)</gray>",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("pet_name", pet.getPetName()),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("pet_type", pet.getPetType().name()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

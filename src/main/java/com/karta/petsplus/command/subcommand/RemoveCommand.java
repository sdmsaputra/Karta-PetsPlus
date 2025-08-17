package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RemoveCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public RemoveCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes one of your pets.";
    }

    @Override
    public String getSyntax() {
        return "/pets remove <pet_id>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "invalid-arguments", "<red>Invalid arguments. Usage: " + getSyntax() + "</red>");
            return;
        }

        UUID petId;
        try {
            petId = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            plugin.getMessageManager().sendMessage(player, "invalid-pet-id", "<red>Invalid pet ID.</red>");
            return;
        }

        Optional<Pet> petOpt = plugin.getPlayerDataManager().getPet(player, petId);

        if (petOpt.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "pet-not-found", "<red>Pet not found.</red>");
            return;
        }

        Pet pet = petOpt.get();
        plugin.getPlayerDataManager().removePet(player, pet);
        plugin.getMessageManager().sendMessage(player, "pet-removed", "<green>You have removed <pet_name>.</green>", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("pet_name", pet.getPetName()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

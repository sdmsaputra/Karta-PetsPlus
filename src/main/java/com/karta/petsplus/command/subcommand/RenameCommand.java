package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.event.PetRenameEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RenameCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public RenameCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Renames your pet.";
    }

    @Override
    public String getSyntax() {
        return "/pets rename <pet_id> <new_name>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
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
        String newName = String.join(" ", args).substring(args[0].length() + 1);

        PetRenameEvent event = new PetRenameEvent(player, pet, newName);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.getMessageManager().sendMessage(player, "rename-cancelled", "<red>You cannot use that name.</red>");
            return;
        }

        pet.setPetName(event.getNewName());
        plugin.getPlayerDataManager().savePlayerPet(player, pet);
        plugin.getMessageManager().sendMessage(player, "pet-renamed", "<green>You have renamed your pet to <pet_name>.</green>", Placeholder.parsed("pet_name", pet.getPetName()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

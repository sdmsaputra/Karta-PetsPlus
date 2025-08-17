package com.karta.petsplus.command.subcommand;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StayCommand implements SubCommand {

    private final KartaPetsPlus plugin;

    public StayCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stay";
    }

    @Override
    public String getDescription() {
        return "Tells your pet to stay.";
    }

    @Override
    public String getSyntax() {
        return "/pets stay";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command", "<red>This command can only be run by a player.</red>");
            return;
        }

        Player player = (Player) sender;
        Optional<Pet> activePetOpt = plugin.getPetManager().getActivePet(player);

        if (activePetOpt.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "no-pet-active", "<red>You do not have an active pet.</red>");
            return;
        }

        Pet activePet = activePetOpt.get();
        if (activePet.getStatus() == Pet.PetStatus.STAY) {
            plugin.getMessageManager().sendMessage(player, "pet-already-staying", "<red>Your pet is already staying.</red>");
        } else {
            activePet.setStatus(Pet.PetStatus.STAY);
            plugin.getPlayerDataManager().savePlayerPet(player, activePet);
            plugin.getMessageManager().sendMessage(player, "pet-now-staying", "<green><pet_name> is now staying.</green>", Placeholder.parsed("pet_name", activePet.getPetName()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

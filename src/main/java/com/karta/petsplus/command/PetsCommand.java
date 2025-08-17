package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import com.karta.petsplus.data.Pet;
import com.karta.petsplus.ui.PetManagementGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Handles the /pets command, which opens the pet management menu
 * and provides subcommands for pet interaction.
 */
public class PetsCommand implements CommandExecutor {

    private final KartaPetsPlus plugin;

    public PetsCommand(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            PetManagementGUI.open(plugin, player, 0);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Optional<Pet> activePetOpt = plugin.getPetManager().getActivePet(player);

        if ("stay".equals(subCommand)) {
            if (activePetOpt.isEmpty()) {
                plugin.getMessageManager().sendMessage(player, "no-pet-active");
                return true;
            }
            Pet activePet = activePetOpt.get();
            if (activePet.getStatus() == Pet.PetStatus.STAY) {
                plugin.getMessageManager().sendMessage(player, "pet-already-staying");
            } else {
                activePet.setStatus(Pet.PetStatus.STAY);
                plugin.getStorageManager().savePlayerPet(player, activePet);
                plugin.getMessageManager().sendMessage(player, "pet-now-staying", Map.of("pet_name", activePet.getPetName()));
            }
            return true;
        }

        if ("follow".equals(subCommand)) {
            if (activePetOpt.isEmpty()) {
                plugin.getMessageManager().sendMessage(player, "no-pet-active");
                return true;
            }
            Pet activePet = activePetOpt.get();
            if (activePet.getStatus() == Pet.PetStatus.SUMMONED) {
                plugin.getMessageManager().sendMessage(player, "pet-already-following");
            } else {
                activePet.setStatus(Pet.PetStatus.SUMMONED);
                plugin.getStorageManager().savePlayerPet(player, activePet);
                plugin.getMessageManager().sendMessage(player, "pet-now-following", Map.of("pet_name", activePet.getPetName()));
            }
            return true;
        }

        // Fallback to GUI if subcommand is not recognized
        PetManagementGUI.open(plugin, player, 0);
        return true;
    }
}

package com.karta.petsplus.command;

import com.karta.petsplus.KartaPetsPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PetShopCommandCompleter implements TabCompleter {

    private final KartaPetsPlus plugin;

    public PetShopCommandCompleter(KartaPetsPlus plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("kartapetsplus.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "edit");
        }

        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    return Arrays.stream(EntityType.values())
                            .filter(EntityType::isAlive)
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
                if (args.length == 4) {
                    return Arrays.stream(Material.values())
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (args.length == 2) {
                    return new ArrayList<>(plugin.getConfigManager().getPets().getConfigurationSection("pets").getKeys(false));
                }
                if (args.length == 3) {
                    return Arrays.asList("name", "icon", "description", "price", "delete");
                }
                if (args.length == 4 && args[2].equalsIgnoreCase("icon")) {
                    return Arrays.stream(Material.values())
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
            }
        }

        return new ArrayList<>();
    }
}

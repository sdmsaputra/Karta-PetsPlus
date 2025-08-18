package com.karta.petsplus.commands;

import com.karta.petsplus.PetData;
import com.karta.petsplus.PetManager;
import com.karta.petsplus.PetType;
import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.shop.CurrencyProvider;
import com.karta.petsplus.shop.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PetsCommand implements CommandExecutor, TabCompleter {

    private final PetsPlus plugin;
    private final PetManager petManager;

    public PetsCommand(PetsPlus plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("petsplus.use")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(player);
                break;
            case "shop":
                if (!player.hasPermission("petsplus.shop")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getShopManager().openShop(player);
                break;
            case "buy":
                if (!player.hasPermission("petsplus.buy")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("buy-usage")); // Needs to be added to messages.yml
                    return true;
                }
                handleBuyCommand(player, args[1]);
                break;
            case "summon":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("summon-usage"));
                    return true;
                }
                petManager.summonPet(player, args[1]);
                break;
            case "dismiss":
                petManager.dismissPet(player, false);
                break;
            case "rename":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("rename-usage"));
                    return true;
                }
                String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                petManager.renamePet(player, newName);
                break;
            case "list":
                sendPetList(player);
                break;
            case "reload":
                if (!player.hasPermission("petsplus.admin")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getConfigManager().reloadConfig();
                plugin.getMessageManager().reloadMessages();
                plugin.getShopManager().reload();
                player.sendMessage(plugin.getMessageManager().getMessage("reload"));
                break;
            default:
                player.sendMessage(plugin.getMessageManager().getMessage("unknown-command"));
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        plugin.getMessageManager().getHelpMessage().forEach(player::sendMessage);
    }

    private void sendPetList(Player player) {
        PetData petData = petManager.getPlayerData(player);
        if (petData == null) {
            // Data might still be loading
            player.sendMessage(plugin.getMessageManager().getMessage("player-data-loading"));
            return;
        }

        List<String> ownedPets = petData.getOwnedPets();
        if (ownedPets.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-pets-owned"));
            return;
        }

        player.sendMessage(plugin.getMessageManager().getMessage("owned-pets-header"));
        for (String petType : ownedPets) {
            player.sendMessage(plugin.getMessageManager().getMessage("owned-pets-entry").replace("{pet_type}", petType));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("summon", "dismiss", "rename", "list", "help", "shop", "buy"));
            if (sender.hasPermission("petsplus.admin")) {
                subcommands.add("reload");
            }
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("summon")) {
            return plugin.getConfigManager().getPetTypes().keySet().stream()
                    .filter(type -> sender.hasPermission("petsplus.summon." + type) || sender.hasPermission("petsplus.summon.*"))
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
            if (!sender.hasPermission("petsplus.buy")) {
                return new ArrayList<>();
            }
            ShopManager shopManager = plugin.getShopManager();
            return plugin.getConfigManager().getPetTypes().keySet().stream()
                    .filter(petType -> {
                        ConfigurationSection override = shopManager.getShopConfig().getOverride(petType);
                        ConfigurationSection defaults = shopManager.getShopConfig().getDefaultPetSection();
                        boolean isHidden = (override != null) ? override.getBoolean("hidden", defaults.getBoolean("hidden")) : defaults.getBoolean("hidden");
                        boolean isPurchasable = (override != null) ? override.getBoolean("purchasable", defaults.getBoolean("purchasable")) : defaults.getBoolean("purchasable");
                        return !isHidden && isPurchasable;
                    })
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void handleBuyCommand(Player player, String petTypeName) {
        ShopManager shopManager = plugin.getShopManager();
        PetType petType = plugin.getConfigManager().getPetType(petTypeName);

        if (petType == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("pet-not-found"));
            return;
        }

        // Check for permission for specific pet
        if (!player.hasPermission("petsplus.buy." + petType.getInternalName()) && !player.hasPermission("petsplus.buy.*")) {
             player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
             return;
        }

        ConfigurationSection override = shopManager.getShopConfig().getOverride(petType.getInternalName());
        ConfigurationSection defaults = shopManager.getShopConfig().getDefaultPetSection();

        boolean isHidden = (override != null) ? override.getBoolean("hidden", defaults.getBoolean("hidden")) : defaults.getBoolean("hidden");
        if (isHidden) {
            player.sendMessage(plugin.getMessageManager().getMessage("pet-not-purchasable"));
            return;
        }

        boolean isPurchasable = (override != null) ? override.getBoolean("purchasable", defaults.getBoolean("purchasable")) : defaults.getBoolean("purchasable");
        if (!isPurchasable) {
            player.sendMessage(plugin.getMessageManager().getMessage("pet-not-purchasable"));
            return;
        }

        double price = (override != null) ? override.getDouble("price", defaults.getDouble("price")) : defaults.getDouble("price");
        String currencyName = (override != null) ? override.getString("currency", defaults.getString("currency")) : defaults.getString("currency");

        CurrencyProvider currency = shopManager.getCurrencyProvider(currencyName)
                .orElse(shopManager.getDefaultCurrencyProvider());

        if (currency == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("currency-provider-unavailable"));
            return;
        }

        // The purchase handler will deal with already owned pets, funds, etc.
        shopManager.getPurchaseHandler().attemptPurchase(player, petType, price, currency);
    }
}

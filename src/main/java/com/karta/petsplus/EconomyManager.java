package com.karta.petsplus;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final PetsPlus plugin;
    private Economy economy;

    public EconomyManager(PetsPlus plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isEconomyEnabled() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (economy == null) return 0;
        return economy.getBalance(player);
    }

    public boolean hasEnough(Player player, double amount) {
        if (economy == null) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public String getCurrencyName(double amount) {
        if (economy == null) return "Coins";
        return amount == 1 ? economy.currencyNameSingular() : economy.currencyNamePlural();
    }
}

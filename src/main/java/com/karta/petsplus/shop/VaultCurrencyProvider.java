package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultCurrencyProvider implements CurrencyProvider {

    private final PetsPlus plugin;
    private Economy economy;

    public VaultCurrencyProvider(PetsPlus plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.economy = null;
            return;
        }
        this.economy = rsp.getProvider();
    }

    public boolean isEnabled() {
        return this.economy != null;
    }

    @Override
    public String getInternalName() {
        return "VAULT";
    }

    @Override
    public String getCurrencyName() {
        return isEnabled() ? economy.getName() : "Money";
    }

    @Override
    public String getCurrencySymbol() {
        return isEnabled() ? economy.currencyNameSingular() : "$";
    }

    @Override
    public boolean has(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }
}

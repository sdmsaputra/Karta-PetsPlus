package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TokenCurrencyProvider implements CurrencyProvider {

    private final String currencyName;
    private final String currencySymbol;
    private TokenManager tokenManager;
    private boolean enabled = false;

    public TokenCurrencyProvider(PetsPlus plugin, String currencyName, String currencySymbol) {
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
        if (Bukkit.getPluginManager().isPluginEnabled("TokenManager")) {
            this.tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
            this.enabled = this.tokenManager != null;
        } else {
            plugin.getLogger().warning("TokenManager plugin not found, Tokens currency will not be available.");
        }
    }

    @Override
    public String getInternalName() {
        return "TOKENS";
    }

    @Override
    public String getCurrencyName() {
        return currencyName;
    }

    @Override
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    @Override
    public boolean has(Player player, double amount) {
        if (!enabled) return false;
        return tokenManager.getTokens(player).orElse(0) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!enabled) return false;
        tokenManager.removeTokens(player, (long) Math.round(amount));
        return true; // TokenManager doesn't return a boolean, so we assume success
    }

    @Override
    public double getBalance(Player player) {
        if (!enabled) return 0;
        return tokenManager.getTokens(player).orElse(0);
    }

    public boolean isEnabled() {
        return enabled;
    }
}

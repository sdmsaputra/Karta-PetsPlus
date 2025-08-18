package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import com.karta.petsplus.api.tokens.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TokenCurrencyProvider implements CurrencyProvider {

    private boolean isEnabled;

    public TokenCurrencyProvider(PetsPlus plugin) {
        this.isEnabled = Bukkit.getPluginManager().getPlugin("TokenManager") != null;
    }

    @Override
    public String getInternalName() {
        return "TOKENS";
    }

    @Override
    public String getCurrencyName() {
        return "Tokens";
    }

    @Override
    public String getCurrencySymbol() {
        return "tkn";
    }

    @Override
    public boolean has(Player player, double amount) {
        if (!isEnabled) return false;
        // The placeholder API uses static methods.
        // Also assuming tokens are stored as long.
        return TokenManager.hasTokens(player, (long) Math.round(amount));
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled || amount < 0) return false;
        return TokenManager.removeTokens(player, (long) Math.round(amount));
    }

    @Override
    public double getBalance(Player player) {
        if (!isEnabled) return 0;
        return TokenManager.getTokens(player);
    }
}

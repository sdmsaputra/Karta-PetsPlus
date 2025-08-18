package com.karta.petsplus.shop;

import com.karta.petsplus.PetsPlus;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PointsCurrencyProvider implements CurrencyProvider {

    private PlayerPointsAPI pointsApi;
    private final String currencyName;
    private final String currencySymbol;
    private boolean enabled = false;

    public PointsCurrencyProvider(PetsPlus plugin, String currencyName, String currencySymbol) {
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.pointsApi = PlayerPoints.getInstance().getAPI();
            this.enabled = this.pointsApi != null;
        } else {
            plugin.getLogger().warning("PlayerPoints plugin not found, Points currency will not be available.");
        }
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
        return pointsApi.look(player.getUniqueId()) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!enabled) return false;
        return pointsApi.take(player.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public double getBalance(Player player) {
        if (!enabled) return 0;
        return pointsApi.look(player.getUniqueId());
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getInternalName() {
        return "POINTS";
    }
}
